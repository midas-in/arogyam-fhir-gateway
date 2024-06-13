package in.ac.iisc.midas.fhir.gateway.modules.inbound.authentication;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.AuthenticationOutcome;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.UserSessionDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.AuthenticationOutcome.setOutcome;


@AllArgsConstructor
@Slf4j
public class AuthenticationService {
	private final AuthenticationProtocol.Registry registry;
	@Getter
	private final Boolean shouldSwitchToAnonymousUserOnAuthError;

	public void perform(RequestDetails requestDetails) {
		var authFailed = false;
		for (AuthenticationProtocol protocol : registry.getAll()) {
			UserSessionDetail userSessionDetail = null;
			try {
				if (protocol.canAuthenticate(requestDetails)) {
					userSessionDetail = protocol.authenticate(requestDetails);
				}
			} catch (Exception e) {
				authFailed = true;
				log.info("Failed to authenticate", e);
			}

			if (userSessionDetail != null) {
				onAuthenticated(requestDetails, userSessionDetail);
				return;
			}
		}
		onNotAuthenticated(requestDetails, authFailed);
	}

	private void onAuthenticated(RequestDetails requestDetails, UserSessionDetail userSessionDetail) {
		var outcome = new AuthenticationOutcome(userSessionDetail, true, false);
		setOutcome(requestDetails, outcome);
	}

	private void onNotAuthenticated(RequestDetails requestDetails, boolean authFailed) {
		var outcome = new AuthenticationOutcome(null, false, authFailed);
		setOutcome(requestDetails, outcome);
	}
}
