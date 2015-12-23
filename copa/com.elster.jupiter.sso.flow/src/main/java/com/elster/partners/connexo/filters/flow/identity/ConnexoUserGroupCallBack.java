package com.elster.partners.connexo.filters.flow.identity;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.jboss.solder.core.Veto;
import org.kie.api.task.UserGroupCallback;

@Veto
public class ConnexoUserGroupCallBack implements UserGroupCallback {

	ConnexoFlowRestProxyManager manager;

    ConnexoUserGroupCallBack() {
		manager = ConnexoFlowRestProxyManager.getInstance();
	}

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
