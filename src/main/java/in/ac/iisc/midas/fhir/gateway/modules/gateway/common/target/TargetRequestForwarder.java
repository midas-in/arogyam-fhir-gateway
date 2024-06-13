package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.models.ProxyBundleProvider;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ResourceType;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class TargetRequestForwarder {
    private final ITargetProvider targetProvider;
    private final IParser parser;

    public TargetRequestForwarder(ITargetProvider targetProvider, FhirContext context) {
        this.targetProvider = targetProvider;
        this.parser = context.newJsonParser();
    }

    @NotNull
    public ProxyBundleProvider tryParseBundleProviderResponse(RequestDetails theRequestDetails) {
        var bundle = tryParseBundleResponse(theRequestDetails);
        return new ProxyBundleProvider(bundle);
    }

    @NotNull
    public Bundle tryParseBundleResponse(RequestDetails theRequestDetails) {
        var target = targetProvider.get(theRequestDetails);
        var response = target.forward(theRequestDetails);

        // In case we have received some error during a bundle response
        if (response.getStatusLine().getStatusCode() >= 400) {
            var error = BaseServerResponseException.newInstance(response.getStatusLine().getStatusCode(), "");
            try {
                var parsedResponse = parser.parseResource(response.getEntity().getContent());
                if (parsedResponse instanceof OperationOutcome) {
                    error.setOperationOutcome((IBaseOperationOutcome) parsedResponse);
                    throw error;
                }
                var outcome = new OperationOutcome();
                outcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.FATAL);
                error.setOperationOutcome(outcome);
                throw error;
            } catch (IOException e) {
                log.error("Failed to parse", e);
                var outcome = new OperationOutcome();
                outcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.FATAL);
                error.setOperationOutcome(outcome);
                throw error;
            }
        }

        try {
            return parser.parseResource(Bundle.class, response.getEntity().getContent());
        } catch (IOException e) {
            log.error("Failed to parse resource", e);
            OperationOutcome oo = new OperationOutcome();
            oo.addIssue().setSeverity(OperationOutcome.IssueSeverity.FATAL);
            throw new InternalErrorException("InternalServerError", oo);
        }
    }

    @NotNull
    public MethodOutcome tryParseMethodOutcomeResponse(RequestDetails theRequestDetails) {
        var target = targetProvider.get(theRequestDetails);
        var response = target.forward(theRequestDetails);

        // In case we have received some error during a bundle response
        if (response.getStatusLine().getStatusCode() >= 400) {
            var error = BaseServerResponseException.newInstance(response.getStatusLine().getStatusCode(), "");
            try {
                var parsedResponse = parser.parseResource(response.getEntity().getContent());
                if (parsedResponse instanceof OperationOutcome) {
                    error.setOperationOutcome((IBaseOperationOutcome) parsedResponse);
                    throw error;
                }
                var outcome = new OperationOutcome();
                outcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.FATAL);
                error.setOperationOutcome(outcome);
                throw error;
            } catch (IOException e) {
                log.error("Failed to parse", e);
                var outcome = new OperationOutcome();
                outcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.FATAL);
                error.setOperationOutcome(outcome);
                throw error;
            }
        }

        try {
            var resource = parser.parseResource(response.getEntity().getContent());
            var methodOutcome = new MethodOutcome();
            methodOutcome.setResource(resource);
            methodOutcome.setCreatedUsingStatusCode(response.getStatusLine().getStatusCode());
            methodOutcome.setId(resource.getIdElement());
            return methodOutcome;
        } catch (IOException e) {
            log.error("Failed to parse resource", e);
            OperationOutcome oo = new OperationOutcome();
            oo.addIssue().setSeverity(OperationOutcome.IssueSeverity.FATAL);
            throw new InternalErrorException("InternalServerError", oo);
        }
    }

    public IBaseResource tryParseResourceResponse(RequestDetails theRequestDetails) {
        var target = targetProvider.get(theRequestDetails);
        var response = target.forward(theRequestDetails);
        try {
            var inflateResponse = handleAutoInflateResponse(response, theRequestDetails);
            if (inflateResponse != null) return inflateResponse;

            var parsedResponse = parser.parseResource(response.getEntity().getContent());
            if (parsedResponse instanceof OperationOutcome) {
                var error = BaseServerResponseException.newInstance(response.getStatusLine().getStatusCode(), "");
                error.setOperationOutcome((IBaseOperationOutcome) parsedResponse);
                throw error;
            }
            return parsedResponse;
        } catch (IOException e) {
            log.error("Failed to parse resource", e);
            OperationOutcome oo = new OperationOutcome();
            oo.addIssue().setSeverity(OperationOutcome.IssueSeverity.FATAL);
            throw new InternalErrorException("InternalServerError", oo);
        }
    }

    @SneakyThrows
    public void tryBinaryResponse(RequestDetails theRequestDetails) {
        var target = targetProvider.get(theRequestDetails);
        var response = target.forward(theRequestDetails);
        try {
            var bytes = response.getEntity().getContent().readAllBytes();
            Header contentTypeHeader = response.getFirstHeader("content-type");
            // Write the bytes to output stream
            var stream = theRequestDetails
                    .getResponse()
                    .getResponseOutputStream(
                            response.getStatusLine().getStatusCode(),
                            contentTypeHeader.getValue(),
                            bytes.length
                    );
            stream.write(bytes);

            // Commit the response
            theRequestDetails.getResponse().commitResponse(stream);
        } catch (Exception e) {
            log.error("Failed to load binary", e);
            var error = "Failed";
            var bytes = error.getBytes();
            var stream = theRequestDetails.getResponse().getResponseOutputStream(500, "application/octet-stream", bytes.length);
            stream.write(bytes);
            theRequestDetails.getResponse().commitResponse(stream);
        }
    }

    /**
     * At time Binary resource will be inflated and send out by the origin fhir server. We will
     * deflate it and send out back
     *
     * @param response
     * @return
     */
    @SneakyThrows
    private IBaseResource handleAutoInflateResponse(HttpResponse response, RequestDetails requestDetails) {
        // If the request fails
        if (response.getStatusLine().getStatusCode() >= 400) return null;
        // Only binary are inflated
        if (!Objects.equals(requestDetails.getResourceName(), ResourceType.Binary.name())) return null;
        // This is not an individual binary read, so it won't be inflated
        if (requestDetails.getId() == null) return null;
        // If the source server has sent fhir json, that mean the resource hasn't been inflated, so
        // we will proceed as it is
        Header contentTypeHeader = response.getFirstHeader("content-type");
        if (contentTypeHeader == null || contentTypeHeader.getValue().startsWith("application/fhir+json")) return null;

        // Read all bytes
        var bytes = response.getEntity().getContent().readAllBytes();

        // Write the bytes to output stream
        var stream = requestDetails.getResponse().getResponseOutputStream(response.getStatusLine().getStatusCode(), contentTypeHeader.getValue(), bytes.length);
        stream.write(bytes);

        // Commit the response
        requestDetails.getResponse().commitResponse(stream);
        return new Binary()
                .setData(bytes)
                .setContentType(contentTypeHeader.getValue())
                .setId(requestDetails.getId());
    }

}
