/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.inject.Inject;

public class UsagePointLifeCyclePrivilegeInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public UsagePointLifeCyclePrivilegeInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public UsagePointLifeCyclePrivilegeInfo from(UsagePointTransition.Level level) {
        UsagePointLifeCyclePrivilegeInfo info = new UsagePointLifeCyclePrivilegeInfo();
        info.privilege = level.name();
        info.name = thesaurus.getString(level.getPrivilege(), info.privilege);
        return info;
    }
}
