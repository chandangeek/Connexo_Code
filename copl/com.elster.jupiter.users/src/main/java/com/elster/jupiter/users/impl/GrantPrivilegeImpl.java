/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class GrantPrivilegeImpl extends AbstractPrivilege implements GrantPrivilege {

    enum Fields {
        CATEGORIES("grantableCategories");

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    private List<GrantableCategory> grantableCategories = new ArrayList<>();

    @Inject
	GrantPrivilegeImpl(DataModel dataModel) {
        super(dataModel);
    }

    static GrantPrivilegeImpl from(DataModel dataModel, String name, Resource resource, PrivilegeCategory privilegeCategory) {
        return new GrantPrivilegeImpl(dataModel).init(name, resource, privilegeCategory);
    }

    void persist() {
		dataModel.mapper(Privilege.class).persist(this);
	}

    @Override
    GrantPrivilegeImpl init(String name, Resource resource, PrivilegeCategory privilegeCategory) {
        super.init(name, resource, privilegeCategory);
        return this;
    }

    @Override
    public Set<PrivilegeCategory> grantableCategories() {
        return grantableCategories.stream()
                .map(GrantableCategory::category)
                .collect(Collectors.toSet());
    }

    @Override
    public void addGrantableCategory(PrivilegeCategory category) {
        if (!grantableCategories().contains(category)) {
            grantableCategories.add(GrantableCategory.of(dataModel, this, category));
        }
    }
}
