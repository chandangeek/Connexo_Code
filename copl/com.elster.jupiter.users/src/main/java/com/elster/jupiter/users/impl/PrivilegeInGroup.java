/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.Privilege;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;

class PrivilegeInGroup {
	// persistent fields
	private long groupId;
	@Size(max = NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
	private String privilegeName;
	@Size(max = 10, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_65 + "}")
	private String applicationName;

	@SuppressWarnings("unused") // Managed by ORM
	private long version;
	@SuppressWarnings("unused") // Managed by ORM
	private Instant createTime;
	@SuppressWarnings("unused") // Managed by ORM
	private Instant modTime;
	@SuppressWarnings("unused") // Managed by ORM
	private String userName;

	// associations
	private Privilege privilege;
	private Group group;
    private final DataModel dataModel;

	@Inject
	private PrivilegeInGroup(DataModel dataModel) {
        this.dataModel = dataModel;
	}

	PrivilegeInGroup init(Group group , String applicationName, Privilege privilege) {
		this.groupId = group.getId();
		this.group = group;
		this.privilegeName = privilege.getName();
		this.applicationName = applicationName;
		this.privilege = privilege;
        return this;
	}

    static PrivilegeInGroup from(DataModel dataModel, Group group, String applicationName, Privilege privilege) {
        return dataModel.getInstance(PrivilegeInGroup.class).init(group, applicationName, privilege);
    }

	Privilege getPrivilege() {
		if (privilege == null) {
			privilege = dataModel.mapper(Privilege.class).getExisting(privilegeName);
		}
		return privilege;
	}

	public String getApplicationName() {
		return applicationName;
	}

	Group getGroup() {
		if (group == null) {
			group = dataModel.mapper(Group.class).getExisting(groupId);
		}
		return group;
	}

	void persist() {
		dataModel.mapper(PrivilegeInGroup.class).persist(this);
	}

    public void delete() {
        dataModel.mapper(PrivilegeInGroup.class).remove(this);
    }
}
