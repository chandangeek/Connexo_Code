package com.elster.jupiter.users.impl;

import java.time.Instant;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;

import javax.inject.Inject;

class PrivilegeInGroup {
	// persistent fields
	private long groupId;
	private String privilegeName;
	private String applicationName;

	@SuppressWarnings("unused")
	private Instant createTime;
	
	// associations
	private Privilege privilege;
	private Group group;
    private final DataModel dataModel;
	
	@Inject
	private PrivilegeInGroup(DataModel dataModel) {
        this.dataModel = dataModel;
	}
	
	PrivilegeInGroup init(Group group , String applicationName, Privilege privilege) {
		this.groupId = group.getId();
		this.group = group;
		this.privilegeName = privilege.getName();
		this.applicationName = applicationName;
		this.privilege = privilege;
        return this;
	}

    static PrivilegeInGroup from(DataModel dataModel, Group group, String applicationName, Privilege privilege) {
        return dataModel.getInstance(PrivilegeInGroup.class).init(group, applicationName, privilege);
    }

	Privilege getPrivilege() {
		if (privilege == null) {
			privilege = dataModel.mapper(Privilege.class).getExisting(privilegeName);
		}
		return privilege;
	}

	public String getApplicationName() {
		return applicationName;
	}
	
	Group getGroup() {
		if (group == null) {
			group = dataModel.mapper(Group.class).getExisting(groupId);
		}
		return group;
	}
	
	void persist() {
		dataModel.mapper(PrivilegeInGroup.class).persist(this);
	}

    public void delete() {
        dataModel.mapper(PrivilegeInGroup.class).remove(this);
    }
}
