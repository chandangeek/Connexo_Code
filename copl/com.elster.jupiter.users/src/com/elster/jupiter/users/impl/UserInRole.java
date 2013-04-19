package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.*;

public class UserInRole {
	// persistent fields
	@SuppressWarnings("unused")
	private long userId;
	private long roleId;
	// associations
	private Role role;
	
	@SuppressWarnings("unused")
	private UserInRole() {		
	}
	
	UserInRole(User user, Role role) {
		this.userId = user.getId();
		this.role = role;
		this.roleId = role.getId();		
	}
	
	Role getRole() {
		if (role == null) {
			role = Bus.getOrmClient().getRoleFactory().get(roleId);
		}
		return role;
	}
	
	void persist() {
		Bus.getOrmClient().getUserInRoleFactory().persist(this);
	}
}
