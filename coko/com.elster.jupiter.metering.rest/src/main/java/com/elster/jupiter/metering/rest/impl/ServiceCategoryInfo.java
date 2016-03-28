package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.nls.Thesaurus;

public class ServiceCategoryInfo {

    public String name;
    public String displayName;

    public ServiceCategoryInfo(ServiceCategory category) {
        this.name = category.getKind().name();
        this.displayName = category.getName();
    }

    public ServiceCategoryInfo(ServiceCategory category, Thesaurus thesaurus) {
        this.name = category.getKind().name();
        this.displayName = thesaurus.getString(category.getTranslationKey(), category.getName());
    }
}
