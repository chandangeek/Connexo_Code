package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.util.time.UtcInstant;

class PrivilegeImpl implements Privilege {
	// persistent fields
	private String componentName;
	private String name;
	private String description;
	@SuppressWarnings("unused")
	private UtcInstant createTime;

	@SuppressWarnings("unused")
	private PrivilegeImpl() {		
	}
	
	PrivilegeImpl(String componentName , String name , String description) {
		this.componentName = componentName;
		this.name = name;
		this.description = description;
	}
	

	@Override 
	public String getName() {
		return name;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public String getComponentName() {
		return componentName;
	}

	void persist() {
		Bus.getOrmClient().getPrivilegeFactory().persist(this);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PrivilegeImpl) {
			PrivilegeImpl o = (PrivilegeImpl) other;
			return this.name.equals(o.name);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
