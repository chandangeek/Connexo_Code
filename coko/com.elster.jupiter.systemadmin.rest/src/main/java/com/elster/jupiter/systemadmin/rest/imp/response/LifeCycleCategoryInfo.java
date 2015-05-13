package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.nls.Thesaurus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LifeCycleCategoryInfo {
    public String kind;
    public String name;
    public int retainedPartitionCount;
    public int retention;

    public LifeCycleCategoryInfo(){}

    public LifeCycleCategoryInfo(LifeCycleCategory category, Thesaurus thesaurus){
        this.kind = category.getKind().name();
        this.name = thesaurus.getStringBeyondComponent(category.getTranslationKey(), category.getName());
        this.retainedPartitionCount = category.getRetainedPartitionCount();
        this.retention = category.getRetention().getDays();
    }
}
