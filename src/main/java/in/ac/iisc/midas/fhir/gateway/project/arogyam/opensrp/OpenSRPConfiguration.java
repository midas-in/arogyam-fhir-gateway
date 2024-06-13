package in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.ITargetProvider;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.location.LocationHierarchyResourceProvider;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.practionerdetail.PractitionerDetailsResourceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSRPConfiguration {

    @Bean
    OpenSrpProperties openSrpProperties() {
        return new OpenSrpProperties();
    }

    @Bean
    LocationHierarchyResourceProvider locationResourceProvider(ITargetProvider targetProvider) {
        return new LocationHierarchyResourceProvider(targetProvider);
    }

    @Bean
    PractitionerDetailsResourceProvider practitionerDetailsResourceProvider(ITargetProvider targetProvider,
                                                                            LocationHierarchyResourceProvider locationHierarchyResourceProvider) {
        return new PractitionerDetailsResourceProvider(targetProvider, locationHierarchyResourceProvider);
    }

    @Bean
    SyncStrategySearchNarrowingInterceptor syncStrategySearchNarrowingInterceptor(PractitionerDetailsResourceProvider resourceProvider,
                                                                                  OpenSrpProperties openSrpProperties,
                                                                                  OpenSRPSyncStrategyFinder syncStrategyFinder) {
        return new SyncStrategySearchNarrowingInterceptor(resourceProvider, openSrpProperties.getWhitelist(), syncStrategyFinder);
    }

    @Bean
    OpenSRPSyncStrategyFinder syncStrategyFinder(ITargetProvider targetProvider,
                                                 ObjectMapper objectMapper) {
        return new OpenSRPSyncStrategyFinder(targetProvider, objectMapper);
    }
}
