package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.ResourceDefinition;

import java.util.List;

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
}
