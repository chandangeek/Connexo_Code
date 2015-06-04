package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;

import java.util.List;

/**
 * Created by bvn on 9/12/14.
 */
public class ExecutionLevelInfo {
    public String id;
    public String name;
    public List<IdWithNameInfo> userRoles;

    public ExecutionLevelInfo(String id, String name, List<IdWithNameInfo> userRoles) {
        this.id = id;
        this.name = name;
        this.userRoles = userRoles;
    }
}
