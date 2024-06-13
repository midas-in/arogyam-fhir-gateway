package in.ac.iisc.midas.fhir.gateway.modules.inbound.authentication;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.UserSessionDetail;

import java.util.ArrayList;
import java.util.List;

public interface AuthenticationProtocol {

    boolean canAuthenticate(RequestDetails requestDetails);
    UserSessionDetail authenticate(RequestDetails requestDetails);
    String name();

    class Registry {
        private final List<AuthenticationProtocol> protocols = new ArrayList<>();

        public Registry(OIDCAuthenticationProtocol oAuth2) {
            protocols.add(oAuth2);
        }

        List<AuthenticationProtocol> getAll() {
            return protocols;
        }
    }
}
