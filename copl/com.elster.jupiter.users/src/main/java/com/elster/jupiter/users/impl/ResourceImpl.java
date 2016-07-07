package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

final class ResourceImpl implements Resource {
    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private String componentName;
    private String name;
    private String description;
    @SuppressWarnings("unused")
    private Instant createTime;
    private final DataModel dataModel;

    private List<Privilege> privileges;

    @Inject
    private ResourceImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static ResourceImpl from(DataModel dataModel, String componentName, String name, String description) {
        return new ResourceImpl(dataModel).init(componentName, name, description);
    }

    ResourceImpl init(String componentName, String name, String description) {
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
    public void createPrivilege(String name) {
        if (getPrivileges().stream().map(Privilege::getName).noneMatch(s -> Objects.equals(s, name))) {
            PrivilegeImpl result = PrivilegeImpl.from(dataModel, name, this);
            result.persist();
            doGetPrivileges().add(result);
        }
    }

    @Override
    public List<Privilege> getPrivileges() {
        return Collections.unmodifiableList(doGetPrivileges());
    }

    private List<Privilege> doGetPrivileges() {
        if (privileges == null) {
            privileges = new CopyOnWriteArrayList<>(dataModel.mapper(Privilege.class).find("resource", this));
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
