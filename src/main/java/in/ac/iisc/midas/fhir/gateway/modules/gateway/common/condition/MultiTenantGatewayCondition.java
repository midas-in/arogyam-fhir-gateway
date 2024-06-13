package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MultiTenantGatewayCondition implements Condition {
	@Override
	public boolean matches(ConditionContext theConditionContext, AnnotatedTypeMetadata metadata) {
		String property = theConditionContext.getEnvironment().getProperty("hapi.fhir.gateway.multi-tenant");
		return Boolean.parseBoolean(property);
	}
}
