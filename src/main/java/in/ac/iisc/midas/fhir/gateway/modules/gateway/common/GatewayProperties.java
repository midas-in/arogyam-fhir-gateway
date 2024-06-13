package in.ac.iisc.midas.fhir.gateway.modules.gateway.common;

import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.Target;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "hapi.fhir.gateway")
@Getter
@Setter
public class GatewayProperties {
	private Boolean enabled = false;
	private Boolean multiTenant = false;
	private List<Target> targets = Collections.emptyList();
}
