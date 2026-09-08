package acquiring.controllers;

import acquiring.dto.SmsRequest;
import acquiring.services.impl.SmsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Integration")
@Slf4j
public class SmsController {

    private final SmsService service;

    @PostMapping("/send")
    public ResponseEntity<String> send(@Valid @RequestBody SmsRequest req) {

        log.info("Sending whatsapp request: to={}, message={}",
                req.getTo(), req.getMessage());

        String msg = service.sendCodeWhatsApp(req.getTo(), req.getMessage());

//        if (msg != null && !msg.isEmpty()) {
//            return ResponseEntity.status(HttpStatusCode.valueOf(500)).body(msg);
//        }

        return ResponseEntity.ok().body("ok");
    }
}