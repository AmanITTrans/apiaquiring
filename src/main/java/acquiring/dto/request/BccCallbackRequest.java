package acquiring.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BccCallbackRequest {
    @NotBlank
    private String id;
    private String acquirerOrderId;
    private String refNo;
    private Integer status;
    private Integer state;
}

