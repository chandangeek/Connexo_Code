package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.*;

public class UserInGroup {
	// persistent fields
	@SuppressWarnings("unused")
	private long userId;
	private long groupId;
	// associations
	private Group group;
	
	@SuppressWarnings("unused")
	private UserInGroup() {		
	}
	
	UserInGroup(User user, Group group) {
		this.userId = user.getId();
		this.group = group;
		this.groupId = group.getId();		
	}
	
	Group getGroup() {
		if (group == null) {
			group = Bus.getOrmClient().getGroupFactory().get(groupId);
			
		}
		return group;
	}
	
	void persist() {
		Bus.getOrmClient().getUserInGroupFactory().persist(this);
	}
}
