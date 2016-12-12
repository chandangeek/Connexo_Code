package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.PrivilegeCategory;

import javax.inject.Inject;

class PrivilegeCategoryImpl implements PrivilegeCategory {

    enum Fields {
        NAME("name");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }
    }

    private String name;

    @Inject
    PrivilegeCategoryImpl() {
    }

    static PrivilegeCategoryImpl of(DataModel dataModel, String name) {
        return dataModel.getInstance(PrivilegeCategoryImpl.class).init(name);
    }

    private PrivilegeCategoryImpl init(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }
}
