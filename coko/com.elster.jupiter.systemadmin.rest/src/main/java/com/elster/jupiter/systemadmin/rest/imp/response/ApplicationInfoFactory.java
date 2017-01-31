/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.system.Subsystem;

public class ApplicationInfoFactory {

    public ApplicationInfo asInfo(Subsystem subsystem) {
        ApplicationInfo info = new ApplicationInfo();
        info.id = subsystem.getId();
        info.name = subsystem.getName();
        info.version = subsystem.getVersion();
        return info;
    }
}
