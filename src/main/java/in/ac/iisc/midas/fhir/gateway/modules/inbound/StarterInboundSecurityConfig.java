package in.ac.iisc.midas.fhir.gateway.modules.inbound;

import in.ac.iisc.midas.fhir.gateway.modules.inbound.authentication.AuthenticationConfig;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.authorization.AuthorizationConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AuthenticationConfig.class, AuthorizationConfig.class})
public class StarterInboundSecurityConfig {
}
