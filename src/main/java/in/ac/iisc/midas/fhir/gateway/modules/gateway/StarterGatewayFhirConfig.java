package in.ac.iisc.midas.fhir.gateway.modules.gateway;


import in.ac.iisc.midas.fhir.gateway.modules.cors.CorsProperties;
import in.ac.iisc.midas.fhir.gateway.modules.logging.LoggerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LoggerProperties.class,
        CorsProperties.class,
        GatewayFhirServerConfigR4.class
})
public class StarterGatewayFhirConfig {
}
