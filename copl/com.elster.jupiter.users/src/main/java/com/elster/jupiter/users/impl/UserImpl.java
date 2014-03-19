package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import java.util.*;

import static com.elster.jupiter.util.Checks.is;

public class UserImpl implements User {

    private static final int MINIMAL_PASSWORD_STRENGTH = 4;
    // persistent fields
    private long id;
    private String authenticationName;
    private String description;
    private String ha1;
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String languageTag;
    private Reference<UserDirectory> userDirectory = ValueReference.absent();

    // transient
    private List<UserInGroup> memberships;

    private final DataModel dataModel;

    @Inject
    UserImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static UserImpl from(DataModel dataModel, UserDirectory userDirectory, String authenticationName) {
        return from(dataModel, userDirectory, authenticationName, null);
    }

    static UserImpl from(DataModel dataModel, UserDirectory userDirectory, String authenticationName, String description) {
        return dataModel.getInstance(UserImpl.class).init(userDirectory, authenticationName, description);
    }

    UserImpl init(UserDirectory userDirectory, String authenticationName, String description) {
        validateAuthenticationName(authenticationName);
        this.userDirectory.set(userDirectory);
        this.authenticationName = authenticationName;
        this.description = description;
        return this;
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
        UserInGroup membership = UserInGroup.from(dataModel, this, group);
        if (memberships != null) {
        	memberships.add(membership);
        }
        membership.persist();        
        return true;
    }

    @Override
    public boolean isMemberOf(Group group) {
        return getGroups().contains(group);
    }

    @Override
    public boolean leave(Group group) {
        for (UserInGroup userInGroup : getMemberships()) {
            if (group.equals(userInGroup.getGroup())) {
                userInGroup.delete();
                if (memberships != null) {
                	memberships.remove(userInGroup);
                }
                return true;
            }
        }
        return false;
    }

    private List<UserInGroup> getMemberships() {
    	if (memberships == null) {
    		return dataModel.mapper(UserInGroup.class).find("user", this);
    	}
    	return memberships;
    }
    
    @Override
    public List<Group> getGroups() {
        return userDirectory.get().getGroups(this);
    }

    List<Group> doGetGroups() {
        List<UserInGroup> userInGroups = getMemberships();
        ImmutableList.Builder<Group> builder = ImmutableList.builder();
        for (UserInGroup each : userInGroups) {
            builder.add(each.getGroup());
           }
        return builder.build();
    }

    public void save() {
        if (id == 0) {
            dataModel.mapper(User.class).persist(this);
        } else {
            dataModel.mapper(User.class).update(this);
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
        dataModel.mapper(User.class).remove(this);
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
        return Objects.hash(id);
    }

	@Override
	public String getDigestHa1() {	
		return ha1;
	}
	
	@Override
	public void setPassword(String password) {
		password = Objects.requireNonNull(password);
		if (password.length() < MINIMAL_PASSWORD_STRENGTH) {
			throw new IllegalArgumentException("Password too weak");
		}
		ha1 = createHa1(password);
	}
	
	private String createHa1(String password ) {
        return new DigestHa1Util().createHa1(userDirectory.get().getDomain(), authenticationName, password);
	}

	@Override
	public boolean check(String password) {
        return !is(password).empty() && createHa1(password).equals(ha1);
    }

    @Override
    public Optional<Locale> getLocale() {
        if (languageTag == null) {
            return Optional.absent();
        }
        return Optional.of(Locale.forLanguageTag(languageTag));
    }

    @Override
    public void setLocale(Locale locale) {
        languageTag = locale == null ? null : locale.toLanguageTag();
    }

    @Override
    public Set<Privilege> getPrivileges() {
        Set<Privilege> privileges = new HashSet<>();
        List<Group> groups = getGroups();
        for(Group group : groups){
            privileges.addAll(group.getPrivileges());
        }

        return privileges;
    }

    @Override
    public String getDomain() {
        return userDirectory.get().getDomain();
    }

    @Override
    public String getLanguage() {
        return languageTag;
    }

    @Override
    public Date getCreationDate() {
        return createTime.toDate();
    }

    @Override
    public Date getModifiedDate() {
        return modTime.toDate();
    }
}
