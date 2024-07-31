package in.ac.iisc.midas.fhir.gateway.project.arogyam.task;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("project.arogyam.task")
@Component
@Setter
@Getter
public class TaskConfigProperties {
    private String screeningPlanDefinition;
    private String labellingPlanDefinition;
    private boolean isScreeningEnabled;
    private boolean isLabellingEnabled;
}
