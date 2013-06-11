package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.To;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;


class GroupImpl implements Group {
	//persistent fields
	private long id;
	private String name;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime;
	
	@SuppressWarnings("unused")
	private GroupImpl() {		
	}
	
	GroupImpl(String name) {
        validateName(name);
        this.name = name;
	}

    private void validateName(String name) {
        if (is(name).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("Group name cannot be empty.");
        }
    }

    @Override
	public long getId() {
		return id;		
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean hasPrivilege(String privilegeName) {
        return FluentIterable.from(getPrivileges()).transform(To.Name).contains(privilegeName);
	}
	
	List<Privilege> getPrivileges() {
		List<PrivilegeInGroup> privilegeInGroups = Bus.getOrmClient().getPrivilegeInGroupFactory().find("groupId", getId());
		List<Privilege> result = new ArrayList<>(privilegeInGroups.size());
		for (PrivilegeInGroup each : privilegeInGroups) {
			result.add(each.getPrivilege());
		}
		return result;
	}
	
	void add(User user) {
		new UserInGroup(user, this).persist();
	}
	
	void add(Privilege privilege) {
		new PrivilegeInGroup(this,privilege).persist();
	}
	
	void persist() {
		groupFactory().persist(this);
	}

    private DataMapper<Group> groupFactory() {
        return Bus.getOrmClient().getGroupFactory();
    }

    public Date getCreateDate() {
		return createTime.toDate();
	}
	
	public Date getModificationDate() {
		return modTime.toDate();
	}
	
	public long getVersion() {
		return version;
	}
	
	@Override
	public String toString() {
		return "Group: " + getName();
	}
	
	@Override
	public void grant(String privilegeName) {
		Privilege privilege = Bus.getOrmClient().getPrivilegeFactory().getExisting(privilegeName);
		add(privilege);
	}
	
	public void save() {
		if (id == 0) {
			groupFactory().persist(this);
		} else {
			groupFactory().update(this);
		}
	}

    @Override
    public void delete() {
        groupFactory().remove(this);
    }
}
