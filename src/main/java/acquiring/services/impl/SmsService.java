package acquiring.services.impl;

import acquiring.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    @Autowired
    private final RestTemplate restTemplate;

    @Value("${whatsapp.url}")
    private String url;
    @Value("${whatsapp.key}")
    private String key;
    @Value("${whatsapp.from}")
    private String from;
    @Value("${whatsapp.template}")
    private String template;
    @Value("${whatsapp.apiUrl}")
    private String apiUrl;
    @Value("${whatsapp.idInstances}")
    private List<String> idInstances;
    @Value("${whatsapp.apiTokenInstances}")
    private List<String> apiTokenInstances;

    public String sendCodeWhatsApp(String phone, String text) {
        for (int i = 0; i < idInstances.size(); i++) {
            String instance = idInstances.get(i);
            String token = apiTokenInstances.get(i);

            String msg = trySend(instance, token, phone, text);
            if (!msg.isEmpty()) {
                return msg;
            }
        }

        return "";
    }

    public void sendSmsWhatsapp(String recipient, String codeText) {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Component bodyComponent = new Component(
                "body",
                null,
                null,
                List.of(new Parameter("text", codeText))
        );

        Component buttonComponent = new Component(
                "button",
                "url",
                "0",
                List.of(new Parameter("text", codeText))
        );

        WhatsAppRequest requestBody = new WhatsAppRequest(
                from,
                "whatsapp",
                recipient,
                "template",
                new Template(
                        template,
                        new Language("ru"),
                        List.of(bodyComponent, buttonComponent)
                )
        );

        try {
            String jsonBody = mapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", key)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Response: {}", response.body());

        } catch (Exception e) {
            log.info("Error in whatsapp {}", e.getMessage());
        }
    }

    private String trySend(String instance, String token, String phone, String text) {
        try {
            if (instanceIsNotAuthorized(instance, token)) {
                log.warn("Instance {} is not authorized", instance);
                return "Instance "+instance+" is not authorized";
            }

            sendCodeWhatsappByInstance(instance, token, phone, text);
            return "";

        } catch (Exception e) {
            log.warn("WhatsApp send failed for instance {}: {}", instance, e.getMessage());
            return "WhatsApp send failed for instance "+instance+": "+ e.getMessage();
        }
    }

    private boolean instanceIsNotAuthorized(String idInstance, String apiTokenInstance) {
        String requestUrl = apiUrl +
                "/waInstance" + idInstance +
                "/getStateInstance/" +
                apiTokenInstance;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String response = restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, String.class).getBody();
        log.info("Response: " + response);

        return response == null || !response.contains("authorized");
    }

    private void sendCodeWhatsappByInstance(String idInstance, String apiTokenInstance, String phone, String text) throws Exception {
        String requestUrl = apiUrl +
                "/waInstance" + idInstance +
                "/sendMessage/" +
                apiTokenInstance;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String, String> payload = Map.of(
                "chatId", phone + "@c.us",
                "message", text
        );

        String body = new ObjectMapper().writeValueAsString(payload);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        String response = restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, String.class).getBody();
        log.info("Response: " + response);

        if (response == null || !response.contains("200")) {
            throw new Exception("SMS code send failed");
        }
    }
}
