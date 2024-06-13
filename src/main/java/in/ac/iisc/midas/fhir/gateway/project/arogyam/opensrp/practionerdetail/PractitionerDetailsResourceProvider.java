package in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.practionerdetail;

import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.SystemRequestDetails;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.BundleProviders;
import ca.uhn.fhir.rest.server.IResourceProvider;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.ITargetProvider;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.location.LocationHierarchy;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.location.LocationHierarchyResourceProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
public class PractitionerDetailsResourceProvider implements IResourceProvider {
    private final ITargetProvider targetProvider;
    private final LocationHierarchyResourceProvider locationHierarchyResourceProvider;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return PractitionerDetail.class;
    }

    @SneakyThrows
    @Search(allowUnknownParams = true)
    public IBundleProvider search(HttpServletRequest theServletRequest,
                                  HttpServletResponse theServletResponse,
                                  RequestDetails theRequestDetails,
                                  @Description(shortDefinition = "Practitioner")
                                  @OptionalParam(name = "keycloak-uuid")
                                  TokenParam theIdentifier
    ) {
        if (theIdentifier == null) return BundleProviders.newEmptyList();
        return find(theRequestDetails, theIdentifier);
    }

    @NotNull
    public IBundleProvider find(RequestDetails theRequestDetails, TokenParam theIdentifier) {
        var practitioner = fetchPractitionerByUserId(theIdentifier.getValue(), theRequestDetails);
        if (practitioner == null) return BundleProviders.newEmptyList();
        var practitionerId = practitioner.getIdElement().getIdPart();

        var practitionerDetails = new PractitionerDetail();
        practitionerDetails.setId(practitionerId);

        var fhirPractitionerDetail = new FhirPractitionerDetails();
        fhirPractitionerDetail.setPractitioners(List.of(practitioner));
        practitionerDetails.setFhirPractitionerDetails(fhirPractitionerDetail);

        var careTeams = fetchPractitionerCareTeam(practitionerId, theRequestDetails);
        fhirPractitionerDetail.setCareTeams(careTeams);

        var practitionerRoles = fetchPractitionerRole(practitionerId, theRequestDetails);
        fhirPractitionerDetail.setPractitionerRoles(practitionerRoles);

        var careTeamManagingOrganizationIds = extractManagingOrganizationFromCareTeam(careTeams);
        var practitionerOrganizationIds = extractManagingOrganizationFromPractitionerRole(practitionerRoles);
        var organizationIds = Stream.concat(careTeamManagingOrganizationIds.stream(), practitionerOrganizationIds.stream())
                .distinct()
                .collect(Collectors.toList());
        var organizations = fetchOrganization(organizationIds, theRequestDetails);
        fhirPractitionerDetail.setOrganizations(organizations);

        var groups = fetchAssignedGroups(practitionerId, theRequestDetails);
        fhirPractitionerDetail.setGroups(groups);

        var orgAffiliations = fetchOrganizationAffiliation(organizationIds, theRequestDetails);
        fhirPractitionerDetail.setOrganizationAffiliations(orgAffiliations);

        var locationIds = extractLocationIdsFromOrganizationAffiliation(orgAffiliations);
        var locationHierarchy = fetchLocationHierarchy(locationIds, theRequestDetails);
        fhirPractitionerDetail.setLocationHierarchyList(locationHierarchy);

        var locations = fetchLocations(locationIds, theRequestDetails);
        fhirPractitionerDetail.setLocations(locations);

        return BundleProviders.newList(practitionerDetails);
    }

    private Practitioner fetchPractitionerByUserId(String userId, RequestDetails requestDetails) {
        return targetProvider.get(requestDetails).getFhirClient()
                .search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().identifier(userId))
                .count(1)
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .map(r -> (Practitioner) r)
                .findFirst()
                .orElse(null);
    }

    private List<CareTeam> fetchPractitionerCareTeam(String practitionerId, RequestDetails requestDetails) {
        return targetProvider.get(requestDetails).getFhirClient()
                .search()
                .forResource(CareTeam.class)
                .where(CareTeam.PARTICIPANT.hasId(practitionerId))
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .map(r -> (CareTeam) r)
                .collect(Collectors.toList());
    }

    // TODO
    private List<Location> fetchLocations(Set<String> locationIds, RequestDetails requestDetails) {
        if (locationIds.isEmpty()) return Collections.emptyList();

        return targetProvider.get(requestDetails).getFhirClient()
                .search()
                .forResource(Location.class)
                .where(Location.RES_ID.exactly().codes(locationIds))
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .map(r -> (Location) r)
                .collect(Collectors.toList());
    }

    private List<Organization> fetchOrganization(List<String> organizationIds, RequestDetails requestDetails) {
        if (organizationIds.isEmpty()) return Collections.emptyList();

        return targetProvider.get(requestDetails).getFhirClient()
                .search()
                .forResource(Organization.class)
                .where(Organization.RES_ID.exactly().codes(organizationIds))
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .map(r -> (Organization) r)
                .collect(Collectors.toList());
    }

    private List<PractitionerRole> fetchPractitionerRole(String practitionerId, RequestDetails requestDetails) {
        return targetProvider.get(requestDetails).getFhirClient()
                .search()
                .forResource(PractitionerRole.class)
                .where(PractitionerRole.PRACTITIONER.hasId(practitionerId))
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .map(r -> (PractitionerRole) r)
                .collect(Collectors.toList());
    }

    private List<Group> fetchAssignedGroups(String practitionerId, RequestDetails requestDetails) {
        return targetProvider.get(requestDetails).getFhirClient()
                .search()
                .forResource(Group.class)
                .where(Group.MEMBER.hasId(practitionerId))
                .and(Group.CODE.exactly().systemAndCode("http://snomed.info/sct", "405623001"))
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .map(r -> (Group) r)
                .collect(Collectors.toList());
    }

    private List<LocationHierarchy> fetchLocationHierarchy(Set<String> locationIds, RequestDetails requestDetails) {
        if (locationIds.isEmpty()) return Collections.emptyList();

        return locationIds.parallelStream().map(id -> {
            var request = new SystemRequestDetails(requestDetails);
            return locationHierarchyResourceProvider.getLocationHierarchy(request, id);
        }).collect(Collectors.toList());
    }

    private List<OrganizationAffiliation> fetchOrganizationAffiliation(List<String> organizationIds, RequestDetails requestDetails) {
        if (organizationIds.isEmpty()) return Collections.emptyList();

        return targetProvider.get(requestDetails).getFhirClient()
                .search()
                .forResource(OrganizationAffiliation.class)
                .where(OrganizationAffiliation.PRIMARY_ORGANIZATION.hasAnyOfIds(organizationIds))
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .map(r -> (OrganizationAffiliation) r)
                .collect(Collectors.toList());
    }

    private Set<String> extractManagingOrganizationFromCareTeam(List<CareTeam> careTeams) {
        return careTeams.stream()
                .filter(CareTeam::hasManagingOrganization)
                .flatMap(it -> it.getManagingOrganization().stream())
                .map(it -> it.getReferenceElement().getIdPart())
                .collect(Collectors.toSet());
    }

    private Set<String> extractManagingOrganizationFromPractitionerRole(List<PractitionerRole> careTeams) {
        return careTeams.stream()
                .filter(PractitionerRole::hasOrganization)
                .map(it -> it.getOrganization().getReferenceElement().getIdPart())
                .collect(Collectors.toSet());
    }

    private Set<String> extractLocationIdsFromOrganizationAffiliation(List<OrganizationAffiliation> organizationAffiliations) {
        if (organizationAffiliations.isEmpty()) return Collections.emptySet();

        return organizationAffiliations.stream()
                .map(it -> it.getLocationFirstRep().getReferenceElement().getIdPart())
                .collect(Collectors.toSet());
    }
}
