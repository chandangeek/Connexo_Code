package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.*;
import com.elster.jupiter.util.time.UtcInstant;

public class UserInGroup {
	// persistent fields
	@SuppressWarnings("unused")
	private long userId;
	private long groupId;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
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
			group = Bus.getOrmClient().getGroupFactory().getExisting(groupId);
			
		}
		return group;
	}
	
	void persist() {
		Bus.getOrmClient().getUserInGroupFactory().persist(this);
	}
}
