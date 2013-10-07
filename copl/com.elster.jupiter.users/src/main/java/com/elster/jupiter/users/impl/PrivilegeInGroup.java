package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.*;
import com.elster.jupiter.util.time.UtcInstant;

class PrivilegeInGroup {
	// persistent fields
	@SuppressWarnings("unused")
	private long groupId;
	private String privilegeName;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	
	// associations
	private Privilege privilege;
	private Group group;
	
	@SuppressWarnings("unused")
	private PrivilegeInGroup() {		
	}
	
	PrivilegeInGroup(Group group , Privilege privilege) {		
		this.groupId = group.getId();
		this.group = group;
		this.privilegeName = privilege.getName();
		this.privilege = privilege;
	}

	Privilege getPrivilege() {
		if (privilege == null) {
			privilege = Bus.getOrmClient().getPrivilegeFactory().getExisting(privilegeName);
		}
		return privilege;
	}
	
	Group getGroup() {
		if (group == null) {
			group = Bus.getOrmClient().getGroupFactory().getExisting(groupId);
		}
		return group;
	}
	
	void persist() {
		Bus.getOrmClient().getPrivilegeInGroupFactory().persist(this);
	}

    public void delete() {
        Bus.getOrmClient().getPrivilegeInGroupFactory().remove(this);

    }
}
