package acquiring.integration.bcc;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnicornClient {

    private final UnicornProperties props;

    private RestTemplate restTemplate;

    private static final MediaType XML = MediaType.TEXT_XML;

    @PostConstruct
    private void init() {
        this.restTemplate = buildRestTemplate();
    }

    // ==========================
    // Public API
    // ==========================

    public String createPayment(String merchantOrderId,
                                BigDecimal amount,
                                List<Long> orderIds,
                                String clientIpAddress,
                                String redirectSuccessUrl,
                                String redirectFailUrl) {
        long amountTiyn = UnicornXmlBuilder.toTiyn(amount);

        String xml = UnicornXmlBuilder.buildAuthAcquiring(
                props.getPoint(),
                props.getService(),
                merchantOrderId,
                amountTiyn,
                props.getCurrency(),
                props.getNotifyUrl(),
                props.getRedirectUrl(),
                redirectSuccessUrl,
                redirectFailUrl,
                props.getEmail(),
                clientIpAddress,
                "Оплата заказа(ов): " + orderIds,
                Map.of("channel", "web")
        );

        String respXml = postXml(merchantOrderId, xml, "PAYMENT");
        return extractPayUrl(respXml);
    }

    public String getStatus(String merchantOrderId) {
        String xml = UnicornXmlBuilder.buildStatus(
                props.getPoint(),
                merchantOrderId);

        String respXml = postXml(merchantOrderId, xml, "PAYMENT");
        return extractPayUrl(respXml);
    }

    public void sendCancelXml(String merchantOrderId, String xml) {
        String respXml = postXml(merchantOrderId, xml, "CANCEL");
        assertNoUnicornError(merchantOrderId, respXml, "CANCEL");
    }

    public String sendPaymentXml(String merchantOrderId, String xml) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.setAccept(Collections.singletonList(MediaType.TEXT_XML));

            HttpEntity<String> entity = new HttpEntity<>(xml, headers);

            log.info("Sending payment request to: {}", props.getUrl());
            log.debug("Request XML: {}", xml);

            ResponseEntity<String> resp = restTemplate.postForEntity(props.getUrl(), entity, String.class);

            log.info("Unicorn response status: {}", resp.getStatusCode());
            log.info("Unicorn response body: {}", resp.getBody());
            return extractPayUrl(resp.getBody());
        } catch (HttpStatusCodeException e) {
            log.error("event=ERROR_EXTERNAL status=502 code=EXTERNAL_ERROR msg=\"{}\"",
                    e.getMessage()
            );
            throw e;
        } catch (Exception e) {
            log.error("event=ERROR_INTERNAL status=500 code=INTERNAL_ERROR msg=\"{}\" ex={}",
                    e.getMessage(), e.getClass().getName()
            );
            throw e;
        }
    }


    // ==========================
    // Core HTTP
    // ==========================

    private String postXml(String merchantOrderId, String xml, String op) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(XML);
            headers.setAccept(Collections.singletonList(XML));

            HttpEntity<String> entity = new HttpEntity<>(xml, headers);

            log.info("Sending {} request to: {}", op, props.getUrl());
            log.debug("{} Request XML: {}", op, xml);

            ResponseEntity<String> resp = restTemplate.postForEntity(props.getUrl(), entity, String.class);

            log.info("Unicorn {} response status: {}", op, resp.getStatusCode());
            log.info("Unicorn {} response body: {}", op, resp.getBody());

            return resp.getBody();
        } catch (HttpStatusCodeException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    private void assertNoUnicornError(String merchantOrderId, String responseXml, String op) {
        if (responseXml == null || responseXml.isBlank()) {
            throw new IllegalStateException("Empty Unicorn response for " + op);
        }

        try {
            Document doc = parseXmlSecure(responseXml);
            XPath xp = XPathFactory.newInstance().newXPath();

            // 1) <error-detail>
            String errorText = (String) xp.evaluate("//error-detail/text()", doc, XPathConstants.STRING);
            if (errorText != null && !errorText.isBlank()) {
                String code = (String) xp.evaluate("//error-detail/@code", doc, XPathConstants.STRING);
                String desc = (String) xp.evaluate("//error-detail/@desc", doc, XPathConstants.STRING);

                throw new IllegalStateException("Unicorn " + op + " error: code=" + safe(code)
                        + " desc=" + safe(desc) + " detail=" + errorText.trim());
            }

            // 2) <result code="0|...">
            String codeAttr = (String) xp.evaluate("//result/@code", doc, XPathConstants.STRING);
            if (codeAttr != null && !codeAttr.isBlank() && !"0".equals(codeAttr.trim())) {
                String desc = (String) xp.evaluate("//result/@desc", doc, XPathConstants.STRING);
                String state = (String) xp.evaluate("//result/@state", doc, XPathConstants.STRING);

                throw new IllegalStateException("Unicorn " + op + " declined: code=" + codeAttr.trim()
                        + " desc=" + safe(desc) + " state=" + safe(state));
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Unicorn " + op + " response", e);
        }
    }

    private static Document parseXmlSecure(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        return dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    public static String extractPayUrl(String xml) {
        if (xml == null || xml.isBlank()) return null;
        try {
            Document doc = parseXmlSecure(xml);

            XPath xp = XPathFactory.newInstance().newXPath();
            String payUrl = (String) xp.evaluate("//input[@key='pay-url']/@value", doc, XPathConstants.STRING);
            if (payUrl != null && !payUrl.isBlank()) {
                return payUrl.trim();
            }

            Pattern p = Pattern.compile(
                    "<input\\s+[^>]*key=['\"]pay-url['\"][^>]*value=['\"]([^'\"]+)['\"][^>]*/?>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher m = p.matcher(xml);
            if (m.find()) {
                return m.group(1).trim();
            }
            return null;
        } catch (Exception e) {
            log.warn("extractPayUrl failed", e);
            return null;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    /** Дописывает параметр {@code id=<merchantOrderId>} в URL (учитывая наличие '?'). */
    private static String withMerchantOrderId(String url, String merchantOrderId) {
        if (url == null || url.isBlank() || merchantOrderId == null || merchantOrderId.isBlank()) {
            return url;
        }
        String encoded = java.net.URLEncoder.encode(merchantOrderId, StandardCharsets.UTF_8);
        return url + (url.contains("?") ? "&" : "?") + "id=" + encoded;
    }

    // ==========================
    // RestTemplate build (mTLS)
    // ==========================

    private RestTemplate buildRestTemplate() {
        try {
            log.info("Building RestTemplate with properties: {}", props);

            if (props == null || props.getKeystore() == null) {
                throw new IllegalStateException("UnicornProperties or keystore are null");
            }

            var keyStore = loadKeyStore(
                    props.getKeystore().getType(),
                    props.getKeystore().getPath(),
                    props.getKeystore().getPassword()
            );

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, props.getKeystore().getPassword().toCharArray());

            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), trustAll, new SecureRandom());

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

            var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            HttpClient httpClient = HttpClientBuilder.create()
                    .setConnectionManager(connectionManager)
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);

            if (props.getHttp() != null) {
                requestFactory.setConnectTimeout(Duration.ofMillis(props.getHttp().getConnectTimeoutMs()));
            }

            log.info("RestTemplate built successfully with mTLS configuration");
            return new RestTemplate(requestFactory);
        } catch (Exception e) {
            log.error("Failed to build Unicorn mTLS RestTemplate", e);
            throw new IllegalStateException("Failed to build Unicorn mTLS RestTemplate", e);
        }
    }


    private KeyStore loadKeyStore(String type, String path, String password) {
        try {
            if (path == null || path.trim().isEmpty()) {
                throw new IllegalArgumentException("Keystore path is null or empty");
            }

            File keyStoreFile = new File(path);
            if (!keyStoreFile.exists()) {
                InputStream classpathStream = getClass().getClassLoader().getResourceAsStream(path);
                if (classpathStream != null) {
                    log.info("Loading keystore from classpath: {}", path);
                    KeyStore keyStore = KeyStore.getInstance(type);
                    keyStore.load(classpathStream, password.toCharArray());
                    classpathStream.close();
                    return keyStore;
                }
                throw new IllegalArgumentException("Keystore file does not exist: " + path);
            }

            KeyStore keyStore = KeyStore.getInstance(type);
            try (InputStream keyStoreStream = new FileInputStream(keyStoreFile)) {
                keyStore.load(keyStoreStream, password.toCharArray());
            }

            log.info("Successfully loaded keystore from: {}", path);
            log.info("Keystore type: {}, size: {}", keyStore.getType(), keyStore.size());

            var aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                log.info("Found alias in keystore: {}, is key entry: {}", alias, keyStore.isKeyEntry(alias));
            }

            return keyStore;
        } catch (Exception e) {
            log.error("Failed to load keystore from path: {}", path, e);
            throw new IllegalStateException("Failed to load keystore from path: " + path, e);
        }
    }
}