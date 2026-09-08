package acquiring.controllers;

import acquiring.dto.request.BccCallbackRequest;
import acquiring.dto.request.CreatePaymentRequest;
import acquiring.dto.response.CreatePaymentResponseClient;
import acquiring.dto.response.RefundFullResponse;
import acquiring.integration.bcc.BccPaymentWorkflowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bcc")
@RequiredArgsConstructor
@Tag(name = "BCC Integration")
@Slf4j
public class BccController {

    private final BccPaymentWorkflowService service;

    @PostMapping("/create")
    public ResponseEntity<CreatePaymentResponseClient> createPayment(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                                     @Valid @RequestBody CreatePaymentRequest req,
                                                                     HttpServletRequest request) {

        req.setPaymentId(idempotencyKey);
        req.setClientIpAddress(request.getRemoteAddr());
        log.info("Received payment creation request: amount={}, orderId={}, clientIp={}",
                req.getAmount(), req.getOrderId(), req.getClientIpAddress());
        return ResponseEntity.ok(service.createPayment(req));
    }

    @PostMapping("/create-refund")
    public ResponseEntity<RefundFullResponse> createRefund(@RequestBody String paymentId,
                                                           HttpServletRequest request) {
        log.info("Received payment refund request: orderId={}, clientIp={}", paymentId, request.getRemoteAddr());
        return ResponseEntity.ok(service.refundFull(paymentId));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callbackGet(@ModelAttribute BccCallbackRequest req,
                                              @RequestParam(name = "order.id", required = false) String acquirerOrderId) {
        log.info("Received callback get: status={}, state={}, acquirerOrderId={}, refNo={}",
                req.getStatus(), req.getState(), acquirerOrderId, req.getRefNo());
        req.setAcquirerOrderId(acquirerOrderId);
        return ResponseEntity.ok("OK");
    }

    @PostMapping(value="/callback", consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> callbackPost(@ModelAttribute BccCallbackRequest req,
                                               @RequestParam(name = "order.id", required = false) String acquirerOrderId) {
        log.info("Received callback post: status={}, state={}, acquirerOrderId={}, refNo={}",
                req.getStatus(), req.getState(), acquirerOrderId, req.getRefNo());
        req.setAcquirerOrderId(acquirerOrderId);
        service.handleCallback(req);
        return ResponseEntity.ok("OK");
    }
}