package acquiring.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreatePaymentRequest {

    private String paymentId;
    private BigDecimal amount;
    private String product;

    private String returnUrl;

    private String redirectFailUrl;
    private String redirectSuccessUrl;
    private String orderNumber;

    private String locale;
    private List<Long> orderId;

    private int paymentMethod;

    private String currency;
    private String clientIpAddress;

    private String description;
}