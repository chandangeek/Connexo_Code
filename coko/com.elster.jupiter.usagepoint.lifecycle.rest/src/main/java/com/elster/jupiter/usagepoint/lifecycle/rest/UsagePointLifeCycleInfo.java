/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointLifeCycleInfo {
    public long id;
    public String name;
    public long version;
    public boolean isDefault;
    public boolean obsolete;
    public List<UsagePointLifeCycleStateInfo> states;
    public int transitionsCount;
}
