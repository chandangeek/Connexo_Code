/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

public class UsagePointLifeCycleStageInfoFactory {
    public IdWithNameInfo from(UsagePointStage stage) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = stage.getKey();
        info.name = stage.getDisplayName();

        return info;
    }
}
