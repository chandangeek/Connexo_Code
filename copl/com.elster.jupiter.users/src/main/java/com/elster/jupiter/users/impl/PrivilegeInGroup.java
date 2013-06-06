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
	Privilege privilege;
	
	@SuppressWarnings("unused")
	private PrivilegeInGroup() {		
	}
	
	PrivilegeInGroup(Group group , Privilege privilege) {
		this.groupId = group.getId();
		this.privilegeName = privilege.getName();
		this.privilege = privilege;
	}

	Privilege getPrivilege() {
		if (privilege == null) {
			privilege = Bus.getOrmClient().getPrivilegeFactory().getExisting(privilegeName);
		}
		return privilege;
	}
	
	void persist() {
		Bus.getOrmClient().getPrivilegeInGroupFactory().persist(this);
	}
	
}
