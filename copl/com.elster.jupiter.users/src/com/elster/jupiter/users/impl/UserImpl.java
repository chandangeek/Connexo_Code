package com.elster.jupiter.users.impl;

import java.util.*;

import com.elster.jupiter.time.UtcInstant;
import com.elster.jupiter.users.*;

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
	public boolean hasPrivilege(Privilege privilege) {
		for (Role each : getRoles()) {
			if (each.hasPrivilege(privilege)) {
				return true;
			}
		}
		return false;
	}
	
	@Override 
	public String getName() {
		return authenticationName;
	}
	
	List<Role> getRoles() {
		List<UserInRole> userInRoles = Bus.getOrmClient().getUserInRoleFactory().find("userId", getId());
		List<Role> result = new ArrayList<>(userInRoles.size());
		for (UserInRole each : userInRoles) {
			result.add(each.getRole());
		}
		return result;
	}
	
	void addRole(Role role) {
		new UserInRole(this,role).persist();
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
	public boolean hasRole(String roleName) {
		return true;
	}
}
