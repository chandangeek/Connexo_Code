package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;

public class UserInGroup {
	// persistent fields
	private long userId;
	private long groupId;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	// associations
	private User user;
	private Group group;
    private final DataModel dataModel;
	
	@Inject
	private UserInGroup(DataModel dataModel) {
        this.dataModel = dataModel;
	}
	
	UserInGroup init(User user, Group group) {
		this.user = user;
		this.userId = user.getId();
		this.group = group;
		this.groupId = group.getId();
        return this;
	}

    static UserInGroup from(DataModel dataModel, User user, Group group) {
        return dataModel.getInstance(UserInGroup.class).init(user, group);
    }
	
	Group getGroup() {
		if (group == null) {
			group = dataModel.mapper(Group.class).getExisting(groupId);
		}
		return group;
	}
	
	User getUser() {
		if (user == null) {
			user = dataModel.mapper(User.class).getExisting(userId);
		}
		return user;
	}
	
	void persist() {
        dataModel.mapper(UserInGroup.class).persist(this);
	}

    public void delete() {
        dataModel.mapper(UserInGroup.class).remove(this);
    }
}
