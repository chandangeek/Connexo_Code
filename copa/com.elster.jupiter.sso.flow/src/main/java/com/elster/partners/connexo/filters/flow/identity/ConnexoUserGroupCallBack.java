package com.elster.partners.connexo.filters.flow.identity;

import org.jboss.solder.core.Veto;
import org.kie.api.task.UserGroupCallback;
import java.util.List;

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
		return manager.getGroupsOf(userId);
	}
}
