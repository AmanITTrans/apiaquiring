package acquiring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhatsAppRequest {
    private String from;
    private String messaging_product;
    private String to;
    private String type;
    private Template template;
}