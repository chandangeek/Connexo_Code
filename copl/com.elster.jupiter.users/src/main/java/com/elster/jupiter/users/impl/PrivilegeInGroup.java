package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.*;

class PrivilegeInGroup {
	// persistent fields
	@SuppressWarnings("unused")
	private long groupId;
	private String privilegeName;
	
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
			privilege = Bus.getOrmClient().getPrivilegeFactory().get(privilegeName);
		}
		return privilege;
	}
	
	void persist() {
		Bus.getOrmClient().getPrivilegeInGroupFactory().persist(this);
	}
	
}
