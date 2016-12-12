package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.UserService;

import java.util.HashSet;
import java.util.Set;

class GrantPrivilegeBuilderImpl implements Resource.GrantPrivilegeBuilder {

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
    public Resource.GrantPrivilegeBuilder in(PrivilegeCategory category) {
        this.category = category;
        return this;
    }

    @Override
    public Resource.GrantPrivilegeBuilder forCategory(PrivilegeCategory privilegeCategory) {
        categories.add(privilegeCategory);
        return this;
    }

    @Override
    public GrantPrivilege create() {
        GrantPrivilegeImpl grantPrivilege = GrantPrivilegeImpl.from(dataModel, name, resource, getCategory());
        categories.forEach(grantPrivilege::addGrantableCategory);
        dataModel.mapper(Privilege.class).persist(grantPrivilege);
        return grantPrivilege;
    }

    private PrivilegeCategory getCategory() {
        return category == null ? userService.getDefaultPrivilegeCategory() : category;
    }
}
