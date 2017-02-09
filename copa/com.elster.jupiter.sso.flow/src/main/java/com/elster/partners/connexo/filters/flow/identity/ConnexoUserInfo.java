package com.elster.partners.connexo.filters.flow.identity;

import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.UserInfo;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.uberfire.commons.services.cdi.Veto;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Veto
public class ConnexoUserInfo implements UserInfo {

	@Inject
	ConnexoFlowRestProxyService connexoFlowRestProxyService;

    ConnexoUserInfo() {}

	@Override
	public String getDisplayName(OrganizationalEntity entity) {
        // We may want to provide user full name in the future
		return entity.getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<OrganizationalEntity> getMembersForGroup(Group group) {
		List<String> members = connexoFlowRestProxyService.getMembersOf(group.getId());

		List<OrganizationalEntity> membersList = new ArrayList<>();
		for(String member : members) {
			User user = TaskModelProvider.getFactory().newUser();
			((InternalOrganizationalEntity) user).setId(member);
			membersList.add(user);
		}

		return (Iterator<OrganizationalEntity>) membersList;
	}

	@Override
	public boolean hasEmail(Group group) {
		// We may want to provide email support for our users in the future
        return false;
	}

	@Override
	public String getEmailForEntity(OrganizationalEntity entity) {
		// We may want to provide email support for our users in the future
        return "";
	}

	@Override
	public String getLanguageForEntity(OrganizationalEntity entity) {
		return connexoFlowRestProxyService.getLanguageOf(entity.getId());
		//return "en_US";
	}

}
