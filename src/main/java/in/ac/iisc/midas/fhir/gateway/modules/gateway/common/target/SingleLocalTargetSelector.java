package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.GatewayProperties;
import lombok.AllArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class SingleLocalTargetSelector implements ITargetProvider {

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
        return properties
                .getTargets()
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Unable to locate target"));
    }
}
