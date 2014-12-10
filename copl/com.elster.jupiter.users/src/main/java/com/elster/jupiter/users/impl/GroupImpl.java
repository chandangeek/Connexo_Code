package com.elster.jupiter.users.impl;

import static com.elster.jupiter.util.Checks.is;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.google.common.collect.ImmutableList;


final class GroupImpl implements Group , PersistenceAware {
	//persistent fields
	private long id;
	private String name;
	private long version;
	private Instant createTime;
	private Instant modTime;
	
	//transient fields
	private List<PrivilegeInGroup> privilegeInGroups;
    private final DataModel dataModel;
    private String description;

    @Inject
	private GroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }
	
	GroupImpl init(String name, String description) {
        validateName(name);
        this.name = name;
        this.description = description;
        return this;
	}

    static GroupImpl from(DataModel dataModel, String name, String description) {
        return dataModel.getInstance(GroupImpl.class).init(name, description);
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
	public boolean hasPrivilege(String privilegeCode) {
        return getPrivileges().stream().anyMatch(privilege -> privilege.getName().equals(privilegeCode));
	}

    @Override
	public List<Privilege> getPrivileges() {    	
    	List<PrivilegeInGroup> privilegeInGroups = getPrivilegeInGroups();
    	ImmutableList.Builder<Privilege> builder = new ImmutableList.Builder<>();
    	for (PrivilegeInGroup each : privilegeInGroups) {
    		builder.add(each.getPrivilege());
    	}    	    	
    	return builder.build();
    }
    
    private List<PrivilegeInGroup> getPrivilegeInGroups() {
    	if (privilegeInGroups == null) {
    		privilegeInGroups = dataModel.mapper(PrivilegeInGroup.class).find("groupId", getId());
    	}
    	return privilegeInGroups;
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
		PrivilegeInGroup privilegeInGroup = PrivilegeInGroup.from(dataModel, this, privilege);
		privilegeInGroup.persist();
		getPrivilegeInGroups().add(privilegeInGroup);
        return false;
	}

    @Override
    public boolean revoke(Privilege privilege) {
    	Iterator<PrivilegeInGroup> it = getPrivilegeInGroups().iterator();
    	while (it.hasNext()) {
    		PrivilegeInGroup each = it.next();
    		if (each.getPrivilege().equals(privilege)) {
                each.delete();
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public Instant getCreationDate() {
        return createTime;
    }

    @Override
    public Instant getModifiedDate() {
        return modTime;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;

    }

    void persist() {
		groupFactory().persist(this);
	}
    
    
    private DataMapper<Group> groupFactory() {
        return dataModel.mapper(Group.class);
    }

    public long getVersion() {
		return version;
	}
	
	@Override
	public String toString() {
		return "Group: " + getName();
	}
	
	@Override
	public void grant(String privilegeCode) {
        Privilege privilege = dataModel.mapper(Privilege.class).getExisting(privilegeCode);
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
        return Objects.hash(id);
    }

	@Override
	public void postLoad() {
		getPrivileges();
	}
}
