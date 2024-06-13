package in.ac.iisc.midas.fhir.gateway.modules.inbound.authentication;

import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.PermissionGroup;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "hapi.fhir.security.inbound.authentication")
@Getter
@Setter
public class AuthenticationProperties {
	private Keycloak keycloak;
	// Convert the user to be anonymous when the authentication fails with auth error
	private Boolean proceedToAuthorizationOnFailure = false;
	private Boolean proceedToAuthorizationOnNoAuth = false;
	private Boolean enabled = false;

	@Getter
	@Setter
	public static class Keycloak {
		private String baseUri;
		private String managementRealm;
		private String userRealm;
		private String clientId;
		private String username;
		private String password;
		@NestedConfigurationProperty
		private List<Group> groups = Collections.emptyList();
		@NestedConfigurationProperty
		private List<User> users = Collections.emptyList();

		public String userRealmUri() {
			return String.format("%s/realms/%s", getBaseUri(), getUserRealm());
		}

		public String userRealmAdminUri() {
			return String.format("%s/admin/realms/%s", getBaseUri(), getUserRealm());
		}

		@Getter
		@Setter
		public static class Group {
			private String name;
			private boolean isDefault = false;
			private List<PermissionGroup> permissions = Collections.emptyList();
		}

		@Getter
		@Setter
		public static class User {
			private String username;
			private String givenName;
			private String familyName;
			private String email;
			private List<String> groups = Collections.emptyList();
			private Boolean isPractitioner = false;
			private List<PermissionGroup> permissions = Collections.emptyList();
		}
	}
}
