package in.ac.iisc.midas.fhir.gateway.project.arogyam.task;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incoming-webhook")
@AllArgsConstructor
public class IncomingWebhookRestController {
    private final GenerateImageLabellingTask generateImageLabellingTask;
    private final GeneratePatientCareTask generatePatientCareTask;

    @PostMapping(value = "/{targetId}/encounter-change/{planDefinitionId}")
    public void triggerEncounterChange(@PathVariable String targetId, @PathVariable String planDefinitionId) {
        generatePatientCareTask.generate(targetId, planDefinitionId);

    }

    @PostMapping(value = "/{targetId}/media-change/{planDefinitionId}")
    public void triggerMediaChange(@PathVariable String targetId, @PathVariable String planDefinitionId) {
        generateImageLabellingTask.generate(targetId, planDefinitionId);
    }
}
