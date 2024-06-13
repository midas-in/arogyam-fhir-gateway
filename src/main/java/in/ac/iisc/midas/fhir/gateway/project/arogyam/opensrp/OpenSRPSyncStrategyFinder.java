package in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.SystemRequestDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.ITargetProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.springframework.cache.annotation.Cacheable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
public class OpenSRPSyncStrategyFinder {
    private final ITargetProvider targetProvider;
    private final ObjectMapper objectMapper;

    @Cacheable(cacheNames = "syncStrategy", key = "#appId")
    public String findStrategy(RequestDetails requestDetails, String appId) {
        // Pull the composition which holds all the configuration for the given app id
        var composition = targetProvider.get(requestDetails)
                .getFhirClient()
                .search()
                .forResource(Composition.class)
                .where(Composition.IDENTIFIER.exactly().codes(appId))
                .count(1)
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(r -> (Composition) r.getResource())
                .findFirst()
                .orElse(null);
        if (composition == null) return null;

        // Extract the app configuration binary reference
        var applicationBinaryId = composition.getSection()
                .stream()
                .filter(s -> s.getFocus() != null && s.getFocus().getIdentifier() != null && s.getFocus().getIdentifier().getValue() != null)
                .filter(s -> Objects.equals(s.getFocus().getIdentifier().getValue(), "application"))
                .filter(s -> s.getFocus().hasReference())
                .findFirst()
                .map(s -> s.getFocus().getReferenceElement().getIdPart())
                .orElse(null);
        if (applicationBinaryId == null) return null;

        // Request to load binary
        var binaryRequest = new SystemRequestDetails(requestDetails);
        Binary applicationBinary;
        try {
            applicationBinary = targetProvider.get(requestDetails)
                    .getFhirClient().read()
                    .resource(Binary.class)
                    .withId(applicationBinaryId)
                    .execute();
        } catch (Exception e) {
            log.error("Failed to load application binary");
            return null;
        }
        if (applicationBinary == null || !applicationBinary.hasData()) return null;

        // Parse the binary and extract the sync strategy
        try {
            var config = objectMapper.readValue(applicationBinary.getData(), new TypeReference<Map<String, Object>>() {
            });
            var syncStrategies = config.get("syncStrategy");
            if (!(syncStrategies instanceof List)) {
                log.error("Sync strategies should be a list");
                return null;
            }
            var strategy = ((List<?>) syncStrategies).get(0);
            if (!(strategy instanceof String)) {
                log.error("Sync strategy should be a string");
                return null;
            }
            return (String) strategy;
        } catch (IOException e) {
            log.error("Unable to parse json for application config");
            return null;
        }
    }
}
