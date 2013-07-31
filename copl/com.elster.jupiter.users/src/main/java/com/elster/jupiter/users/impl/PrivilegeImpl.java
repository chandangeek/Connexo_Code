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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Privilege)) {
            return false;
        }

        Privilege privilege = (Privilege) o;

        return name.equals(privilege.getName());

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "PrivilegeImpl{" +
                "componentName='" + componentName + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
