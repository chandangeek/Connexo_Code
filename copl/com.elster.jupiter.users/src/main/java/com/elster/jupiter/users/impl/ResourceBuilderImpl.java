/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.ResourceBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ResourceBuilderImpl implements ResourceBuilder {

    private String component;
    private String name;
    private String description;
    private List<PrivilegeBuilderImpl> privilegeBuilders = new ArrayList<>();
    private List<GrantPrivilegeBuilderImpl> grantPrivilegeBuilders = new ArrayList<>();
    private ResourceImpl resource;
    private final DataModel dataModel;

    ResourceBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ResourceBuilder component(String component) {
        this.component = component;
        return this;
    }

    @Override
    public ResourceBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ResourceBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public PrivilegeBuilder addPrivilege(String name) {
        return new PrivilegeBuilderImpl(name);
    }

    @Override
    public GrantPrivilegeBuilder addGrantPrivilege(String name) {
        return new GrantPrivilegeBuilderImpl(name);
    }

    @Override
    public Resource create() {
        resource = ResourceImpl.from(dataModel, component, name, description);
        resource.persist();
        privilegeBuilders.forEach(PrivilegeBuilderImpl::build);
        grantPrivilegeBuilders.forEach(GrantPrivilegeBuilderImpl::build);
        return resource;
    }

    private class PrivilegeBuilderImpl implements PrivilegeBuilder {

        private final String name;
        private PrivilegeCategory category;

        public PrivilegeBuilderImpl(String name) {
            this.name = name;
        }

        @Override
        public PrivilegeBuilder in(PrivilegeCategory category) {
            this.category = category;
            return this;
        }

        @Override
        public ResourceBuilder add() {
            privilegeBuilders.add(this);
            return ResourceBuilderImpl.this;
        }

        private void build() {
            if (category == null) {
                resource.createPrivilege(name);
                return;
            }
            resource.createPrivilege(name, category);
        }
    }

    private class GrantPrivilegeBuilderImpl implements GrantPrivilegeBuilder {

        private final String name;
        private PrivilegeCategory category;
        private Set<PrivilegeCategory> grantables = new HashSet<>();

        private GrantPrivilegeBuilderImpl(String name) {
            this.name = name;
        }

        @Override
        public GrantPrivilegeBuilder in(PrivilegeCategory category) {
            this.category = category;
            return this;
        }

        @Override
        public GrantPrivilegeBuilder forCategory(PrivilegeCategory category) {
            grantables.add(category);
            return this;
        }

        @Override
        public ResourceBuilder add() {
            grantPrivilegeBuilders.add(this);
            return ResourceBuilderImpl.this;
        }

        private void build() {
            Resource.GrantPrivilegeBuilder builder = resource.createGrantPrivilege(name)
                    .in(category);
            grantables.forEach(builder::forCategory);
            builder.create();
        }

    }
}
