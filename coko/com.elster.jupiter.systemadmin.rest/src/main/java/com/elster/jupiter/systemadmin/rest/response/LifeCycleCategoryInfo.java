package com.elster.jupiter.systemadmin.rest.response;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LifeCycleCategoryInfo {
    public String kind;
    public String name;
    public int retainedPartitionCount;
    public int retention;

    public LifeCycleCategoryInfo(){}

    public LifeCycleCategoryInfo(LifeCycleCategory category){
        this.kind = category.getKind().name();
        this.name = category.getName();
        this.retainedPartitionCount = category.getRetainedPartitionCount();
        this.retention = category.getRetention().getDays();
    }
}
