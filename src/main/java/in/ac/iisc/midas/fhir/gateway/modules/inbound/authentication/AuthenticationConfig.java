package in.ac.iisc.midas.fhir.gateway.modules.inbound.authentication;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@Conditional(InboundSecurityAuthenticationCondition.class)
public class AuthenticationConfig {

	@Bean
	AuthenticationProperties authenticationProperties() {
		return new AuthenticationProperties();
	}

	@Bean
	AuthenticationService authenticationService(AuthenticationProtocol.Registry registry,
															  AuthenticationProperties properties) {
		return new AuthenticationService(registry, properties.getProceedToAuthorizationOnFailure());
	}

	@Bean
	AuthenticationInterceptor authenticationInterceptor(AuthenticationService authenticationService,
																		 AuthenticationProperties properties) {
		return new AuthenticationInterceptor(
			authenticationService,
			properties.getProceedToAuthorizationOnFailure(),
			properties.getProceedToAuthorizationOnNoAuth());
	}

	@Bean
	Keycloak keycloak(AuthenticationProperties properties) {
		var keycloakConfig = properties.getKeycloak();
		return KeycloakBuilder.builder()
			.serverUrl(keycloakConfig.getBaseUri())
			.realm(keycloakConfig.getManagementRealm())
			.clientId(keycloakConfig.getClientId())
			.username(keycloakConfig.getUsername())
			.password(keycloakConfig.getPassword())
			.grantType(OAuth2Constants.PASSWORD)
			.build();
	}

	@Bean
	KeycloakConfigurer keycloakConfigurer( RealmResource resource) {
		return new KeycloakConfigurer(resource);
	}

	@Bean
	RealmResource userRealm(Keycloak keycloak, AuthenticationProperties properties) {
		var keycloakConfig = properties.getKeycloak();
		return keycloak.realm(keycloakConfig.getUserRealm());
	}

	@Bean
	public JwtDecoder jwtDecoder(AuthenticationProperties properties) {
		return JwtDecoders.fromIssuerLocation(properties.getKeycloak().userRealmUri());
	}

	@Bean
	public OIDCAuthenticationProtocol oidcAuthenticationProtocol(JwtDecoder jwtDecoder) {
		return new OIDCAuthenticationProtocol(jwtDecoder);
	}

	@Bean
	public AuthenticationProtocol.Registry registry(OIDCAuthenticationProtocol authenticationProtocol) {
		return new AuthenticationProtocol.Registry(authenticationProtocol);
	}
}
