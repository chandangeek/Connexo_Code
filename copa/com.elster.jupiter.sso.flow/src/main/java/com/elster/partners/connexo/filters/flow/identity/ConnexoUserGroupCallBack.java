package com.elster.partners.connexo.filters.flow.identity;

import java.util.List;

import org.jboss.solder.core.Veto;
import org.kie.api.task.UserGroupCallback;

@Veto
public class ConnexoUserGroupCallBack implements UserGroupCallback {

	ConnexoRestProxyManager manager = ConnexoRestProxyManager.getInstance();

    ConnexoUserGroupCallBack() {}

	@Override
	public boolean existsUser(String userId) {
		return manager.existsUser(userId);
	}

	@Override
	public boolean existsGroup(String groupId) {
		return manager.existsGroup(groupId);
	}

	@Override
	public List<String> getGroupsForUser(String userId, List<String> groupIds, List<String> allExistingGroupIds) {
		List<String> roles = manager.getGroupsOf(userId);
		// TODO - remove this when the AuthorizationManager issue is fixed
		roles.add("admin");
		return roles;
	}
}
