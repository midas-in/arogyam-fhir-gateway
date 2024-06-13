package in.ac.iisc.midas.fhir.gateway.modules.gateway.common;

import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.condition.GatewayCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(GatewayProperties.class)
@Conditional({GatewayCondition.class})
@Configuration
public class StarterGatewayR4Config {

}
