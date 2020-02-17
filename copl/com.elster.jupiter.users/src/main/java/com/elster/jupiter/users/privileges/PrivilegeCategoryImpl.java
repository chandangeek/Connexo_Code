/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.privileges;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.PrivilegeCategory;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement
public class PrivilegeCategoryImpl implements PrivilegeCategory {

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

    public static PrivilegeCategoryImpl of(DataModel dataModel, String name) {
        return dataModel.getInstance(PrivilegeCategoryImpl.class).init(name);
    }

    private PrivilegeCategoryImpl init(String name) {
        this.name = name;
        return this;
    }

    @Override
    @XmlAttribute
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrivilegeCategoryImpl that = (PrivilegeCategoryImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
