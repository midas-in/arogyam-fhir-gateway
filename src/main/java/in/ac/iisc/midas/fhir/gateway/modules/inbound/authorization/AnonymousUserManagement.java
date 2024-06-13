package in.ac.iisc.midas.fhir.gateway.modules.inbound.authorization;

import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.Permission;
import in.ac.iisc.midas.fhir.gateway.modules.inbound.shared.UserSessionDetail;
import lombok.Getter;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class AnonymousUserManagement {
	private final boolean isAnonymousAccessAllowed;
	private final Set<UserSessionDetail.GrantedAuthority> authorities;
	private final boolean isAccessPermissionNeeded;

	public AnonymousUserManagement(Boolean isAnonymousAccessAllowed,
											 Set<Permission> permissions,
											 List<String> permissionArgument,
											 Boolean isAccessPermissionNeeded) {
		this.isAnonymousAccessAllowed = isAnonymousAccessAllowed;
		authorities = anonymousUserGrantedAuthorizes(permissions, permissionArgument);
		this.isAccessPermissionNeeded = isAccessPermissionNeeded;
	}

	private static Set<UserSessionDetail.GrantedAuthority> anonymousUserGrantedAuthorizes(
		Set<Permission> permissions,
		List<String> argument) {

		var permissionArgument = new HashSetValuedHashMap<String, String>();
		argument.stream()
			.map(entry -> entry.split("/", 2))
			.filter(v -> v.length == 2)
			.forEach(v -> permissionArgument.put(v[0], v[1]));

		return permissions
			.stream()
			.filter(Objects::nonNull)
			.map(p -> new UserSessionDetail.GrantedAuthority(p, permissionArgument))
			.collect(Collectors.toSet());
	}
}
