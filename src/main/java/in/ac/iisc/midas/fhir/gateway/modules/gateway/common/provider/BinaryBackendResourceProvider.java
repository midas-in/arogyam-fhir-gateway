package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.provider;

import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.TargetRequestForwarder;
import org.hl7.fhir.r4.model.Binary;

public class BinaryBackendResourceProvider extends BackendResourceProvider<Binary>{
    public BinaryBackendResourceProvider(TargetRequestForwarder requestForwarder, Class<Binary> klass) {
        super(requestForwarder, klass);
    }
}
