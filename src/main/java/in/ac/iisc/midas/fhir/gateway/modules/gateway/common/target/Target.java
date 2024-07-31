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
    private Set<String> headersToForward = Set.of(
            "content-type",
            "cache-control",
            "accept-encoding",
            "last-modified",
            "etag",
            "prefer",
            "fhirVersion",
            "if-none-exist",
            "if-match",
            "if-none-match",
            "if-modified-since",
            "if-unmodified-since",
            "if-range",
            "x-request-id",
            "x-correlation-id",
            "x-forwarded-for",
            "x-forwarded-host"
    );
    private Set<String> headerToReturn = Set.of(
            "last-modified",
            "date",
            "expires",
            "etag",
            "x-progress",
            "x-request-id",
            "x-correlation-id"
    );
}