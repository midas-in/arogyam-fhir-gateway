package in.ac.iisc.midas.fhir.gateway.project.arogyam.task;

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
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
@Component
public class GenerateImageLabellingTask {
    private final ITargetProvider targetProvider;
    private final StructureMapUtilities structureMapUtilities;
    private final IWorkerContext workerContext;

    public void generate(String targetId, String planDefinitionId) {
        var media = fetchMediaWithCriteria(targetId);
        while (media != null) {
            var planDefinition = fetchPlanDefinition(targetId, planDefinitionId);
            if (planDefinition == null) {
                throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Invalid request");
            }
            handleMediaOperation(targetId, media, planDefinition);

            var newMedia = fetchMediaWithCriteria(targetId);
            if (newMedia != null && newMedia.getIdPart().equals(media.getIdPart())) break;
            media = newMedia;
        }
    }

    private void handleMediaOperation(String targetId, Media media, PlanDefinition planDefinition) {
        var encounter = fetchEncounter(targetId, media.getEncounter().getReferenceElement().getIdPart());
        if (encounter == null) return;

        var carePlan = (CarePlan) applyPlanDefinition(
                targetId,
                encounter,
                planDefinition
        );
        var resources = carePlan.getContained()
                .stream()
                .filter(r -> !Objects.equals(r.fhirType(), ResourceType.RequestGroup.name()))
                .flatMap(t -> transform(targetId, encounter, media, t, planDefinition))
                .toList();

        var bundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        resources.forEach(r -> {
            var request = new Bundle.BundleEntryRequestComponent()
                    .setMethod(Bundle.HTTPVerb.PUT)
                    .setUrl(String.format("%s/%s", r.fhirType(), r.getIdElement().toUnqualifiedVersionless().getIdPart()));
            bundle.addEntry().setResource(r).setRequest(request);
        });

        var txResult = targetProvider.get(targetId).getFhirClient()
                .transaction()
                .withBundle(bundle)
                .execute();

        log.info("Media processed for image labelling : {}", encounter.getId());
    }

    @NotNull
    private Stream<Resource> transform(String targetId, Encounter encounter, Media media, Resource t, PlanDefinition planDefinition) {
        if (StringUtils.isBlank(extractInstantiateCanonicalUrl(t))) return Stream.of(t);

        var structureMap = findStructureMapForTransform(targetId, planDefinition, t);
        if (structureMap == null) return Stream.of(t);

        var source = new Parameters();
        source.addParameter().setName("context").setResource(media);
        source.addParameter().setName("encounter").setResource(encounter);
        source.addParameter().setName("resource").setResource(t);

        var target = new Bundle();
        target.setType(Bundle.BundleType.COLLECTION);
        try {
            structureMapUtilities.transform(workerContext, source, structureMap, target);
        } catch (FHIRException e) {
            log.error("Failed to transform", e);
            return Stream.empty();
        }

        return target.getEntry().stream().map(Bundle.BundleEntryComponent::getResource);
    }

    private Media fetchMediaWithCriteria(String targetId) {
        try {
            return targetProvider.get(targetId).getFhirClient()
                    .search()
                    .forResource(Media.class)
                    .where(Media.STATUS.exactly().codes(Media.MediaStatus.PREPARATION.toCode()))
                    // Oral mucous membrane structure
                    .and(Media.SITE.exactly().systemAndCode("http://www.snomed.org", "113277000"))
                    .count(1)
                    .returnBundle(Bundle.class)
                    .execute()
                    .getEntry().stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .map(r -> (Media) r)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Failed to fetch media", e);
            return null;
        }
    }

    private Encounter fetchEncounter(String targetId, String encounterId) {
        try {
            return targetProvider.get(targetId).getFhirClient()
                    .read()
                    .resource(Encounter.class)
                    .withId(encounterId)
                    .execute();
        } catch (Exception e) {
            log.error("Failed to fetch encounter", e);
            return null;
        }
    }

    private CarePlan applyPlanDefinition(String targetId, Encounter encounter, PlanDefinition planDefinition) {
        var parameters = new Parameters()
                .addParameter("subject", encounter.getSubject().getReference())
                .addParameter("encounter", encounter.getIdElement().toUnqualifiedVersionless().getValue());
        return targetProvider.get(targetId)
                .getFhirClient()
                .operation()
                .onInstanceVersion(planDefinition.getIdElement())
                .named("$apply")
                .withParameters(parameters)
                .returnResourceType(CarePlan.class)
                .execute();
    }

    private PlanDefinition fetchPlanDefinition(String targetId, String planDefinitionId) {
        try {
            return targetProvider.get(targetId).getFhirClient()
                    .read()
                    .resource(PlanDefinition.class)
                    .withId(planDefinitionId)
                    .execute();
        } catch (Exception e) {
            log.error("Failed to fetch plan definition for reader workflow", e);
            return null;
        }
    }

    private StructureMap findStructureMapForTransform(String targetId, PlanDefinition planDefinition, IBaseResource resource) {
        return planDefinition.getContained()
                .stream()
                .filter(r -> r.hasType(ResourceType.ActivityDefinition.name()))
                .map(r -> (ActivityDefinition) r)
                .filter(r -> StringUtils.isNotBlank(r.getUrl()))
                .filter(r -> Objects.equals(r.getUrl(), extractInstantiateCanonicalUrl(resource)))
                .findFirst()
                .map(ActivityDefinition::getTransform)
                .filter(StringUtils::isNotBlank)
                .map(sMapId -> fetchStructureMap(targetId, sMapId))
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

    private StructureMap fetchStructureMap(String targetId, String canonicalUrl) {
        var parts = canonicalUrl.split(ResourceType.StructureMap.name() + "/");
        if (parts.length == 1) return null;
        var id = parts[parts.length - 1];
        try {
            return targetProvider.get(targetId).getFhirClient()
                    .read()
                    .resource(StructureMap.class)
                    .withId(id)
                    .execute();
        } catch (Exception e) {
            log.error("Failed to fetch structure map for reader workflow", e);
            return null;
        }
    }
}
