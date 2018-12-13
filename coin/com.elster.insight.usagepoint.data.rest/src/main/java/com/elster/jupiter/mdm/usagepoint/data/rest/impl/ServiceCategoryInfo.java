/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;

public class ServiceCategoryInfo {
    public ServiceKind name;
    public String displayName;
    public List<CustomPropertySetInfo> customPropertySets;

    public ServiceCategoryInfo() {
    }

    public ServiceCategoryInfo(ServiceCategory serviceCategory, List<CustomPropertySetInfo> customPropertySets, Thesaurus thesaurus) {
        this();
        this.name = serviceCategory.getKind();
        this.displayName = serviceCategory.getDisplayName();
        this.customPropertySets = customPropertySets;
    }

}