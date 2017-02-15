/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointGroupInfo {
    public long id;
    public String mRID;
    public String name;
    public long version;
    public boolean dynamic;
    public String filter;
    public List<Long> usagePoints;
}
