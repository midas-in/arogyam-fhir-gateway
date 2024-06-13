package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.GatewayProperties;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class LocalTargetSelector implements ITargetProvider {

    private final FhirContext fhirContext;
    private final GatewayProperties properties;
    private final ConcurrentHashMap<String, Target> targets = new ConcurrentHashMap<>();

    @Override
    public BackendTarget get(RequestDetails requestDetails) {
        return get(requestDetails.getTenantId());
    }

    @Override
    public BackendTarget get(String idOrCode) {
        var tg = findTargetId(idOrCode);
        return new BackendTarget(tg.getBaseUrl(), fhirContext, tg.getHeadersToForward(), tg.getHeaderToReturn());
    }

    private Target findTargetId(String idOrCode) {
        if (StringUtils.isBlank(idOrCode)) {
            throw new ResourceNotFoundException("Unable to locate target");
        }
        if (!targets.containsKey(idOrCode)) {
            var target = properties
                    .getTargets()
                    .stream()
                    .filter(t -> t.getId().toLowerCase().equals(idOrCode))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Unable to locate resource"));
            targets.put(idOrCode, target);
        }
        return targets.get(idOrCode);
    }
}
