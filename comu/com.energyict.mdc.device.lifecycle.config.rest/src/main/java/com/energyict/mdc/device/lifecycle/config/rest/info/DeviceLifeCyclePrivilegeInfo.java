/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCyclePrivilegeInfo {
    public String privilege;
    public String name;

    public DeviceLifeCyclePrivilegeInfo() {}

    public DeviceLifeCyclePrivilegeInfo(Thesaurus thesaurus, AuthorizedAction.Level level){
        this.privilege = level.name();
        this.name = thesaurus.getString("privilege.level." + this.privilege, this.privilege);
    }
}
