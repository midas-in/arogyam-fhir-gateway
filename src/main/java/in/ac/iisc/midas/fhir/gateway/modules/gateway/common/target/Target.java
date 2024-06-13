package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;

@Getter
@Setter
public class Target {
    private String id;
    private String baseUrl;
    private String name;
    private Set<String> headersToForward = Collections.emptySet();
    private Set<String> headerToReturn = Collections.emptySet();
}