package in.ac.iisc.midas.fhir.gateway.infra.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

	@Bean
	CacheService cacheService(CacheManager cacheManager) {
		return new CacheService(cacheManager);
	}
}
