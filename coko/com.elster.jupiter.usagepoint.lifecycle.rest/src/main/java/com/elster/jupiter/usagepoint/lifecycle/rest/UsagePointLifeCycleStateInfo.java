/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointLifeCycleStateInfo {
    public long id;
    public long version;
    public String name;
    public boolean isInitial;
    public List<BusinessProcessInfo> onEntry = new ArrayList<>();
    public List<BusinessProcessInfo> onExit = new ArrayList<>();
    public VersionInfo<Long> parent;
    public UsagePointStage.Key stage;
}
