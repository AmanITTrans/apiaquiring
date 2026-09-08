package acquiring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundFullResponse {
    private String paymentToken;      // merchantOrderId (cb.id)
    private String acquirerOrderId;   // unicorn payment id (cb.acquirerOrderId)
    private String status;            // PENDING / DECLINED (на этом шаге финал обычно pending)
    private String message;
    private List<Long> orderIds;
}