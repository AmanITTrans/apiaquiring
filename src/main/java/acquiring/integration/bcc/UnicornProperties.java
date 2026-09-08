package acquiring.integration.bcc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "unicorn")
@Getter @Setter
public class UnicornProperties {

    /** <a href="https://test-payment.unicornlab.kz/external/extended-cert">...</a> (TEST) /
     * <a href="https://payment.unicornlab.kz/external/extended-cert">...</a> (PROD) */
    private String url;
    /** Номер терминала (point) */
    private String point;
    /** 1330 (1-стадийный) или 1331 (2-стадийный) */
    private final String service = "1330";
    /** Код валюты, 398 = KZT */
    private final Integer currency = 398;
    private String email;

    private String notifyUrl;
    private String redirectUrl;
    private String redirectSuccessUrl;
    private String redirectFailUrl;

    private final Keystore keystore = new Keystore();
    private final Truststore truststore = new Truststore();
    private final Http http = new Http();

    @Setter
    @Getter
    public static class Keystore {
        /** Путь к PKCS12-файлу клиентского сертификата */
        private String path;
        private String password;
        private String type = "PKCS12";

    }

    @Setter
    @Getter
    public static class Truststore {
        /** Опционально: путь к truststore */
        private String path;
        private String password;
        private String type = "JKS";

    }

    @Setter
    @Getter
    public static class Http {
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 10000;
        private int socketTimeoutMs = 10000;

    }
}