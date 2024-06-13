package in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationConstants;
import ca.uhn.fhir.rest.server.interceptor.auth.SearchNarrowingInterceptor;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.AuthenticationOutcome;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.practionerdetail.PractitionerDetail;
import in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp.practionerdetail.PractitionerDetailsResourceProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Narrow the search so that only relevant data is shown to the end consumer.
 * There are 4 strategy using which narrowing is done:
 * - Location - All the location where the current logged in practitioner is a member
 * - CareTeam - All the care team where the practitioner is a member
 * - Organization(Team) - All the team where the practitioner is a member
 * - RelatedLocation -> No sure based on open srp doc. Currently implemented it same as Location
 */
@Interceptor(order = AuthorizationConstants.ORDER_AUTH_INTERCEPTOR + 1)
@AllArgsConstructor
@Slf4j
public class SyncStrategySearchNarrowingInterceptor extends SearchNarrowingInterceptor {
    private final PractitionerDetailsResourceProvider practitionerDetailsResourceProvider;
    private final List<OpenSrpProperties.SearchNarrowEntry> whitelisted;
    private final OpenSRPSyncStrategyFinder syncStrategyFinder;
    private static final String RELATED_LOCATION_TAG = "https://smartregister.org/related-entity-location-tag-id";
    private static final String LOCATION_TAG = "https://smartregister.org/location-tag-id";
    private static final String ORGANIZATION_TAG = "https://smartregister.org/organisation-tag-id";
    private static final String CARE_TEAM_TAG = "https://smartregister.org/care-team-tag-id";

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void hookIncomingRequestPostProcessed(
            RequestDetails theRequestDetails,
            HttpServletRequest theRequest,
            HttpServletResponse theResponse)
            throws AuthenticationException {
        if (!shouldNarrowSearch(theRequestDetails)) return;
// TODO: Remove this before deployment
//        var appId = extractAppId(theRequestDetails);
//        if (appId == null) throwForbidden();

        // Only app where are su
        var strategy = syncStrategyFinder.findStrategy(theRequestDetails, "app");
        addParameters(theRequestDetails, strategy);
    }

    private boolean shouldNarrowSearch(RequestDetails theRequestDetails) {
        return isResourceSearchRequest(theRequestDetails)
                && doesClientNeedsNarrowing(theRequestDetails)
                && !isWhitelisted(theRequestDetails);
    }

    private static boolean isResourceSearchRequest(RequestDetails theRequestDetails) {
        return theRequestDetails.getRestOperationType() == RestOperationTypeEnum.SEARCH_TYPE;
    }

    private boolean isWhitelisted(RequestDetails theRequestDetails) {
        var httpRequest = (ServletRequestDetails) theRequestDetails;
        return whitelisted
                .stream()
                .anyMatch(entry -> {
                    var methodMatch = Objects.equals(httpRequest.getServletRequest().getMethod(), entry.getMethod().name());
                    if (!methodMatch) return false;

                    var pathMatched = Objects.equals(theRequestDetails.getRequestPath(), entry.getPath());
                    if (!pathMatched) return false;

                    return entry.getQueryParams().entrySet()
                            .stream()
                            .allMatch(item -> {
                                var requestQueryParam = theRequestDetails.getParameters().get(item.getKey());
                                if (requestQueryParam == null || requestQueryParam.length == 0) return true;
                                var requestValue = requestQueryParam[0];
                                if (Objects.equals(item.getValue(), "*")) return true;
                                return item.getValue().equals(requestValue);
                            });
                });
    }

    private boolean doesClientNeedsNarrowing(RequestDetails theRequestDetails) {
        // TODO
        return false;
    }

    private void addParameters(RequestDetails theRequestDetails, String strategy) {
        var parameters = buildParameters(theRequestDetails, strategy);
        if (parameters.isEmpty()) throwForbidden();
        theRequestDetails.addParameter("_tag", parameters.toArray(new String[0]));
    }

    private static void throwForbidden() {
        var outcome = new OperationOutcome();
        outcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.FATAL)
                .setCode(OperationOutcome.IssueType.FORBIDDEN);
        throw new ForbiddenOperationException("You are not authorized to perform this action", outcome);
    }

    private List<String> buildParameters(RequestDetails theRequestDetails, String strategy) {
        var practitioner = fetchPractitioner(theRequestDetails);
        if (practitioner == null) return Collections.emptyList();

        switch (strategy) {
            case "Location":
                return buildLocationSyncParameters(theRequestDetails, practitioner, LOCATION_TAG);
            case "Organization":
                return buildOrganizationSyncParameters(theRequestDetails, practitioner);
            case "CareTeam":
                return buildCareTeamSyncParameters(theRequestDetails, practitioner);
            // assigned locations
            case "RelatedEntityLocation":
            case "Practitioner":
                return buildLocationSyncParameters(theRequestDetails, practitioner, RELATED_LOCATION_TAG);
            default:
                return Collections.emptyList();
        }
    }

    private List<String> buildLocationSyncParameters(RequestDetails theRequestDetails, PractitionerDetail practitioner, String tag) {
        var parentChildrenList =
                practitioner.getFhirPractitionerDetails().getLocationHierarchyList().stream()
                        .flatMap(
                                locationHierarchy ->
                                        locationHierarchy
                                                .getLocationHierarchyTree()
                                                .getLocationsHierarchy()
                                                .getParentChildren()
                                                .stream())
                        .collect(Collectors.toList());

        return parentChildrenList.stream()
                .flatMap(parentChildren -> parentChildren.getChildIdentifiers().stream())
                .map(StringType::toString)
                .filter(it -> it.contains("/"))
                .map(it -> it.split("/", 2)[1])
                .filter(StringUtils::isNotBlank)
                .map(it -> String.format("%s|%s", tag, it))
                .collect(Collectors.toList());
    }

    @Nullable
    private PractitionerDetail fetchPractitioner(RequestDetails theRequestDetails) {
        var outcome = AuthenticationOutcome.getOutcome(theRequestDetails);
        if (outcome == null || outcome.getUserSessionDetail() == null) return null;

        var userId = outcome.getUserSessionDetail().getId();
        if (userId == null) return null;
        return (PractitionerDetail) practitionerDetailsResourceProvider.find(theRequestDetails, new TokenParam(userId))
                .getAllResources()
                .stream()
                .findFirst().orElse(null);
    }

    private List<String> buildOrganizationSyncParameters(RequestDetails theRequestDetails, PractitionerDetail practitioner) {
        return practitioner.getFhirPractitionerDetails().getOrganizations()
                .stream()
                .map(Resource::getIdPart)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .map(it -> String.format("%s|%s", ORGANIZATION_TAG, it))
                .collect(Collectors.toList());
    }

    private List<String> buildCareTeamSyncParameters(RequestDetails theRequestDetails, PractitionerDetail practitioner) {
        return practitioner.getFhirPractitionerDetails().getCareTeams()
                .stream()
                .map(Resource::getIdPart)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .map(it -> String.format("%s|%s", CARE_TEAM_TAG, it))
                .collect(Collectors.toList());
    }

    private String extractAppId(RequestDetails requestDetails) {
        var outcome = AuthenticationOutcome.getOutcome(requestDetails);
        if (outcome == null || outcome.getUserSessionDetail() == null) return null;

        var jwt = outcome.getUserSessionDetail().getJwt();
        if (jwt == null) return null;
        return jwt.getClaimAsString("fhir_core_app_id");
    }
}