package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    // transient
    private List<UserInGroup> memberships;
    
    @Inject
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
        UserInGroup membership = new UserInGroup(this, group);
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
    		return Bus.getOrmClient().getUserInGroupFactory().find("user", this);
    	}
    	return memberships;
    }
    
    @Override
    public List<Group> getGroups() {
    	List<UserInGroup> userInGroups = getMemberships();
    	ImmutableList.Builder<Group> builder = ImmutableList.builder();
    	for (UserInGroup each : userInGroups) {
    		builder.add(each.getGroup());
   		}
    	return builder.build();
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
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(authenticationName.getBytes());
			messageDigest.update(":".getBytes());
			messageDigest.update(Bus.REALM.getBytes());
			messageDigest.update(":".getBytes());
			messageDigest.update(password.getBytes());			
			byte[] md5 = messageDigest.digest();
			return DatatypeConverter.printHexBinary(md5).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean check(String password) {
        return !is(password).empty() && createHa1(password).equals(ha1);
    }
}
