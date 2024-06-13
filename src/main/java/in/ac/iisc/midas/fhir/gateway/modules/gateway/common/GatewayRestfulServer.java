package in.ac.iisc.midas.fhir.gateway.modules.gateway.common;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.IInterceptorService;
import ca.uhn.fhir.rest.server.RestfulServer;

public class GatewayRestfulServer extends RestfulServer {
    public GatewayRestfulServer() {
    }

    public GatewayRestfulServer(FhirContext theCtx) {
        super(theCtx);
    }

    public GatewayRestfulServer(FhirContext theCtx, IInterceptorService theInterceptorService) {
        super(theCtx, theInterceptorService);
    }

    @Override
    protected String createPoweredByHeaderProductName() {
        return "Midas";
    }

    @Override
    protected String createPoweredByHeaderProductVersion() {
        return "1.0.0";
    }
}
