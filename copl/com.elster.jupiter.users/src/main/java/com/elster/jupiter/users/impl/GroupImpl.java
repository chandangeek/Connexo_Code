package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.util.To;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;


final class GroupImpl implements Group {
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

    @Override
	public List<Privilege> getPrivileges() {
		List<PrivilegeInGroup> privilegeInGroups = fetchPrivilegeInGroups();
		List<Privilege> result = new ArrayList<>(privilegeInGroups.size());
		for (PrivilegeInGroup each : privilegeInGroups) {
			result.add(each.getPrivilege());
		}
		return result;
	}

    @Override
    public boolean hasPrivilege(Privilege privilege) {
        return getPrivileges().contains(privilege);
    }
	
	@Override
    public boolean grant(Privilege privilege) {
        if (hasPrivilege(privilege)) {
            return false;
        }
		new PrivilegeInGroup(this,privilege).persist();
        return false;
	}

    @Override
    public boolean revoke(Privilege privilege) {
        for (PrivilegeInGroup each : fetchPrivilegeInGroups()) {
            if (each.getPrivilege().equals(privilege)) {
                each.delete();
                return true;
            }
        }
        return false;
    }

    private List<PrivilegeInGroup> fetchPrivilegeInGroups() {
        return Bus.getOrmClient().getPrivilegeInGroupFactory().find("groupId", getId());
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
		grant(privilege);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Group)) {
            return false;
        }

        Group group = (Group) o;

        return id == group.getId();

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
