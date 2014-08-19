package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;
import java.util.List;

public class ResourceImpl implements Resource {
    // persistent fields
    private long id;
    private String componentName;
    private String name;
    private String description;
    @SuppressWarnings("unused")
    private UtcInstant createTime;
    private final DataModel dataModel;

    private List<Privilege> privileges;

    @Inject
    private ResourceImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static ResourceImpl from(DataModel dataModel, String componentName , String name , String description) {
        return new ResourceImpl(dataModel).init(componentName, name, description);
    }

    ResourceImpl init(String componentName , String name , String description) {
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
    public void delete() {
        dataModel.mapper(Resource.class).remove(this);
    }

    @Override
    public Privilege createPrivilege(String name) {
        PrivilegeImpl result = PrivilegeImpl.from(dataModel, name, this);
        result.persist();

        return result;
    }

    @Override
    public List<Privilege> getPrivileges() {
        if (privileges == null) {
            privileges = dataModel.mapper(Privilege.class).find("resource", this);
        }
        return privileges;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    void persist() {
        dataModel.mapper(Resource.class).persist(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Resource)) {
            return false;
        }

        Resource resource = (Resource) o;

        return name.equals(resource.getName());

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ResourceImpl{" +
                "componentName='" + componentName + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
