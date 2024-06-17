package in.ac.iisc.midas.fhir.gateway.project.arogyam;

import ca.uhn.fhir.rest.server.RestfulServer;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.ITargetProvider;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.SyncStrategySearchNarrowingInterceptor;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.context.SimpleWorkerContext;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.utils.StructureMapUtilities;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ArogyamConfig {
    @Bean
    ArogyamProjectServerConfigurer openSrpPostInitRegisterer(ITargetProvider targetProvider,
                                                             RestfulServer restfulServer,
                                                             SyncStrategySearchNarrowingInterceptor interceptor) {
        return new ArogyamProjectServerConfigurer(interceptor, restfulServer, targetProvider);
    }

    @Bean
    IWorkerContext workerContext() throws IOException {
        var worker = SimpleWorkerContext.fromNothing();
        worker.setExpansionProfile(new Parameters());
        return worker;
    }

    @Bean
    StructureMapUtilities structureMapUtilities(IWorkerContext workerContext) {
        return new StructureMapUtilities(workerContext);
    }
}
