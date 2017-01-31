/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

import javax.inject.Inject;
import java.util.Objects;

public class DeviceLifeCyclePrivilegeFactory {

    private final Thesaurus thesaurus;

    @Inject
    public DeviceLifeCyclePrivilegeFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceLifeCyclePrivilegeInfo from(AuthorizedAction.Level level){
        Objects.requireNonNull(level);
        return new DeviceLifeCyclePrivilegeInfo(thesaurus, level);
    }
}
