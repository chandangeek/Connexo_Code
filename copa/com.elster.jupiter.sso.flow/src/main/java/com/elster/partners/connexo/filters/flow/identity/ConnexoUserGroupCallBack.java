/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.flow.identity;

import org.kie.api.task.UserGroupCallback;
import org.uberfire.commons.services.cdi.Veto;

import java.util.List;

@Veto
public class ConnexoUserGroupCallBack implements UserGroupCallback {
	private ConnexoFlowRestProxyService connexoFlowRestProxyService;

	public ConnexoUserGroupCallBack(ConnexoFlowRestProxyService connexoFlowRestProxyService) {
		this.connexoFlowRestProxyService = connexoFlowRestProxyService;
	}

	@Override
	public boolean existsUser(String userId) {
		return connexoFlowRestProxyService.existsUser(userId);
	}

	@Override
	public boolean existsGroup(String groupId) {
		return connexoFlowRestProxyService.existsGroup(groupId);
	}

	@Override
	public List<String> getGroupsForUser(String userId, List<String> groupIds, List<String> allExistingGroupIds) {
		return connexoFlowRestProxyService.getGroupsOf(userId);
	}
}
