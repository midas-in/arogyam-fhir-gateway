package in.ac.iisc.midas.fhir.gateway.modules.inbound.authorization;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class InboundSecurityAuthorizationCondition implements Condition {
	@Override
	public boolean matches(ConditionContext theConditionContext, AnnotatedTypeMetadata metadata) {
		String property = theConditionContext.getEnvironment().getProperty("hapi.fhir.security.inbound.authorization.enabled");
		return Boolean.parseBoolean(property);
	}
}
