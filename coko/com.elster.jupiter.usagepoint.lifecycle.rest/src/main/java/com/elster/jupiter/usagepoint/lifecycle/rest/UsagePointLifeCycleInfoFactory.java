package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;

public class UsagePointLifeCycleInfoFactory {

    public UsagePointLifeCycleInfo from(UsagePointLifeCycle lifeCycle) {
        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = lifeCycle.getId();
        info.name = lifeCycle.getName();
        info.version = lifeCycle.getVersion();
        return info;
    }

    public UsagePointLifeCycleInfo fullInfo(UsagePointLifeCycle lifeCycle) {
        UsagePointLifeCycleInfo info = from(lifeCycle);
        return info;
    }
}
