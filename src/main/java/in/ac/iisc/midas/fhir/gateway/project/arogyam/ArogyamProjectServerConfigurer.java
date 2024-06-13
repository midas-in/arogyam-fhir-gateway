package in.ac.iisc.midas.fhir.gateway.project.arogyam;

import ca.uhn.fhir.rest.server.RestfulServer;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.ITargetProvider;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.OpenSRPResourceProviderFactory;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.SyncStrategySearchNarrowingInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ArogyamProjectServerConfigurer {
	private final SyncStrategySearchNarrowingInterceptor interceptor;
	private final RestfulServer restfulServer;
	private final ITargetProvider targetProvider;


	@PostConstruct()
	void attach() {
		var factory = new OpenSRPResourceProviderFactory(targetProvider);
		restfulServer.registerProviders(factory.createProviders());
		restfulServer.registerInterceptor(interceptor);
	}
}
