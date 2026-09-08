package acquiring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Component {
    private String type;
    private String sub_type;
    private String index;
    private List<Parameter> parameters;
}