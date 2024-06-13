package in.ac.iisc.midas.fhir.gateway.modules.cors;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnCorsPresent implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
        String property = conditionContext.getEnvironment().getProperty("hapi.fhir.cors.enabled");
        return Boolean.parseBoolean(property);
    }
}
