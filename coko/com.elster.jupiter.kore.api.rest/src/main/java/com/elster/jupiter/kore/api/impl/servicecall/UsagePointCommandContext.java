/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.metering.UsagePoint;

public class UsagePointCommandContext {
    private UsagePoint usagePoint;
    private UsagePointCommandInfo usagePointCommandInfo;
    private CommandHelper commandHelper;

    public UsagePointCommandContext(UsagePoint usagePoint, UsagePointCommandInfo usagePointCommandInfo, CommandHelper commandHelper) {
        this.usagePoint = usagePoint;
        this.usagePointCommandInfo = usagePointCommandInfo;
        this.commandHelper = commandHelper;
    }
    public UsagePoint getUsagePoint() {
        return usagePoint;
    }

    public UsagePointCommandInfo getUsagePointCommandInfo() {
        return usagePointCommandInfo;
    }

    public CommandHelper getCommandHelper() {
        return commandHelper;
    }
}
