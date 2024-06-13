package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.provider;

import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.TargetRequestForwarder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.*;

import java.util.Date;

@RequiredArgsConstructor
@Slf4j
public class BackendResourceProvider<T extends IBaseResource> implements IResourceProvider {
    private final TargetRequestForwarder requestForwarder;
    private final Class<T> klass;

    public Class<? extends IBaseResource> getResourceType() {
        return klass;
    }

    @SneakyThrows
    @Search(allowUnknownParams = true)
    public IBundleProvider search(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseBundleProviderResponse(theRequestDetails);
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam T theResource, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseMethodOutcomeResponse(theRequestDetails);
    }

    @Read(version = true)
    public IBaseResource read(HttpServletRequest theRequest, @IdParam IIdType theId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam T theResource, @IdParam IIdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseMethodOutcomeResponse(theRequestDetails);
    }

    @Patch
    public MethodOutcome patch(HttpServletRequest theRequest, @IdParam IIdType theId, @ConditionalUrlParam String theConditionalUrl, RequestDetails theRequestDetails, @ResourceParam String theBody, PatchTypeEnum thePatchType, @ResourceParam IBaseParameters theRequestBody) {
        return requestForwarder.tryParseMethodOutcomeResponse(theRequestDetails);
    }

    @Delete
    public MethodOutcome delete(HttpServletRequest theRequest, @IdParam IIdType theResource, @ConditionalUrlParam(supportsMultiple = true) String theConditional, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseMethodOutcomeResponse(theRequestDetails);
    }

    @History
    public IBundleProvider getHistoryForResourceInstance(HttpServletRequest theRequest, @Offset Integer theOffset, @IdParam IIdType theId, @Since Date theSince, @At DateRangeParam theAt, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseBundleProviderResponse(theRequestDetails);
    }


    @History
    public IBundleProvider getHistoryForResourceType(HttpServletRequest theRequest, @Offset Integer theOffset, @Since Date theSince, @At DateRangeParam theAt, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseBundleProviderResponse(theRequestDetails);
    }

    @Validate
    public MethodOutcome validate(@ResourceParam T theResource, @ResourceParam String theRawResource, @ResourceParam EncodingEnum theEncoding, @Validate.Mode ValidationModeEnum theMode, @Validate.Profile String theProfile, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseMethodOutcomeResponse(theRequestDetails);
    }

    @Validate
    public MethodOutcome validate(@ResourceParam T theResource, @IdParam IIdType theId, @ResourceParam String theRawResource, @ResourceParam EncodingEnum theEncoding, @Validate.Mode ValidationModeEnum theMode, @Validate.Profile String theProfile, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseMethodOutcomeResponse(theRequestDetails);
    }


    @Operation(name = "$expunge", idempotent = false, returnParameters = {@OperationParam(name = "count", typeName = "integer")})
    public IBaseParameters expunge(@IdParam IIdType theIdParam, @OperationParam(name = "limit", typeName = "integer") IPrimitiveType<Integer> theLimit, @OperationParam(name = "expungeDeletedResources", typeName = "boolean") IPrimitiveType<Boolean> theExpungeDeletedResources, @OperationParam(name = "expungePreviousVersions", typeName = "boolean") IPrimitiveType<Boolean> theExpungeOldVersions, RequestDetails theRequest) {
        return (IBaseParameters) requestForwarder.tryParseResourceResponse(theRequest);
    }

    @Operation(name = "$expunge", idempotent = false, returnParameters = {@OperationParam(name = "count", typeName = "integer")})
    public IBaseParameters expunge(@OperationParam(name = "limit", typeName = "integer") IPrimitiveType<Integer> theLimit, @OperationParam(name = "expungeDeletedResources", typeName = "boolean") IPrimitiveType<Boolean> theExpungeDeletedResources, @OperationParam(name = "expungePreviousVersions", typeName = "boolean") IPrimitiveType<Boolean> theExpungeOldVersions, RequestDetails theRequest) {
        return (IBaseParameters) requestForwarder.tryParseResourceResponse(theRequest);
    }

    @Description("Request a global list of tags, profiles, and security labels")
    @Operation(name = "$meta", idempotent = true, returnParameters = {@OperationParam(name = "return", typeName = "Meta")})
    public IBaseParameters meta(RequestDetails theRequestDetails) {
        return (IBaseParameters) requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    @Description("Request a list of tags, profiles, and security labels for a specfic resource instance")
    @Operation(name = "$meta", idempotent = true, returnParameters = {@OperationParam(name = "return", typeName = "Meta")})
    public IBaseParameters meta(@IdParam IIdType theId, RequestDetails theRequestDetails) {
        return (IBaseParameters) requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    @Description("Add tags, profiles, and/or security labels to a resource")
    @Operation(name = "$meta-add", idempotent = false, returnParameters = {@OperationParam(name = "return", typeName = "Meta")})
    public IBaseParameters metaAdd(@IdParam IIdType theId, @OperationParam(name = "meta", typeName = "Meta") IBaseMetaType theMeta, RequestDetails theRequestDetails) {
        return (IBaseParameters) requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    @Description("Delete tags, profiles, and/or security labels from a resource")
    @Operation(name = "$meta-delete", idempotent = false, returnParameters = {@OperationParam(name = "return", typeName = "Meta")})
    public IBaseParameters metaDelete(@IdParam IIdType theId, @OperationParam(name = "meta", typeName = "Meta") IBaseMetaType theMeta, RequestDetails theRequestDetails) {
        return (IBaseParameters) requestForwarder.tryParseResourceResponse(theRequestDetails);
    }
}
