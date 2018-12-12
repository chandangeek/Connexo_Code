/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.PrivilegeCategory;

import javax.inject.Inject;
import java.util.Objects;

class GrantableCategory {

    enum Fields {
        CATEGORY("category"),
        GRANT_PRIVILEGE("grantPrivilege")
        ;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    private Reference<PrivilegeCategory> category = ValueReference.absent();
    private Reference<GrantPrivilege> grantPrivilege = ValueReference.absent();

    @Inject
    GrantableCategory() {
    }

    static GrantableCategory of(DataModel dataModel, GrantPrivilege grantPrivilege, PrivilegeCategory privilegeCategory) {
        return dataModel.getInstance(GrantableCategory.class).init(grantPrivilege, privilegeCategory);
    }

    private GrantableCategory init(GrantPrivilege grantPrivilege, PrivilegeCategory privilegeCategory) {
        this.category.set(privilegeCategory);
        this.grantPrivilege.set(grantPrivilege);
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GrantableCategory that = (GrantableCategory) o;
        return Objects.equals(category.get(), that.category.get()) &&
                Objects.equals(grantPrivilege.get(), that.grantPrivilege.get());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(category.get(), grantPrivilege.get());
    }

    public PrivilegeCategory category() {
        return category.get();
    }
}
