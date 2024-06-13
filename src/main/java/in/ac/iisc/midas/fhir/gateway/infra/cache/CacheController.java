package in.ac.iisc.midas.fhir.gateway.infra.cache;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cache")
@AllArgsConstructor
public class CacheController {
	private final CacheService cacheService;

	@DeleteMapping("{cacheName}")
	void clearCache(@PathVariable String cacheName) {
		cacheService.evictCache(cacheName);
	}

	@DeleteMapping()
	void clearCache() {
		cacheService.evictAllCache();
	}

}
