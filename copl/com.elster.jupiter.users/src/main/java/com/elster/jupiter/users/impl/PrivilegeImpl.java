/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;

import javax.inject.Inject;

final class PrivilegeImpl extends AbstractPrivilege {

    @Inject
	private PrivilegeImpl(DataModel dataModel) {
        super(dataModel);
    }

    static PrivilegeImpl from(DataModel dataModel, String name, Resource resource, PrivilegeCategory privilegeCategory) {
        return new PrivilegeImpl(dataModel).init(name, resource, privilegeCategory);
    }

    void persist() {
		dataModel.mapper(Privilege.class).persist(this);
	}

    @Override
    PrivilegeImpl init(String name, Resource resource, PrivilegeCategory privilegeCategory) {
        super.init(name, resource, privilegeCategory);
        return this;
    }
}
