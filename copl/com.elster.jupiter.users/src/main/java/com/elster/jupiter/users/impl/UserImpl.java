package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;

import java.util.Date;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

public class UserImpl implements User {

    // persistent fields
    private long id;
    private String authenticationName;
    private String description;
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;

    // transient
    private List<Group> groups;
    
    @SuppressWarnings("unused")
    private UserImpl() {
    }

    UserImpl(String authenticationName) {
        this(authenticationName, null);
    }

    UserImpl(String authenticationName, String description) {
        validateAuthenticationName(authenticationName);
        this.authenticationName = authenticationName;
        this.description = description;
    }

    private void validateAuthenticationName(String authenticationName) {
        if (is(authenticationName).emptyOrOnlyWhiteSpace()) {
            throw new IllegalArgumentException("User's authentication name cannot be empty.");
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean hasPrivilege(String privilegeName) {
        for (Group each : getGroups()) {
            if (each.hasPrivilege(privilegeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return authenticationName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;

    }

    @Override
    public boolean join(Group group) {
        if (isMemberOf(group)) {
            return false;
        }
        new UserInGroup(this, group).persist();
        clearGroups();
        return true;
    }

    @Override
    public boolean isMemberOf(Group group) {
        return getGroups().contains(group);
    }

    @Override
    public boolean leave(Group group) {
        for (UserInGroup userInGroup : fetchMemberships()) {
            if (group.equals(userInGroup.getGroup())) {
                userInGroup.delete();
                clearGroups();
                return true;
            }
        }
        return false;
    }

    private List<UserInGroup> fetchMemberships() {
        return Bus.getOrmClient().getUserInGroupFactory().find("userId", getId());
    }

    private void clearGroups() {
    	groups = null;
    }
    
    @Override
    public List<Group> getGroups() {
    	if (groups == null) {
    		List<UserInGroup> userInGroups = fetchMemberships();
    		ImmutableList.Builder<Group> builder = ImmutableList.builder();
    		for (UserInGroup each : userInGroups) {
    			builder.add(each.getGroup());
    		}
    		groups = builder.build();
    	}
    	return groups;
    }

    void addGroup(Group group) {
        new UserInGroup(this, group).persist();
    }

    public void save() {
        if (id == 0) {
            Bus.getOrmClient().getUserFactory().persist(this);
        } else {
            Bus.getOrmClient().getUserFactory().update(this);
        }
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
        return " User " + authenticationName;
    }

    @Override
    public boolean isMemberOf(String groupName) {
        for (Group each : getGroups()) {
            if (each.getName().equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void delete() {
        Bus.getOrmClient().getUserFactory().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        User user = (User) o;

        return id == user.getId();

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
