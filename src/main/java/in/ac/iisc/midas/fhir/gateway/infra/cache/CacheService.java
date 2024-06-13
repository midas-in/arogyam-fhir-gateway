package in.ac.iisc.midas.fhir.gateway.infra.cache;

import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class CacheService {
	private final CacheManager cacheManager;

	void evictCache(String cacheName) {
		var cache = cacheManager.getCache(cacheName);
		if (cache == null) return;
		cache.clear();
	}

	@Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.MINUTES)
	void evictAllCache() {
		cacheManager.getCacheNames().forEach(this::evictCache);
	}
}
