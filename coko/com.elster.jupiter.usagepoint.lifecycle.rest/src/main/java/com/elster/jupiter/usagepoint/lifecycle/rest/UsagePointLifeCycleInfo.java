package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointLifeCycleInfo {
    public long id;
    public String name;
    public long version;
    public boolean isDefault;
}
