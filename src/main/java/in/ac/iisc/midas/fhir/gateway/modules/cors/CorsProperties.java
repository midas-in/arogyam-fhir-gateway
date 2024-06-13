package in.ac.iisc.midas.fhir.gateway.modules.cors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties("hapi.fhir.cors")
@Setter
@Getter
public class CorsProperties {
    private Boolean enabled = false;
    private Boolean allowCredentials = true;
    private Set<String> allowedOrigin = Set.of("*");

}
