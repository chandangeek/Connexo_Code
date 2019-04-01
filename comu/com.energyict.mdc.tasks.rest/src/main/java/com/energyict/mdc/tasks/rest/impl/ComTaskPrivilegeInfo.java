/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.Group;

import com.energyict.mdc.tasks.ComTaskUserAction;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement
public class ComTaskPrivilegeInfo {

    @XmlJavaTypeAdapter(ComTaskUserActionAdapter.class)
    public ComTaskUserAction privilege;
    public String name;
    public List<String> roles;

    static ComTaskPrivilegeInfo from(ComTaskUserAction privilege) {
        ComTaskPrivilegeInfo info = new ComTaskPrivilegeInfo();
        info.privilege = privilege;
        return info;
    }

    public static ComTaskPrivilegeInfo from(ComTaskUserAction privilege, Thesaurus thesaurus) {
        ComTaskPrivilegeInfo info = from(privilege);
        info.name = thesaurus.getFormat(ComTaskExecutionLevelTranslationKeys.from(privilege.getPrivilege())).format();
        return info;
    }

    public static List<ComTaskPrivilegeInfo> from(Collection<ComTaskUserAction> privileges, Thesaurus thesaurus) {
        if (privileges == null) {
            throw new IllegalArgumentException("privileges can't be null");
        }
        return privileges.stream().map(privilege -> from(privilege, thesaurus)).collect(Collectors.toList());
    }

    public static ComTaskPrivilegeInfo from(ComTaskUserAction privilege, Collection<Group> groups,
            Thesaurus thesaurus) {
        ComTaskPrivilegeInfo info = ComTaskPrivilegeInfo.from(privilege, thesaurus);
        info.roles = new ArrayList<>();
        for (Group group : groups) {
            info.roles.add(group.getName());
        }
        return info;
    }

}