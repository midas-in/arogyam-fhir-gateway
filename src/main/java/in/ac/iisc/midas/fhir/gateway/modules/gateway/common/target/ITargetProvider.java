package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target;

import ca.uhn.fhir.rest.api.server.RequestDetails;

public interface ITargetProvider {

    BackendTarget get(RequestDetails requestDetails);

    BackendTarget get(String id);
}
