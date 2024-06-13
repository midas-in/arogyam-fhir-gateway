package in.ac.iisc.midas.fhir.gateway.project.arogyam.task;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.SystemRequestDetails;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.ITargetProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.utils.StructureMapUtilities;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

@RestController
@AllArgsConstructor
@Slf4j
public class PerformPlanDefinitionApply {
    private final ITargetProvider targetProvider;
    private final FhirContext fhirContext;
    private final StructureMapUtilities structureMapUtilities;
    private final IWorkerContext workerContext;

    @GetMapping(value = "/api/process-message/encounter-created/{encId}/{planDefinitionId}", produces = "application/json")
    public String onEncounterCreate(@PathVariable String encId, @PathVariable String planDefinitionId) {
        var requestDetails = new SystemRequestDetails();
        var encounter = fetchEncounter(requestDetails, encId);
        if (encounter == null) throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Encounter not found");

        var planDefinition = fetchPlanDefinition(requestDetails, planDefinitionId);
        if (planDefinition == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "PlanDefinition not found");
        }

        return handleOperation(requestDetails,
                encounter.getSubject().getReference(),
                encounter.getIdElement().toUnqualifiedVersionless().getValue(),
                planDefinition
        );
    }

    @GetMapping(value = "/api/process-message/media-created/{mediaId}/{planDefinitionId}", produces = "application/json")
    public String trigger(@PathVariable String mediaId, @PathVariable String planDefinitionId) {
        var requestDetails = new SystemRequestDetails();

        var media = fetchMedia(requestDetails, mediaId);
        if (media == null) throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Media not found");

        var planDefinition = fetchPlanDefinition(requestDetails, planDefinitionId);
        if (planDefinition == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "PlanDefinition not found");
        }

        return handleOperation(requestDetails,
                media.getSubject().getReference(),
                media.getEncounter().getReference(),
                planDefinition
        );
    }

    public String handleOperation(RequestDetails requestDetails, String subject, String encounter, PlanDefinition planDefinition) {
        var carePlan = (CarePlan) applyPlanDefinition(
                requestDetails,
                subject,
                encounter,
                planDefinition
        );
        var resources = carePlan.getContained()
                .stream()
                .filter(r -> !Objects.equals(r.fhirType(), ResourceType.RequestGroup.name()))
                .flatMap(t -> transform(requestDetails, t, planDefinition))
                .toList();


        var bundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        resources.forEach(r -> {
            var request = new Bundle.BundleEntryRequestComponent()
                    .setMethod(Bundle.HTTPVerb.PUT)
                    .setUrl(r.getIdElement().toUnqualifiedVersionless().toString());
            bundle.addEntry().setResource(r).setRequest(request);
        });
        return fhirContext.newJsonParser().encodeToString(bundle);
    }

    @NotNull
    private Stream<Resource> transform(RequestDetails requestDetails, Resource t, PlanDefinition planDefinition) {
        if (StringUtils.isBlank(extractInstantiateCanonicalUrl(t))) return Stream.of(t);

        var structureMap = findStructureMapForTransform(requestDetails, planDefinition, t);
        if (structureMap == null) return Stream.of(t);

        var target = new Bundle();
        target.setType(Bundle.BundleType.COLLECTION);
        try {
            structureMapUtilities.transform(workerContext, t, structureMap, target);
        } catch (FHIRException e) {
            log.error("Failed to transform", e);
            return Stream.empty();
        }

        return target.getEntry().stream().map(Bundle.BundleEntryComponent::getResource);
    }

    private CarePlan applyPlanDefinition(RequestDetails requestDetails, String subject, String encounter, PlanDefinition planDefinition) {
        var parameters = new Parameters()
                .addParameter("subject", subject)
                .addParameter("encounter", encounter);
        return targetProvider.get(requestDetails)
                .getFhirClient()
                .operation()
                .onInstanceVersion(planDefinition.getIdElement())
                .named("$apply")
                .withParameters(parameters)
                .returnResourceType(CarePlan.class)
                .execute();
    }

    private PlanDefinition fetchPlanDefinition(RequestDetails requestDetails, String planDefinitionId) {
        try {
            return targetProvider.get(requestDetails).getFhirClient()
                    .read()
                    .resource(PlanDefinition.class)
                    .withId(planDefinitionId)
                    .execute();
        } catch (Exception e) {
            log.error("Failed to fetch plan definition for reader workflow", e);
            return null;
        }
    }

    private StructureMap findStructureMapForTransform(RequestDetails requestDetails, PlanDefinition planDefinition, IBaseResource resource) {
        return planDefinition.getContained()
                .stream()
                .filter(r -> r.hasType(ResourceType.ActivityDefinition.name()))
                .map(r -> (ActivityDefinition) r)
                .filter(r -> StringUtils.isNotBlank(r.getUrl()))
                .filter(r -> Objects.equals(r.getUrl(), extractInstantiateCanonicalUrl(resource)))
                .findFirst()
                .map(ActivityDefinition::getTransform)
                .filter(StringUtils::isNotBlank)
                .map(sMapId -> fetchStructureMap(requestDetails, sMapId))
                .orElse(null);
    }

    private static String extractInstantiateCanonicalUrl(IBaseResource resource) {
        if (resource instanceof Task r) {
            r.setBasedOn(Collections.emptyList());
            // TODO: Please find a way to remove a list using FHIR Mapping language or CQL
            r.setBasedOn(Collections.emptyList());
            return r.getInstantiatesCanonical();
        }
        if (resource instanceof ServiceRequest r) {
            // TODO: Please find a way to remove a list using FHIR Mapping language or CQL
            r.setBasedOn(Collections.emptyList());
            return r.getInstantiatesCanonical().isEmpty() ? null : r.getInstantiatesCanonical().get(0).getValue();
        }
        return null;
    }

    private StructureMap fetchStructureMap(RequestDetails requestDetails, String canonicalUrl) {
        var parts = canonicalUrl.split(ResourceType.StructureMap.name() + "/");
        if (parts.length == 1) return null;
        var id = parts[parts.length - 1];
        try {
            return targetProvider.get(requestDetails).getFhirClient()
                    .read()
                    .resource(StructureMap.class)
                    .withId(id)
                    .execute();
        } catch (Exception e) {
            log.error("Failed to fetch structure map for reader workflow", e);
            return null;
        }
    }

    private Media fetchMedia(RequestDetails requestDetails, String mediaId) {
        try {
            return targetProvider.get(requestDetails).getFhirClient()
                    .read()
                    .resource(Media.class)
                    .withId(mediaId)
                    .execute();
        } catch (Exception e) {
            log.error("Failed to fetch media", e);
            return null;
        }
    }

    private Encounter fetchEncounter(RequestDetails requestDetails, String encId) {
        try {
            return targetProvider.get(requestDetails).getFhirClient()
                    .read()
                    .resource(Encounter.class)
                    .withId(encId)
                    .execute();
        } catch (Exception e) {
            log.error("Failed to fetch encounter", e);
            return null;
        }
    }
}
