package in.ac.iisc.midas.fhir.gateway.modules.inbound.authentication;


import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.Permission;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class KeycloakConfigurer {
    private final RealmResource realm;
    private final Keycloak keycloak;
    private final AuthenticationProperties.Keycloak keycloakConfig;
    private final RestTemplate restTemplate;

    @PostConstruct
    void configure() {
        addRolesIfMissing();
    }

    private void addRolesIfMissing() {
        var keycloakRoles = realm.roles().list();
        var missingPermissions = Arrays.stream(Permission.values())
                .filter(p -> keycloakRoles.stream().noneMatch(kr -> Objects.equals(kr.getName(), p.name())))
                .collect(Collectors.toSet());
        if (!missingPermissions.isEmpty()) {
            log.info(String.format("Missing %d permission.Adding them", missingPermissions.size()));
            missingPermissions
                    .stream()
                    .map(p -> new RoleRepresentation(p.name(), "", false))
                    .forEach(r -> realm.roles().create(r));
            log.info(String.format("Added %d roles", missingPermissions.size()));
        }
    }
}
