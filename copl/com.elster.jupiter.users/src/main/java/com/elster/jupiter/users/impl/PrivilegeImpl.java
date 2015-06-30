package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;
import javax.inject.Inject;

class PrivilegeImpl implements Privilege {
	// persistent fields
	private String name;
    private Reference<Resource> resource = ValueReference.absent();
	private final DataModel dataModel;

	@Inject
	private PrivilegeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
	}

    static PrivilegeImpl from(DataModel dataModel, String name, Resource resource) {
        return new PrivilegeImpl(dataModel).init(name, resource);
    }



	PrivilegeImpl init(String name, Resource resource) {
		this.name = name;
        this.resource.set(resource);
		return this;
	}

  	@Override
	public String getName() {
		return name;
	}
	
	@Override
    public void delete() {
        dataModel.mapper(Privilege.class).remove(this);
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

        return (name.equals(privilege.getName()));

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "PrivilegeImpl{" +
                "name='" + name + '\'' +
                '}';
    }
}
