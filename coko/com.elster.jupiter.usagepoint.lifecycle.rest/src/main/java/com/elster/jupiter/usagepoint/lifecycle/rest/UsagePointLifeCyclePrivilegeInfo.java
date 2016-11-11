package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointLifeCyclePrivilegeInfo {
    public String privilege;
    public String name;
}
