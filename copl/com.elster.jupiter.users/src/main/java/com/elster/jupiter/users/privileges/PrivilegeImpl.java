/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.privileges;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public final class PrivilegeImpl extends AbstractPrivilege {

    public PrivilegeImpl() {
    }

    @Inject
	private PrivilegeImpl(DataModel dataModel) {
        super(dataModel);
    }

    public static PrivilegeImpl from(DataModel dataModel, String name, Resource resource, PrivilegeCategory privilegeCategory) {
        return new PrivilegeImpl(dataModel).init(name, resource, privilegeCategory);
    }

    public void persist() {
		dataModel.mapper(Privilege.class).persist(this);
	}

    @Override
    protected PrivilegeImpl init(String name, Resource resource, PrivilegeCategory privilegeCategory) {
        super.init(name, resource, privilegeCategory);
        return this;
    }
}
