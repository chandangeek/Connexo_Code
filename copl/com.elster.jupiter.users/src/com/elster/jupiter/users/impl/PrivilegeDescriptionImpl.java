package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeDescription;

class PrivilegeDescriptionImpl implements PrivilegeDescription {
	// persistent fields
	private String componentName;
	private int id;
	private String description;
	// derived fields
	private Privilege privilege;

	@SuppressWarnings("unused")
	private PrivilegeDescriptionImpl() {		
	}
	
	PrivilegeDescriptionImpl(Privilege privilege , String description) {
		this.privilege = privilege;
		this.componentName = privilege.getComponentName();
		this.id = privilege.getId();
		this.description = description;
	}
	
	@Override
	public Privilege getPrivilege() {
		if (privilege == null) {
			privilege = new Privilege(componentName , id);
		}
		return privilege;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	String getComponentName() {
		return componentName;
	}

	long getId() {
		return id;
	}

	void persist() {
		Bus.getOrmClient().getPrivilegeDescriptionFactory().persist(this);
	}
	
	@Override
	public boolean equals(Object other) {
		try {
			PrivilegeDescriptionImpl o = (PrivilegeDescriptionImpl) other;
			return this.componentName.equals(o.componentName) && this.id == o.id;
		} catch (ClassCastException ex) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.componentName.hashCode() ^ Long.valueOf(id).hashCode();
	}
}
