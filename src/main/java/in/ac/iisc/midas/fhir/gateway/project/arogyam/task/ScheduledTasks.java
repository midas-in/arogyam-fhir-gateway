package in.ac.iisc.midas.fhir.gateway.project.arogyam.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@AllArgsConstructor
@Slf4j
public class ScheduledTasks {
    private final GenerateImageLabellingTask generateImageLabellingTask;
    private final GeneratePatientCareTask generatePatientCareTask;
    private final TaskConfigProperties taskConfigProperties;

    @Scheduled(initialDelay = 1, fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void generateScreenPatientTask() {
        log.info("Generate Screening Patient Task Cron triggered");
        if (taskConfigProperties.isScreeningEnabled()) {
            log.info("Generate Screening Patient Task performing");
            generatePatientCareTask.generate("main", taskConfigProperties.getScreeningPlanDefinition());
        }
    }

    @Scheduled(initialDelay = 2, fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void labelImageTask() {
        log.info("Generate Label Image Task Cron triggered");
        if (taskConfigProperties.isLabellingEnabled()) {
            log.info("Generate Label Image Task performing");
            generateImageLabellingTask.generate("main", taskConfigProperties.getLabellingPlanDefinition());
        }
    }
}
