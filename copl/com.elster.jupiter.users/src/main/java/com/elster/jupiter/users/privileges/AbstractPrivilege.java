/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.privileges;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.Resource;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;

@XmlRootElement
public class AbstractPrivilege implements Privilege {
    protected DataModel dataModel;
    // persistent fields

    @Size(max = NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    private Reference<Resource> resource = ValueReference.absent();

    private Reference<PrivilegeCategory> category = ValueReference.absent();

    public AbstractPrivilege() {
    }

    public AbstractPrivilege(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    @XmlAttribute
    public String getName() {
        return name;
    }

    @Override
    public void delete() {
        this.deleteUsagesInGroup();
        dataModel.mapper(Privilege.class).remove(this);
    }

    @Override
    @XmlElement(type = PrivilegeCategoryImpl.class)
    public PrivilegeCategory getCategory() {
        return category.get();
    }

    public void setCategory(PrivilegeCategory category) {
        this.category.set(category);
    }

    private void deleteUsagesInGroup() {
        this.dataModel
                .mapper(PrivilegeInGroup.class)
                .find("privilegeName", this.getName())
                .forEach(PrivilegeInGroup::delete);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Privilege)) {
            return false;
        }

        Privilege privilege = (Privilege) o;

        return (name.equals(privilege.getName()));

    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "PrivilegeImpl{" +
                "name='" + name + '\'' +
                '}';
    }

    protected Privilege init(String name, Resource resource, PrivilegeCategory privilegeCategory) {
        this.name = name;
        this.resource.set(resource);
        this.category.set(privilegeCategory);
        return this;
    }
}
