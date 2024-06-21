package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.TargetRequestForwarder;
import lombok.AllArgsConstructor;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.QuestionnaireResponse;

@AllArgsConstructor
public class BackendSystemProvider {
    private final TargetRequestForwarder requestForwarder;

    @Operation(name = "$trigger-subscription")
    public IBaseResource triggerSubscription(ca.uhn.fhir.rest.api.server.RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    @Transaction
    public IBaseBundle transaction(RequestDetails theRequestDetails, @TransactionParam IBaseBundle theResources) {
        return requestForwarder.tryParseBundleResponse(theRequestDetails);
    }

    /**
     * $binary-access-read
     */
    @Operation(name = "$binary-access-read", global = true, manualResponse = true, idempotent = true)
    public void binaryAccessRead(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        requestForwarder.tryBinaryResponse(theRequestDetails);
    }

    /**
     * $binary-access-write
     */
    @Operation(name = "$binary-access-write", global = true, manualRequest = true, idempotent = false)
    public IBaseResource binaryAccessWrite(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $cql
     */
    @Operation(name = "$cql", global = true, manualRequest = true, idempotent = true)
    public IBaseResource cql(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $binary-access-write
     */
    @Operation(name = "$evaluate-measure", global = true, manualRequest = true, type = Measure.class, idempotent = false)
    public IBaseResource evaluateMeasure(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $binary-access-write
     */
    @Operation(name = "$evaluate-measure", global = true, manualRequest = true, type = Measure.class, idempotent = false)
    public IBaseResource evaluateMeasure(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $apply
     */
    @Operation(name = "$apply", global = true, manualRequest = true, idempotent = true)
    public IBaseResource apply(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $apply
     */
    @Operation(name = "$apply", global = true, manualRequest = true, idempotent = true)
    public IBaseResource apply(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $submit-data
     */
    @Operation(name = "$submit-data", global = true, manualRequest = true, idempotent = true)
    public IBaseResource submitData(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseBundleResponse(theRequestDetails);
    }

    /**
     * $submit-data
     */
    @Operation(name = "$submit-data", global = true, manualRequest = true, idempotent = true)
    public IBaseResource submitData(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $collect-data
     */
    @Operation(name = "$collect-data", global = true, manualRequest = true, idempotent = true)
    public IBaseResource collectData(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseBundleResponse(theRequestDetails);
    }

    /**
     * $collect-data
     */
    @Operation(name = "$collect-data", global = true, manualRequest = true, idempotent = true)
    public IBaseResource collectData(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseBundleResponse(theRequestDetails);
    }

    /**
     * $everything
     */
    @Operation(name = "$everything", global = true, manualRequest = true, idempotent = true)
    public IBaseResource everything(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseBundleResponse(theRequestDetails);
    }

    /**
     * $convert
     */
    @Operation(name = "$convert", global = true, manualRequest = true, idempotent = true)
    public IBaseResource convert(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $graphql
     */
    @Operation(name = "$graphql", global = true, manualRequest = true, idempotent = true)
    public IBaseResource graphql(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $graphql
     */
    @Operation(name = "$graphql", global = true, manualRequest = true, idempotent = true)
    public IBaseResource graphql(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $stats
     */
    @Operation(name = "$stats", global = true, manualRequest = true, idempotent = true)
    public IBaseResource stats(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $lastn
     */
    @Operation(name = "$lastn", global = true, manualRequest = true, idempotent = true)
    public IBaseResource lastn(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseBundleResponse(theRequestDetails);
    }

    /**
     * $match
     */
    @Operation(name = "$match", global = true, manualRequest = true, idempotent = true)
    public IBaseResource match(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseBundleResponse(theRequestDetails);
    }

    /**
     * $questionnaire
     */
    @Operation(name = "$questionnaire", global = true, manualRequest = true, idempotent = true)
    public IBaseResource questionnaire(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $questionnaire
     */
    @Operation(name = "$questionnaire", global = true, manualRequest = true, idempotent = true)
    public IBaseResource questionnaire(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $snapshot
     */
    @Operation(name = "$snapshot", global = true, manualRequest = true, idempotent = true)
    public IBaseResource snapshot(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $snapshot
     */
    @Operation(name = "$snapshot", global = true, manualRequest = true, idempotent = true)
    public IBaseResource snapshot(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $transform
     */
    @Operation(name = "$transform", global = true, manualRequest = true, idempotent = true)
    public IBaseResource transform(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $transform
     */
    @Operation(name = "$transform", global = true, manualRequest = true, idempotent = true)
    public IBaseResource transform(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $extract
     */
    @Operation(name = "$extract", global = true, manualRequest = true, idempotent = true, type = QuestionnaireResponse.class)
    public IBaseResource $extract(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $extract
     */
    @Operation(name = "$extract", global = true, manualRequest = true, idempotent = true, type = QuestionnaireResponse.class)
    public IBaseResource extract(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $validate-code
     */
    @Operation(name = "$validate-code", global = true, manualRequest = true, idempotent = true)
    public IBaseResource validateCode(RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }

    /**
     * $validate-code
     */
    @Operation(name = "$validate-code", global = true, manualRequest = true, idempotent = true)
    public IBaseResource validateCode(@IdParam IIdType theResourceId, RequestDetails theRequestDetails) {
        return requestForwarder.tryParseResourceResponse(theRequestDetails);
    }
}