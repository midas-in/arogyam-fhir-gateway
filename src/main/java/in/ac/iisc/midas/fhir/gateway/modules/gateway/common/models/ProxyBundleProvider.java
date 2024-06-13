package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.models;

import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.valueset.BundleEntrySearchModeEnum;
import ca.uhn.fhir.model.valueset.BundleEntryTransactionMethodEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AllArgsConstructor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Bundle;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ProxyBundleProvider implements IBundleProvider {
    private final Bundle bundle;

    @Override
    public IPrimitiveType<Date> getPublished() {
        return bundle.getMeta().getLastUpdatedElement();
    }

    @Override
    public String getUuid() {
        return bundle.getId();
    }

    @Override
    public Integer preferredPageSize() {
        return null;
    }

    @Override
    public List<IBaseResource> getResources(int theFromIndex, int theToIndex) {
        return bundle.getEntry().stream().map(r -> {
            var resource = r.getResource();
            if (r.getRequest() != null && r.getRequest().getMethodElement() != null && r.getRequest().getMethodElement().getCode() != null) {
                resource.setUserData(ResourceMetadataKeyEnum.ENTRY_TRANSACTION_METHOD.name(), BundleEntryTransactionMethodEnum.valueOf(r.getRequest().getMethodElement().getCode()));
            }
            if (r.getSearch() != null && r.getSearch().getMode() != null) {
                resource.setUserData(ResourceMetadataKeyEnum.ENTRY_SEARCH_MODE.name(), BundleEntrySearchModeEnum.valueOf(r.getSearch().getMode().name()));
            }
            return (IBaseResource) resource;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer size() {
        var size = bundle.getTotal();
        if (size == 0 && bundle.hasEntry()) return null;
        return size;
    }
}
