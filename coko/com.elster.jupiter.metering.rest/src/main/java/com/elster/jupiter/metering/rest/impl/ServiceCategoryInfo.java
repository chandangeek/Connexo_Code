package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ServiceCategory;

public class ServiceCategoryInfo {

    public String name;
    public String displayName;

    public ServiceCategoryInfo(ServiceCategory category) {
        this.name = category.getKind().name();
        this.displayName = category.getName();
    }
}
