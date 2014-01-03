package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;

class PrivilegeImpl implements Privilege {
	// persistent fields
	private String componentName;
	private String name;
	private String description;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
    private final DataModel dataModel;

	@Inject
	private PrivilegeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
	}

    static PrivilegeImpl from(DataModel dataModel, String componentName , String name , String description) {
        return new PrivilegeImpl(dataModel).init(componentName, name, description);
    }

	PrivilegeImpl init(String componentName , String name , String description) {
		this.componentName = componentName;
		this.name = name;
		this.description = description;
        return this;
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
		dataModel.mapper(Privilege.class).persist(this);
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
