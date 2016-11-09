package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.rest.util.VersionInfo;

import java.util.ArrayList;
import java.util.List;

public class UsagePointLifeCycleStateInfo {
    public long id;
    public long version;
    public String name;
    public boolean isInitial;
    public List<BusinessProcessInfo> onEntry = new ArrayList<>();
    public List<BusinessProcessInfo> onExit = new ArrayList<>();
    public VersionInfo<Long> parent;
}
