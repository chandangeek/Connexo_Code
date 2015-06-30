package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.Resource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Lucian on 6/29/2015.
 */
public class ModuleResourceImpl implements Resource {

    String name;
    String description;
    String application;

    List<Privilege> privileges;

    public ModuleResourceImpl() {
    }

    private ModuleResourceImpl init(String applicationName, String resourceName, String resourceDescription, List<String> privileges) {
        this.name = resourceName;
        this.description = resourceDescription;
        this.application = applicationName;
        this.privileges = privileges
                .stream()
                .map(ModulePrivilegeImpl::from)
                .collect(Collectors.toList());

        return this;

    }

    public static ModuleResourceImpl from(String resourceName, String resourceDescription, List<String> privileges){
        ModuleResourceImpl resource = new ModuleResourceImpl().init(null, resourceName, resourceDescription,privileges);
        return resource;
    }

    public static ModuleResourceImpl from(String applicationName, String resourceName, String resourceDescription, List<String> privileges){
        ModuleResourceImpl resource = new ModuleResourceImpl().init(applicationName, resourceName, resourceDescription, privileges);
        return resource;
    }

    @Override
    public String getComponentName() {
        return application;
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
    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof ModuleResourceImpl && getName().equals(((ModuleResourceImpl) obj).getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
