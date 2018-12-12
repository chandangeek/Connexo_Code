/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;

final class ResourceImpl implements Resource {
    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @Size(max = 3, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_3 + "}")
    private String componentName;
    @Size(max = NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    @Size(max = SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_256 + "}")
    private String description;
    @SuppressWarnings("unused")
    private Instant createTime;
    private final DataModel dataModel;
    private final UserService userService;

    private List<Privilege> privileges;

    @Inject
    ResourceImpl(DataModel dataModel, UserService userService) {
        this.dataModel = dataModel;
        this.userService = userService;
    }

    static ResourceImpl from(DataModel dataModel, String componentName, String name, String description) {
        return dataModel.getInstance(ResourceImpl.class).init(componentName, name, description);
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
        createPrivilege(name, userService.getDefaultPrivilegeCategory());
    }

    @Override
    public void createPrivilege(String name, PrivilegeCategory category) {
        if (getPrivileges().stream().map(Privilege::getName).noneMatch(s -> Objects.equals(s, name))) {
            PrivilegeImpl result = PrivilegeImpl.from(dataModel, name, this, category);
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

    @Override
    public GrantPrivilegeBuilder createGrantPrivilege(String name) {
        return new GrantPrivilegeBuilderImpl(dataModel, userService, this, name);
    }

    class GrantPrivilegeBuilderImpl implements GrantPrivilegeBuilder {

        private final DataModel dataModel;
        private final UserService userService;
        private final Resource resource;
        private final String name;
        private PrivilegeCategory category;
        private final Set<PrivilegeCategory> categories = new HashSet<>();

        GrantPrivilegeBuilderImpl(DataModel dataModel, UserService userService, Resource resource, String name) {
            this.dataModel = dataModel;
            this.userService = userService;
            this.resource = resource;
            this.name = name;
        }

        @Override
        public GrantPrivilegeBuilder in(PrivilegeCategory category) {
            this.category = category;
            return this;
        }

        @Override
        public GrantPrivilegeBuilder forCategory(PrivilegeCategory privilegeCategory) {
            categories.add(privilegeCategory);
            return this;
        }

        @Override
        public GrantPrivilege create() {
            GrantPrivilegeImpl grantPrivilege = GrantPrivilegeImpl.from(dataModel, name, resource, getCategory());
            categories.forEach(grantPrivilege::addGrantableCategory);
            dataModel.mapper(Privilege.class).persist(grantPrivilege);
            doGetPrivileges().add(grantPrivilege);
            return grantPrivilege;
        }

        private PrivilegeCategory getCategory() {
            return category == null ? userService.getDefaultPrivilegeCategory() : category;
        }
    }
}
