package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MessageSeeds;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceLifeCyclePrivilegeInfo {
    public String privilege;
    public String name;

    public DeviceLifeCyclePrivilegeInfo() {}

    public DeviceLifeCyclePrivilegeInfo(Thesaurus thesaurus, AuthorizedAction.Level level){
        this.privilege = level.name();
        this.name = thesaurus.getString(MessageSeeds.Keys.PRIVILEGE_LEVEL_TRANSLATE_KEY + this.privilege, this.privilege);
    }
}
