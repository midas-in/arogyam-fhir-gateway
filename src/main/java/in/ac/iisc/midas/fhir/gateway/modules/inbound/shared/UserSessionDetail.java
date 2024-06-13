package in.ac.iisc.midas.fhir.gateway.modules.inbound.shared;

import ca.uhn.fhir.context.FhirContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

@AllArgsConstructor
@Builder
@Getter
public class UserSessionDetail {
	private final String id;
	private final Jwt jwt;
	private final List<FhirContext> fhirContext;
	private final Set<String> scopes;
	private final String oidcClientId;
	private final Map<String, Object> userData = new HashMap<>();
	private final Boolean accountDisabled;
	private final String email;
	private final Boolean emailVerified;
	private final Boolean accountLocked;
	@Builder.Default
	private final Set<GrantedAuthority> authorities = Collections.emptySet();
	private final String givenName;
	private final String familyName;
	private final String username;
	private final String usernameNamespace;
	private final String systemUser;
	private final Boolean serviceAccount;
	private final String fhirUserUrl;

	@AllArgsConstructor
	public static class GrantedAuthority {
		@Getter
		private final Permission permission;
		private final MultiValuedMap<String, String> argumentMap;

		public Collection<String> getArgument() {
			return argumentMap.containsKey(permission.name()) ? argumentMap.get(permission.name()) : Collections.emptyList();
		}

		public boolean isNegativePermission() {
			return permission.name().toUpperCase().contains("BLOCK");
		}
	}
}
