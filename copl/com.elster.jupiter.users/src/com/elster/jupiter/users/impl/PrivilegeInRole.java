package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.*;

class PrivilegeInRole {
	// persistent fields
	@SuppressWarnings("unused")
	private long roleId;
	private String componentName;
	private int privilegeId;
	
	@SuppressWarnings("unused")
	private PrivilegeInRole() {		
	}
	
	PrivilegeInRole(Role role , PrivilegeDescription privilegeDescription) {
		this.roleId = role.getId();
		this.componentName = privilegeDescription.getPrivilege().getComponentName();
		this.privilegeId = privilegeDescription.getPrivilege().getId();
	}

	Privilege getPrivilege() {
		return new Privilege(componentName, privilegeId);
	}
	
	PrivilegeDescription getPrivilegeDescription() {
		return Bus.getOrmClient().getPrivilegeDescriptionFactory().get(componentName,privilegeId);
	}
	
	void persist() {
		Bus.getOrmClient().getPrivilegeInRoleFactory().persist(this);
	}
	
}
