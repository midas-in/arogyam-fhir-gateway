package in.ac.iisc.midas.fhir.gateway.project.arogyam.opensrp;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("project.arogyam.opensrp")
@Getter
@Setter
public class OpenSrpProperties {
	@NestedConfigurationProperty
	private List<SearchNarrowEntry> whitelist = Collections.emptyList();

	@Setter
	@Getter
	public static class SearchNarrowEntry {
		private String path;
		private RequestTypeEnum method;
		private Map<String, String> queryParams = Collections.emptyMap();
	}
}
