package com.elster.jupiter.users.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.time.UtcInstant;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeDescription;
import com.elster.jupiter.users.Role;


public class RoleImpl implements Role {
	//persistent fields
	private long id;
	private String name;
	private long versionCount;
	private UtcInstant createTime;
	private UtcInstant modTime;
	
	@SuppressWarnings("unused")
	private RoleImpl() {		
	}
	
	RoleImpl(String name) {
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
	public boolean hasPrivilege(Privilege privilege) {
		for (Privilege each : getPrivileges()) {
			if (each.equals(privilege)) {
				return true;
			}			
		}
		return false;
	}
	
	List<Privilege> getPrivileges() {
		List<PrivilegeInRole> privilegeInRoles = Bus.getOrmClient().getPrivilegeInRoleFactory().find("roleId", getId());
		List<Privilege> result = new ArrayList<>(privilegeInRoles.size());
		for (PrivilegeInRole each : privilegeInRoles) {
			result.add(each.getPrivilege());
		}
		return result;
	}
	
	void addPrivilege(PrivilegeDescription privilegeDescription) {
		new PrivilegeInRole(this,privilegeDescription).persist();
	}
	
	void persist() {
		Bus.getOrmClient().getRoleFactory().persist(this);
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
		return "Role " + getName();
	}
}
