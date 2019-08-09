/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.common.device.config.DeviceMessageUserAction;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement
public class DeviceMessagePrivilegeInfo {

    @XmlJavaTypeAdapter(DeviceMessageUserActionAdapter.class)
    public DeviceMessageUserAction privilege;
    public String name;
    public List<String> roles;

    public DeviceMessagePrivilegeInfo() {
    }

    static DeviceMessagePrivilegeInfo from(DeviceMessageUserAction userAction) {
        DeviceMessagePrivilegeInfo info = new DeviceMessagePrivilegeInfo();
        info.privilege = userAction;
        return info;
    }

    public static DeviceMessagePrivilegeInfo from(DeviceMessageUserAction userAction, Thesaurus thesaurus) {
        DeviceMessagePrivilegeInfo info = from(userAction);
        info.name = thesaurus.getFormat(DeviceMessageExecutionLevelTranslationKeys.from(userAction.getPrivilege())).format();
        return info;
    }

    public static DeviceMessagePrivilegeInfo from(DeviceMessageUserAction userAction, Collection<Group> groups, Thesaurus thesaurus) {
        DeviceMessagePrivilegeInfo info = DeviceMessagePrivilegeInfo.from(userAction, thesaurus);
        info.roles = new ArrayList<>();
        for (Group group : groups) {
           info.roles.add(group.getName());
        }
        return info;
    }

}