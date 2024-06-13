package in.ac.iisc.midas.fhir.gateway.modules.logging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("hapi.fhir.logger")
@Getter
@Setter
public class LoggerProperties {
    private String name = "fhirtest.access";
    private String errorFormat = "ERROR - ${requestVerb} ${requestUrl}";
    private String format = "Path[${servletPath}] Source[${requestHeader.x-forwarded-for}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}]";
    private Boolean logExceptions = true;
}
