package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;

public class UsagePointTriggerValidationInfo {

    public String id;
    public String name;
    public Long lastChecked;
    public long version;
    public VersionInfo<String> parent;
}
