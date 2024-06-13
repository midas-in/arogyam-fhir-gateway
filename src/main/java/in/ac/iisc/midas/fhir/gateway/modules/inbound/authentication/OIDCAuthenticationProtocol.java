package in.ac.iisc.midas.fhir.gateway.modules.inbound.authentication;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.Permission;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.UserSessionDetail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class OIDCAuthenticationProtocol implements AuthenticationProtocol {
	private final JwtDecoder jwtDecoder;

	private static final String name = "OAuth2";

	private static String getToken(RequestDetails requestDetails) {
		var authHeader = requestDetails.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader == null) return null;
		var parts = authHeader.split(" ");
		if (parts.length < 2 || !parts[0].equalsIgnoreCase("bearer")) return null;
		return parts[1];
	}

	@Override
	public boolean canAuthenticate(RequestDetails requestDetails) {
		return StringUtils.isNotBlank(getToken(requestDetails));
	}

	@Override
	public UserSessionDetail authenticate(RequestDetails requestDetails) {
		// Assuming this won't return a null or empty token as check might have been done using canAuthenticate
		var token = getToken(requestDetails);
		var jwt = jwtDecoder.decode(token);

		if (jwt.getSubject() == null) {
			throw new RuntimeException("Missing subject in jwt");
		}

		return UserSessionDetail.builder()
			.id(jwt.getSubject())
			.jwt(jwt)
			.fhirContext(Collections.singletonList(requestDetails.getFhirContext()))
			.scopes(extractScopes(jwt))
			.oidcClientId(jwt.getClaimAsString("azp"))
			.accountDisabled(false)
			.email(jwt.getClaimAsString("email"))
			.emailVerified(jwt.getClaimAsBoolean("email_verified"))
			.authorities(extractGrantedAuthorities(jwt))
			.accountLocked(false)
			.authorities(extractGrantedAuthorities(jwt))
			.givenName(jwt.getClaimAsString("given_name"))
			.familyName(jwt.getClaimAsString("family_name"))
			.username(jwt.getClaimAsString("preferred_username"))
			.usernameNamespace(jwt.getClaimAsString("azp"))
			.serviceAccount(false)
			.fhirUserUrl(String.format("%s/%s", "Practitioner", jwt.getSubject()))
			.build();

	}

	private static Set<String> extractScopes(Jwt jwt) {
		try {
			return Arrays.stream(jwt.getClaimAsString("scope").split(" ")).collect(Collectors.toSet());
		} catch (Exception e) {
			log.error("Unable to extract scope", e);
			return Collections.emptySet();
		}
	}

	private static Set<UserSessionDetail.GrantedAuthority> extractGrantedAuthorities(Jwt jwt) {
		try {
			var realmAccess = jwt.getClaimAsMap("realm_access");
			//noinspection unchecked,MismatchedQueryAndUpdateOfCollection
			List<String> permissions = (realmAccess != null ?
				((List<String>) realmAccess.getOrDefault("roles", Collections.emptyList())) :
				Collections.emptyList());

			if (permissions.isEmpty()) return Collections.emptySet();

			var argumentMap = new HashSetValuedHashMap<String, String>();
			//noinspection unchecked
			var argument = ((List<String>) jwt.getClaim("permission_argument"));
			if (argument == null) argument = Collections.emptyList();

			argument.stream()
				.map(entry -> entry.split("/", 2))
				.filter(v -> v.length == 2)
				.forEach(v -> argumentMap.put(v[0], v[1]));

			// Only accept permission as granted authorities and not the role itself
			return permissions.stream()
				.map(p -> {
					try {
						return Permission.valueOf(p);
					} catch (IllegalArgumentException e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.map(p -> new UserSessionDetail.GrantedAuthority(p, argumentMap))
				.collect(Collectors.toSet());

		} catch (Exception e) {
			log.error("Unable to extract permission", e);
			return Collections.emptySet();
		}
	}

	@Override
	public String name() {
		return name;
	}
}