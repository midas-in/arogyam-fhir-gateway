package in.ac.iisc.midas.fhir.gateway.project.arogyam.site;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class SiteResponse {
    private String type = "sitesConfig";
    private List<Environment> environments = Collections.emptyList();
    private List<Site> sites = Collections.emptyList();

    public enum EnvironmentType {
        staging
    }

    @Getter
    @Setter
    public static class Environment {
        private EnvironmentType type;
        private Site site;
    }

    @Getter
    @Setter
    public static class Site {
        private String code;
        private String name;
        private String fhirBaseUrl;
        private String authBaseUrl;
        private String authClientId;
        private String authScope;
        private String mapSdkToken;
        private String openSrpAppId;
        private String sentryDsn;
    }
}
