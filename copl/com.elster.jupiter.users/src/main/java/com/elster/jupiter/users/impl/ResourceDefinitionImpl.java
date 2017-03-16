/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.ResourceDefinition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lucian on 6/29/2015.
 */
public class ResourceDefinitionImpl implements ResourceDefinition {

    String name;
    String description;
    String module;
    String application;

    List<String> privilegeNames;
    List<Privilege> privileges;

    public ResourceDefinitionImpl() {
    }

    private ResourceDefinitionImpl initWithPrivilegeNames(String applicationName, String moduleName, String resourceName, String resourceDescription, List<String> privileges) {
        this.name = resourceName;
        this.description = resourceDescription;
        this.module = moduleName;
        this.application = applicationName;
        this.privilegeNames = privileges;

        return this;

    }

    private ResourceDefinitionImpl initWithPrivileges(String applicationName, String moduleName, String resourceName, String resourceDescription, List<Privilege> privileges) {
        this.name = resourceName;
        this.description = resourceDescription;
        this.module = moduleName;
        this.application = applicationName;
        this.privileges = privileges;

        return this;

    }

    public static ResourceDefinition createResourceDefinition(String moduleName, String resourceName, String resourceDescription, List<String> privileges){
        ResourceDefinitionImpl resource = new ResourceDefinitionImpl().initWithPrivilegeNames(null, moduleName, resourceName, resourceDescription, privileges);
        return resource;
    }

    public static Resource createApplicationResource(String applicationName, String moduleName, String resourceName, String resourceDescription, List<Privilege> privileges){
        ResourceDefinitionImpl resource = new ResourceDefinitionImpl().initWithPrivileges(applicationName, moduleName, resourceName, resourceDescription, privileges);
        return resource;
    }

    //public

    @Override
    public String getComponentName() {
        return module;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void delete() {

    }

    @Override
    public void createPrivilege(String name) {

    }

    @Override
    public void createPrivilege(String name, PrivilegeCategory category) {

    }

    @Override
    public List<Privilege> getPrivileges() {
        return privileges;
    }


    @Override
    public List<String> getPrivilegeNames() {
        return privilegeNames;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof ResourceDefinitionImpl && getName().equals(((ResourceDefinitionImpl) obj).getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public GrantPrivilegeBuilder createGrantPrivilege(String name) {
        return new GrantPrivilegeBuilderImpl(name);
    }

    private class GrantPrivilegeBuilderImpl implements GrantPrivilegeBuilder, GrantPrivilege {
        private final String name;
        private PrivilegeCategory category;
        private Set<PrivilegeCategory> categories = new HashSet<>();

        public GrantPrivilegeBuilderImpl(String name) {
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
            privileges.add(this);
            return this;
        }

        @Override
        public Set<PrivilegeCategory> grantableCategories() {
            return Collections.unmodifiableSet(categories);
        }

        @Override
        public void addGrantableCategory(PrivilegeCategory category) {
            categories.add(category);
        }

        @Override
        public void delete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrivilegeCategory getCategory() {
            return category;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
