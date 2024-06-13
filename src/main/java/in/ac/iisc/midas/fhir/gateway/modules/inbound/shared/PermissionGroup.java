package in.ac.iisc.midas.fhir.gateway.modules.inbound.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PermissionGroup {
	private Permission name;
	private List<String> arguments;
}
