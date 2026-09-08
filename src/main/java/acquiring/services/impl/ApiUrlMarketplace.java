package acquiring.services.impl;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("api")
public class ApiUrlMarketplace {

    private String outMemberId;
    private String access_token;
    private String language;
    private String refresh_token;
    private String appSecret;
    private String recommended;
    private String searchWithImageUrl;
    private String giveOfferIdWithImageId;
    private String URL_HEAD;
    private String categoryUrl;
    private String productsUrl;
    private String createOrder;
    private String flow;
    private String message;
    private String mobile;
    private String phone;
    private String postCode;
    private String cityText;
    private String provinceText;
    private String areaText;
    private String townText;
    private String address;
    private String districtCode;
    private Long adressId;
    private String addressCode;
    private String logisticsUrl;
    private String webSite;

    private String freight_estimate;
}
