package acquiring.integration.bcc;

import acquiring.db.enums.StatusTransactions;
import acquiring.dto.request.BccCallbackRequest;
import acquiring.dto.request.CreatePaymentRequest;
import acquiring.dto.response.CreatePaymentResponseClient;
import acquiring.dto.response.RefundFullResponse;
import acquiring.dto.response.TransactionStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class BccPaymentWorkflowService {

    private final UnicornClient unicornClient;
    private final UnicornProperties props;

    public CreatePaymentResponseClient createPayment(CreatePaymentRequest request) {

        String merchantOrderId = request.getPaymentId();

        String url = unicornClient.createPayment(
                merchantOrderId,
                request.getAmount(),
                request.getOrderId(),
                request.getClientIpAddress(),
                request.getRedirectSuccessUrl(),
                request.getRedirectFailUrl()
        );

        CreatePaymentResponseClient out = new CreatePaymentResponseClient();
        out.setToken(merchantOrderId);
        out.setUrl(url);
        return out;
    }

    public TransactionStatusResponse getTransactionStatus(String paymentId) {
        String url = unicornClient.getStatus(paymentId);

        return null;
    }

    public void handleCallback(BccCallbackRequest cb) {
        try {
            final Integer raw = cb.getStatus() != null ? cb.getStatus() : cb.getState();
            final int code = raw != null ? raw : -1;

            final StatusTransactions txStatus = switch (code) {
                case 60 -> StatusTransactions.APPROVED;      // успех
                case 80 -> StatusTransactions.DECLINED;      // отказ
                default -> StatusTransactions.PENDING;       // прочие/неизвестные → «ожидание»
            };
        } catch (Exception e) {
            log.error("BCC CALLBACK ERROR: {}", e.getMessage(), e);
        }
    }

    public RefundFullResponse refundFull(String paymentId) {
        String xml = UnicornXmlBuilder.buildCancel(
                props.getPoint(),
                paymentId,
                null
        );

        try {
            unicornClient.sendCancelXml(paymentId, xml);
        } catch (Exception e) {
            throw new RuntimeException("Refund request failed: " + e.getMessage(), e);
        }

        return new RefundFullResponse(
                paymentId,
                paymentId,
                "PENDING",
                "Refund request accepted",
                Collections.emptyList()
        );
    }
}