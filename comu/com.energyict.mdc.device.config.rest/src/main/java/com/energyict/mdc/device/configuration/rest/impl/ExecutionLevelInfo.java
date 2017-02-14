/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by bvn on 9/12/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionLevelInfo {
    public String id;
    public String name;
    public List<IdWithNameInfo> userRoles;
    public VersionInfo<Long> parent;

    public ExecutionLevelInfo() {
    }
}
