/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.rest.util.VersionInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsagePointLifeCycleTransitionInfo {
    public long id;
    public String name;
    public long version;
    public UsagePointLifeCycleStateInfo fromState;
    public UsagePointLifeCycleStateInfo toState;
    public List<UsagePointLifeCyclePrivilegeInfo> privileges = new ArrayList<>();
    public Set<MicroActionAndCheckInfo> microActions = new HashSet<>();
    public Set<MicroActionAndCheckInfo> microChecks = new HashSet<>();
    public VersionInfo<Long> parent;
}
