package com.elster.jupiter.users.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.users.*;
import com.elster.jupiter.util.time.UtcInstant;


public class GroupImpl implements Group {
	//persistent fields
	private long id;
	private String name;
	private long versionCount;
	private UtcInstant createTime;
	private UtcInstant modTime;
	
	@SuppressWarnings("unused")
	private GroupImpl() {		
	}
	
	GroupImpl(String name) {
		this.name = name;
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
		for (Privilege each : getPrivileges()) {
			if (each.getName().equals(privilegeName)) {
				return true;
			}			
		}
		return false;
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
		Bus.getOrmClient().getGroupFactory().persist(this);
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
		return "Group: " + getName();
	}
	
	@Override
	public void grant(String privilegeName) {
		Privilege privilege = Bus.getOrmClient().getPrivilegeFactory().getExisting(privilegeName);
		add(privilege);
	}
	
	void save() {
		if (id == 0) {
			Bus.getOrmClient().getGroupFactory().persist(this);
		} else {
			Bus.getOrmClient().getGroupFactory().update(this);
		}
	}
}
