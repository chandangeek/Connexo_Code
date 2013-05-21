package com.elster.jupiter.users.impl;

import java.util.*;

import com.elster.jupiter.users.*;
import com.elster.jupiter.util.time.UtcInstant;

public class UserImpl implements User {
	// persistent fields
	private long id;
	private String authenticationName;
	private String firstName;
	private String lastName;
	private long versionCount;
	private UtcInstant createTime;
	private UtcInstant modTime;
	
	@SuppressWarnings("unused")
	private UserImpl() {		
	}
	
	UserImpl(String authenticationName, String firstName , String lastName) {
		this.authenticationName = authenticationName;
		this.firstName = firstName;
		this.lastName = lastName;
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
	
	List<Group> getGroups() {
		List<UserInGroup> userInGroups = Bus.getOrmClient().getUserInGroupFactory().find("userId", getId());
		List<Group> result = new ArrayList<>(userInGroups.size());
		for (UserInGroup each : userInGroups) {
			result.add(each.getGroup());
		}
		return result;
	}
	
	void addGroup(Group group) {
		new UserInGroup(this,group).persist();
	}
	
	void persist() {
		Bus.getOrmClient().getUserFactory().persist(this);
	}

	public Date getCreateDate() {
		return createTime.toDate();
	}
	
	public Date getModificationDate() {
		return modTime.toDate();
	}
	
	public long getVersion() {
		return versionCount;
	}
	
	@Override 
	public String toString() {
		return " User " + authenticationName + " ( " + firstName + " " + lastName + " )";
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
}
