package in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp;

import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.ITargetProvider;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.location.LocationHierarchyResourceProvider;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.practionerdetail.PractitionerDetailsResourceProvider;

public class OpenSRPResourceProviderFactory extends ResourceProviderFactory {
	public OpenSRPResourceProviderFactory(ITargetProvider targetProvider) {
		var location = new LocationHierarchyResourceProvider(targetProvider);
		addSupplier(() -> location);
		addSupplier(() -> new PractitionerDetailsResourceProvider(targetProvider, location));
	}
}
