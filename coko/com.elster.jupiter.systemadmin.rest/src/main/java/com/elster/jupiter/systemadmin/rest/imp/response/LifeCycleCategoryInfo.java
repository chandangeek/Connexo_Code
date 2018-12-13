/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LifeCycleCategoryInfo {
    public String kind;
    public String name;
    public int retainedPartitionCount;
    public int retention;
    public long version;

    public LifeCycleCategoryInfo() {}

    public LifeCycleCategoryInfo(LifeCycleCategory category) {
        this();
        this.kind = category.getKind().name();
        this.name = category.getDisplayName();
        this.retainedPartitionCount = category.getRetainedPartitionCount();
        this.retention = category.getRetention().getDays();
        this.version = category.getVersion();
    }

}