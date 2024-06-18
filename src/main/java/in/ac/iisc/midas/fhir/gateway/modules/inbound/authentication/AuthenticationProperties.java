package in.ac.iisc.midas.fhir.gateway.modules.inbound.authentication;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hapi.fhir.security.inbound.authentication")
@Getter
@Setter
public class AuthenticationProperties {
	// Convert the user to be anonymous when the authentication fails with auth error
	private Boolean proceedToAuthorizationOnFailure = false;
	private Boolean proceedToAuthorizationOnNoAuth = false;
	private Boolean enabled = false;
	private String userRealmUri;
}
