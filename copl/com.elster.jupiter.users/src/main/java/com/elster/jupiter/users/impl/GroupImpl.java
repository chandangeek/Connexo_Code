/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Currying.test;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_GROUP_NAME + "}")
final class GroupImpl implements Group {

	//persistent fields
    @SuppressWarnings("unused")
    private long id;
    @Size(max = NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String name;
    @Size(max = DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String description;
    @SuppressWarnings("unused")
	private long version;
    @SuppressWarnings("unused")
	private Instant createTime;
    @SuppressWarnings("unused")
	private Instant modTime;
    @SuppressWarnings("unused")
	private String userName;

    //transient fields
    @SuppressWarnings("unused") // Injected by ORM framework
	private List<PrivilegeInGroup> privilegeInGroups = new ArrayList<>();
    private final QueryService queryService;
    private final DataModel dataModel;
    private final UserServiceImpl userService;
    private final ThreadPrincipalService threadPrincipalService;
    private final Thesaurus thesaurus;
    private final Publisher publisher;

    @Inject
    GroupImpl(QueryService queryService, DataModel dataModel, UserService userService, ThreadPrincipalService threadPrincipalService, Thesaurus thesaurus, Publisher publisher) {
        this.queryService = queryService;
        this.dataModel = dataModel;
        this.threadPrincipalService = threadPrincipalService;
        this.thesaurus = thesaurus;
        this.publisher = publisher;
        this.userService = (UserServiceImpl) userService;
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
                .filter(p -> (applicationName == null) || p.getApplicationName().equalsIgnoreCase(applicationName))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPrivilege(String applicationName, Privilege privilege) {
        return getPrivileges(applicationName).contains(privilege);
    }

	@Override
    public boolean grant(String applicationName, Privilege privilege) {
        checkGranting(privilege);
        if (hasPrivilege(applicationName, privilege)) {
            return false;
        }
		PrivilegeInGroup privilegeInGroup = PrivilegeInGroup.from(dataModel, this, applicationName, privilege);
		privilegeInGroup.persist();
		getPrivilegeInGroups(applicationName).add(privilegeInGroup);
        publisher.publish(this, privilege);
        return false;
	}

    private void checkGranting(Privilege privilege) {
        Principal principal = threadPrincipalService.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            boolean hasGrantRightsFor = decorate(user.getGroups()
                    .stream())
                    .map(group -> group.getPrivileges().values().stream())
                    .flatMap(Function.identity())
                    .flatMap(List::stream)
                    .filterSubType(GrantPrivilege.class)
                    .anyMatch(test(GrantPrivilege::canGrant).with(privilege));
            if (!hasGrantRightsFor) {
                throw new ForbiddenException(thesaurus);
            }
        }
    }

    @Override
    public boolean revoke(String applicationName, Privilege privilege) {
        checkGranting(privilege);
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

	public void update() {
        Save.action(this.id).save(this.dataModel, this);
	}

    @Override
    public void delete() {
        this.deletePrivileges();
        this.removeUsers();
        groupFactory().remove(this);
    }

    private void deletePrivileges() {
        privilegeInGroups.forEach(PrivilegeInGroup::delete);
        this.privilegeInGroups.clear();
        this.dataModel
                .mapper(PrivilegeInGroup.class)
                .find("groupId", this.id)
                .forEach(PrivilegeInGroup::delete);
    }

    private void removeUsers() {
        this.dataModel
                .mapper(UserInGroup.class)
                .find("groupId", this.id)
                .forEach(UserInGroup::delete);
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
