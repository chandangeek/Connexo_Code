package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.*;
import com.elster.jupiter.util.time.UtcInstant;

public class UserInGroup {
	// persistent fields
	private long userId;
	private long groupId;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	// associations
	private User user;
	private Group group;

	
	@SuppressWarnings("unused")
	private UserInGroup() {		
	}
	
	UserInGroup(User user, Group group) {
		this.user = user;
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
	
	User getUser() {
		if (user == null) {
			user = Bus.getOrmClient().getUserFactory().getExisting(userId);
		}
		return user;
	}
	
	void persist() {
		Bus.getOrmClient().getUserInGroupFactory().persist(this);
	}

    public void delete() {
        Bus.getOrmClient().getUserInGroupFactory().remove(this);
    }
}
