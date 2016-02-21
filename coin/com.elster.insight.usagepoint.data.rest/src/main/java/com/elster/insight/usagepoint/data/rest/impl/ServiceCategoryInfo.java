package com.elster.insight.usagepoint.data.rest.impl;


import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;

import java.util.List;

public class ServiceCategoryInfo {
    public ServiceKind serviceCategory;
    public List<CustomPropertySetInfo> customPropertySets;

    public ServiceCategoryInfo() {
    }

    public ServiceCategoryInfo(ServiceCategory serviceCategory, List<CustomPropertySetInfo> customPropertySets) {
        this.serviceCategory = serviceCategory.getKind();
        this.customPropertySets = customPropertySets;
    }
}
