package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.Privilege;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_GROUP_NAME + "}")
final class GroupImpl implements Group {

	//persistent fields
    @SuppressWarnings("unused")
    private long id;
    @Size(max = Table.NAME_LENGTH)
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String name;
    @Size(max = Table.DESCRIPTION_LENGTH)
    private String description;
    @SuppressWarnings("unused")
	private long version;
    @SuppressWarnings("unused")
	private Instant createTime;
    @SuppressWarnings("unused")
	private Instant modTime;

    //transient fields
    @SuppressWarnings("unused") // Injected by ORM framework
	private List<PrivilegeInGroup> privilegeInGroups = new ArrayList<>();
    private final QueryService queryService;
    private final DataModel dataModel;

    @Inject
	private GroupImpl(QueryService queryService, DataModel dataModel) {
        this.queryService = queryService;
        this.dataModel = dataModel;
    }

	GroupImpl init(String name, String description) {
        this.name = name;
        this.description = description;
        return this;
	}

    static GroupImpl from(DataModel dataModel, String name, String description) {
        return dataModel.getInstance(GroupImpl.class).init(name, description);
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
	public boolean hasPrivilege(String applicationName, String privilegeCode) {
        return getPrivileges(applicationName).stream().anyMatch(privilege -> privilege.getName().equals(privilegeCode));
	}

    @Override
	public List<Privilege> getPrivileges(String applicationName) {
    	List<PrivilegeInGroup> privilegeInGroups = getPrivilegeInGroups(applicationName);
    	ImmutableList.Builder<Privilege> builder = new ImmutableList.Builder<>();
    	for (PrivilegeInGroup each : privilegeInGroups) {
    		builder.add(each.getPrivilege());
    	}
    	return builder.build();
    }

    @Override
    public Map<String, List<Privilege>> getPrivileges() {
        return getPrivilegeInGroups()
                .stream()
                .collect(Collectors.groupingBy(
                        PrivilegeInGroup::getApplicationName,
                        Collectors.mapping(PrivilegeInGroup::getPrivilege, Collectors.toList())));
    }


    private List<PrivilegeInGroup> getPrivilegeInGroups() {
        return privilegeInGroups;
    }

    private List<PrivilegeInGroup> getPrivilegeInGroups(String applicationName) {
        return getPrivilegeInGroups()
                .stream()
                .filter(p -> (applicationName == null) ? true : p.getApplicationName().equalsIgnoreCase(applicationName))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPrivilege(String applicationName, Privilege privilege) {
        return getPrivileges(applicationName).contains(privilege);
    }

	@Override
    public boolean grant(String applicationName, Privilege privilege) {
        if (hasPrivilege(applicationName, privilege)) {
            return false;
        }
		PrivilegeInGroup privilegeInGroup = PrivilegeInGroup.from(dataModel, this, applicationName, privilege);
		privilegeInGroup.persist();
		getPrivilegeInGroups(applicationName).add(privilegeInGroup);
        return false;
	}

    @Override
    public boolean revoke(String applicationName, Privilege privilege) {
    	Iterator<PrivilegeInGroup> it = getPrivilegeInGroups(applicationName).iterator();
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
	public void grant(String applicationName, String privilegeCode) {
        Privilege privilege = dataModel.mapper(Privilege.class).getExisting(privilegeCode);
		grant(applicationName, privilege);
	}

	public void save() {
        Save.action(this.id).save(this.dataModel, this);
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
    public Optional<Group> getGroup(String name) {
        return getGroupsQuery()
                .select(where("name").isEqualTo(name))
                .stream()
                .findFirst();
    }

    @Override
    public Query<Group> getGroupsQuery() {
        return queryService.wrap(dataModel.query(Group.class));
    }

}
