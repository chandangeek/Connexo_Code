/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.security;

import com.elster.jupiter.nls.TranslationKey;


public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_QUALITY("data.quality", "Data quality"),
    RESOURCE_QUALITY_DESCRIPTION("data.quality.description", "Manage data quality kpi"),

    //Privileges
    ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION(Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, "Administer"),
    VIEW_DATA_QUALITY_KPI_CONFIGURATION(Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION, "View KPIs"),
    VIEW_DATA_QUALITY_RESULTS(Constants.VIEW_DATA_QUALITY_RESULTS, "View results");

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public interface Constants {

        String ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION = "privilege.administer.dataQualityKpi";
        String VIEW_DATA_QUALITY_KPI_CONFIGURATION = "privilege.view.dataQualityKpi";
        String VIEW_DATA_QUALITY_RESULTS = "privilege.view.dataQualityResults";

    }
}
