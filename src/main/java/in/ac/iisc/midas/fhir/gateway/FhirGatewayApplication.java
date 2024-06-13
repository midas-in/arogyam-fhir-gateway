package in.ac.iisc.midas.fhir.gateway;

import ca.uhn.fhir.rest.server.RestfulServer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(AppProperties.class)
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ServletComponentScan(basePackageClasses = {RestfulServer.class})
@AllArgsConstructor
public class FhirGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FhirGatewayApplication.class, args);
    }

    private final AutowireCapableBeanFactory beanFactory;

    @Bean
    AppProperties properties() {
        return new AppProperties();
    }
}
