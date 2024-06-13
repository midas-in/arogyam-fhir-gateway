package in.ac.iisc.midas.fhir.gateway.modules.gateway;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.ApacheProxyAddressStrategy;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import ca.uhn.fhir.rest.server.tenant.ITenantIdentificationStrategy;
import in.ac.iisc.midas.fhir.gateway.modules.cors.CorsProperties;
import in.ac.iisc.midas.fhir.gateway.modules.cors.OnCorsPresent;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.*;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.condition.MultiTenantGatewayCondition;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.multitenant.GatewayMultiTenantIdentificationStrategy;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.provider.BackendResourceProviderFactory;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.ITargetProvider;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.SingleLocalTargetSelector;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.TargetRequestForwarder;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.authentication.AuthenticationInterceptor;
import in.ac.iisc.midas.fhir.gateway.modules.logging.LoggerProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@Configuration
@AllArgsConstructor
@Slf4j
public class GatewayFhirServerConfigR4 {
    private final AutowireCapableBeanFactory beanFactory;

    @Bean
    FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    @Conditional(OnCorsPresent.class)
    @Bean
    public CorsInterceptor corsInterceptor(CorsProperties cors) {
        // Define your CORS configuration. This is an example
        // showing a typical setup. You should customize this
        // to your specific needs
        log.info("CORS is enabled on this server");
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader(HttpHeaders.ORIGIN);
        config.addAllowedHeader(HttpHeaders.ACCEPT);
        config.addAllowedHeader(HttpHeaders.CONTENT_TYPE);
        config.addAllowedHeader(HttpHeaders.AUTHORIZATION);
        config.addAllowedHeader(HttpHeaders.CACHE_CONTROL);
        config.addAllowedHeader("x-fhir-starter");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Prefer");

        Set<String> allAllowedCORSOrigins = cors.getAllowedOrigin();
        allAllowedCORSOrigins.forEach(config::addAllowedOriginPattern);
        log.info("CORS allows the following origins: " + String.join(", ", allAllowedCORSOrigins));

        config.addExposedHeader("Location");
        config.addExposedHeader("Content-Location");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        config.setAllowCredentials(cors.getAllowCredentials());

        // Create the interceptor and register it
        return new CorsInterceptor(config);
    }

    @Bean
    public LoggingInterceptor loggingInterceptor(LoggerProperties loggerProperties) {
        /*
         * Add some logging for each request
         */

        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLoggerName(loggerProperties.getName());
        loggingInterceptor.setMessageFormat(loggerProperties.getFormat());
        loggingInterceptor.setErrorMessageFormat(loggerProperties.getErrorFormat());
        loggingInterceptor.setLogExceptions(loggerProperties.getLogExceptions());
        return loggingInterceptor;
    }

    @Bean
    GatewayProperties gatewayProperties() {
        return new GatewayProperties();
    }

    @Bean
    @Conditional(MultiTenantGatewayCondition.class)
    ITenantIdentificationStrategy tenantIdentificationStrategy() {
        return new GatewayMultiTenantIdentificationStrategy();
    }

    @Bean
    ResourceProviderFactory resourceProviderFactory(TargetRequestForwarder requestForwarder) {
        return new BackendResourceProviderFactory(requestForwarder);
    }

    @Bean("gatewayRestfulServer")
    public GatewayRestfulServer gatewayRestfulServer(FhirContext fhirContext,
                                                     TargetRequestForwarder forwarder,
                                                     LoggingInterceptor loggingInterceptor,
                                                     Optional<CorsInterceptor> corsInterceptor,
                                                     ResourceProviderFactory resourceProviderFactory,
                                                     Optional<ITenantIdentificationStrategy> tenantIdentificationStrategy,
                                                     Optional<AuthenticationInterceptor> authenticationInterceptor,
                                                     Optional<AuthorizationInterceptor> authorizationInterceptor
    ) {
        var fhirServer = new GatewayRestfulServer(fhirContext);
        tenantIdentificationStrategy.ifPresent(fhirServer::setTenantIdentificationStrategy);
        fhirServer.setServerName("Gateway");
        fhirServer.registerInterceptor(loggingInterceptor);
        corsInterceptor.ifPresent(fhirServer::registerInterceptor);
        authenticationInterceptor.ifPresent(i -> {
            fhirServer.registerInterceptor(i);
            authorizationInterceptor.ifPresent(fhirServer::registerInterceptor);
        });
        fhirServer.setServerAddressStrategy(ApacheProxyAddressStrategy.forHttps());
        fhirServer.registerProviders(resourceProviderFactory.createProviders());
        return fhirServer;
    }

    @Bean
    public ITargetProvider targetProvider(FhirContext fhirContext, GatewayProperties properties) {
        return new SingleLocalTargetSelector(fhirContext, properties);
    }

    @Bean
    public TargetRequestForwarder requestForwarder(ITargetProvider targetProvider, FhirContext context) {
        return new TargetRequestForwarder(targetProvider, context);
    }

    @Bean
    public ServletRegistrationBean gatewayServletRegistration(GatewayRestfulServer gatewayRestfulServer) {
        var servletRegistrationBean = new ServletRegistrationBean();
        beanFactory.autowireBean(gatewayRestfulServer);
        servletRegistrationBean.setServlet(gatewayRestfulServer);
        servletRegistrationBean.addUrlMappings("/fhir/*");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }
}
