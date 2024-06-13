package in.ac.iisc.midas.fhir.gateway.modules.inbound.authorization.enforcer;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilder;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.AuthenticationOutcome;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.UserSessionDetail;

public interface Enforcer {
    IAuthRuleBuilder enforce(AuthenticationOutcome outcome, UserSessionDetail.GrantedAuthority authority, IAuthRuleBuilder ruleBuilder);
}
