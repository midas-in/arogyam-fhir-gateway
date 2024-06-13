package in.ac.iisc.midas.fhir.gateway.modules.inbound.authorization;

import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.PermissionGroup;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Set;

@ConfigurationProperties(prefix = "hapi.fhir.security.inbound.authorization")
@Getter
@Setter
public class AuthorizationProperties {

	private Set<PermissionGroup> anonymousAccessPermission = Collections.emptySet();
	private Boolean allowAnonymousAccess = true;
	private Boolean anonymousAccessPermissionRequired = false;

	private Boolean enabled;
}
