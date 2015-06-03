package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by bvn on 9/12/14.
 */
public class ExecutionLevelInfo {
    public String id;
    public String name;
    public List<IdWithNameInfo> userRoles;

    @JsonCreator
    public ExecutionLevelInfo(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("userRoles")  List<IdWithNameInfo> userRoles) {
        this.id = id;
        this.name = name;
        this.userRoles = userRoles;
    }
}
