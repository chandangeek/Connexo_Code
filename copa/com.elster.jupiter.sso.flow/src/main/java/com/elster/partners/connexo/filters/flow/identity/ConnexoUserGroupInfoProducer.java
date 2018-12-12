/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.flow.identity;

import org.jbpm.services.cdi.Selectable;
import org.jbpm.services.cdi.producer.UserGroupInfoProducer;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.task.api.UserInfo;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
@Alternative
@Selectable
public class ConnexoUserGroupInfoProducer implements UserGroupInfoProducer {

	private UserGroupCallback callback;
	private UserInfo userInfo;

	@Inject
	ConnexoFlowRestProxyService connexoFlowRestProxyService;

	public ConnexoUserGroupInfoProducer() {
	}

	@PostConstruct
	public void init() {
		callback = new ConnexoUserGroupCallBack(connexoFlowRestProxyService);
		userInfo = new ConnexoUserInfo();
	}

	@Override
	@Produces
	public UserGroupCallback produceCallback() {

		return callback;
	}

	@Override
	@Produces
	public UserInfo produceUserInfo() {

		return userInfo;
	}

}
