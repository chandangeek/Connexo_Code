package com.elster.partners.connexo.filters.flow.identity;

import org.jbpm.services.cdi.Selectable;
import org.jbpm.services.cdi.producer.UserGroupInfoProducer;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.task.api.UserInfo;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@ApplicationScoped
@Alternative
@Selectable
public class ConnexoUserGroupInfoProducer implements UserGroupInfoProducer {

	private UserGroupCallback	callback	= new ConnexoUserGroupCallBack();
	private UserInfo			userInfo	= new ConnexoUserInfo();

	public ConnexoUserGroupInfoProducer() {
	}

	@Override
	public UserGroupCallback produceCallback() {

		return callback;
	}

	@Override
	public UserInfo produceUserInfo() {

		return userInfo;
	}

}
