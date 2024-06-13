package in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.location;

import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.BundleProviders;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.ITargetProvider;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class LocationHierarchyResourceProvider implements IResourceProvider {
    private final ITargetProvider targetProvider;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return LocationHierarchy.class;
    }

    @SneakyThrows
    @Search(allowUnknownParams = true)
    public IBundleProvider search(HttpServletRequest theServletRequest,
                                  HttpServletResponse theServletResponse,
                                  RequestDetails theRequestDetails,
                                  @Description(shortDefinition = "Location Id")
                                  @OptionalParam(name = "identifier")
                                  TokenParam theIdentifier
    ) {
        var hierarchy = theIdentifier != null && theIdentifier.getValue() != null ?
                getLocationHierarchy(theRequestDetails, theIdentifier.getValue())
                : null;
        if (hierarchy == null) return BundleProviders.newEmptyList();
        return BundleProviders.newList(hierarchy);
    }

    public LocationHierarchy getLocationHierarchy(RequestDetails requestDetails, String locationId) {
        Location location = getLocationById(requestDetails, locationId);
        LocationHierarchyTree locationHierarchyTree = new LocationHierarchyTree();
        LocationHierarchy locationHierarchy = new LocationHierarchy();
        if (location != null) {
            var locations = getLocationHierarchyInternal(requestDetails, locationId, location);
            // It is needed since the parentId(defined in the partOf) doesn't have the url prefix for the id
            // where are the location id has the url. In order to create parent child relationship. Had to remove
            // the location id
            locations = locations.stream()
                    .peek(l -> l.setId(String.format("%s/%s", ResourceType.Location.name(), l.getIdPart())))
                    .collect(Collectors.toList());
            locationHierarchyTree.buildTreeFromList(locations);
            var locationIdString = new StringType().setId(locationId).getIdElement();
            locationHierarchy.setLocationId(locationIdString);
            locationHierarchy.setId(new IdType(ResourceType.Location.name(), locationId));
            locationHierarchy.setLocationHierarchyTree(locationHierarchyTree);
            return locationHierarchy;
        }
        return null;
    }

    private List<Location> getLocationHierarchyInternal(RequestDetails requestDetails, String locationId, Location parentLocation) {
        return descendants(requestDetails, locationId, parentLocation);
    }

    public List<Location> descendants(RequestDetails requestDetails, String locationId, Location parentLocation) {

        var childLocationBundle = fetchChildLocation(locationId, requestDetails);

        List<Location> allLocations = new ArrayList<>();
        if (parentLocation != null) allLocations.add(parentLocation);


        if (childLocationBundle != null) {
            for (var childLocationEntity : childLocationBundle) {
                allLocations.add(childLocationEntity);
                allLocations.addAll(descendants(requestDetails, childLocationEntity.getIdElement().getIdPart(), null));
            }
        }

        return allLocations;
    }

    @Nullable
    private Location getLocationById(RequestDetails requestDetails, String locationId) {
        try {
            return targetProvider.get(requestDetails).getFhirClient()
                    .read()
                    .resource(Location.class)
                    .withId(locationId)
                    .execute();
        } catch (ResourceNotFoundException e) {
            return null;
        } catch (Exception e) {
            log.error("Failed to get resource", e);
            return null;
        }
    }

    private List<Location> fetchChildLocation(String locationId, RequestDetails requestDetails) {
        return targetProvider.get(requestDetails).getFhirClient()
                .search()
                .forResource(Location.class)
                .where(Location.PARTOF.hasId(locationId))
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .map(r -> (Location) r)
                .collect(Collectors.toList());
    }
}
