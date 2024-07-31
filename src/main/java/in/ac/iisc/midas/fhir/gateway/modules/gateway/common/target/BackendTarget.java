package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class BackendTarget {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Set<String> forwardHeadersToBackend;
    private final Set<String> sendHeaderToFrontend;
    @Getter
    private final IGenericClient fhirClient;

    public BackendTarget(String baseUrl, FhirContext fhirContext, Set<String> forwardHeaders, Set<String> sendHeaderToFrontend) {
        this.baseUrl = baseUrl;
        httpClient = HttpClientBuilder.create().build();
        this.fhirClient = fhirContext.newRestfulGenericClient(baseUrl);
        this.fhirClient.registerInterceptor(new LoggingInterceptor());
        this.forwardHeadersToBackend = forwardHeaders;
        this.sendHeaderToFrontend = sendHeaderToFrontend;
    }

    public HttpResponse forward(RequestDetails requestDetails) {
        var request = createRequest((ServletRequestDetails) requestDetails);
        var response = execute(request);
        passHeaderToResponseIfValid(response, requestDetails);
        return response;
    }

    private void passHeaderToResponseIfValid(HttpResponse response, RequestDetails requestDetails) {
        // Only if the request is successful the header will be sent out
        if (response.getStatusLine().getStatusCode() >= 300) return;
        sendHeaderToFrontend.forEach(headerName -> {
            var values = response.getHeaders(headerName);
            Arrays.asList(values).forEach(value -> requestDetails.getResponse().addHeader(headerName, value.getValue()));
        });
    }

    public MultiValuedMap<String, String> additionalHeaders(RequestDetails requestDetails) {
        var headers = new HashSetValuedHashMap<String, String>();
        forwardHeadersToBackend.forEach(header -> {
            var val = requestDetails.getHeaders(header);
            if (val.isEmpty()) return;
            headers.putAll(header, val);
        });
        return headers;
    }

    private HttpResponse execute(HttpUriRequest request) {
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            log.error("Unable to complete the request");
            throw new InternalErrorException("Internal server error");
        }
    }

    private HttpUriRequest createRequest(ServletRequestDetails requestDetails) {
        var incomingRequest = requestDetails.getServletRequest();
        var builder = RequestBuilder.create(incomingRequest.getMethod())
                .setUri(getUri(requestDetails));
        copyRequiredHeaders(requestDetails, builder);
        copyParameters(requestDetails, builder);
        setBodyIfApplicable(requestDetails, builder);
        return builder.build();
    }

    private String getUri(ServletRequestDetails requestDetails) {
        return String.format("%s/%s", baseUrl, requestDetails.getRequestPath());
    }

    private void setBodyIfApplicable(ServletRequestDetails requestDetails, RequestBuilder builder) {
        byte[] incomingBody = requestDetails.loadRequestContents();
        if (incomingBody != null && incomingBody.length > 0) {
            String contentType = requestDetails.getServletRequest().getHeader("Content-Type");
            if (contentType == null) {
                throw new InvalidRequestException("Missing Content-Type Header");
            }
            builder.setEntity(new ByteArrayEntity(incomingBody));
        }
    }

    private void copyRequiredHeaders(ServletRequestDetails request, RequestBuilder builder) {
        for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
            if (forwardHeadersToBackend.contains(entry.getKey().toLowerCase())) {
                for (String value : entry.getValue()) {
                    builder.addHeader(entry.getKey(), value);
                }
            }
        }
    }

    private static void copyParameters(ServletRequestDetails request, RequestBuilder builder) {
        for (Map.Entry<String, String[]> entry : request.getParameters().entrySet()) {
            for (String val : entry.getValue()) {
                builder.addParameter(entry.getKey(), val);
            }
        }
    }
}
