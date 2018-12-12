/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import java.time.Instant;

public class UserInGroup {
	// persistent fields
	private long userId;
	private long groupId;
	@SuppressWarnings("unused") // Managed by ORM
	private long version;
	@SuppressWarnings("unused") // Managed by ORM
	private Instant createTime;
	@SuppressWarnings("unused") // Managed by ORM
	private Instant modTime;
	@SuppressWarnings("unused") // Managed by ORM
	private String userName;
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

	public Group getGroup() {
		if (group == null) {
			group = dataModel.mapper(Group.class).getExisting(groupId);
		}
		return group;
	}

	public User getUser() {
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
