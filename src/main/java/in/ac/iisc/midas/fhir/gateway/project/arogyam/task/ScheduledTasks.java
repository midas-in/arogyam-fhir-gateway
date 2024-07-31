package in.ac.iisc.midas.fhir.gateway.project.arogyam.task;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@AllArgsConstructor
public class ScheduledTasks {
    private final GenerateImageLabellingTask generateImageLabellingTask;
    private final GeneratePatientCareTask generatePatientCareTask;
    private final TaskConfigProperties taskConfigProperties;

    @Scheduled(initialDelay = 1, fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void triggerEncounterChange() {
        if (taskConfigProperties.isScreeningEnabled()) {
            generatePatientCareTask.generate("main", taskConfigProperties.getScreeningPlanDefinition());
        }
    }

    @Scheduled(initialDelay = 2, fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void triggerMediaChange() {
        if (taskConfigProperties.isLabellingEnabled()) {
            generateImageLabellingTask.generate("main", taskConfigProperties.getLabellingPlanDefinition());
        }
    }
}
