/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

enum DataValidationKpiMemberTypes {
    CHANNEL("CHANNEL_"),
    REGISTER("REGISTER_"),
    SUSPECT("SUSPECT_"),
    ALLDATAVALIDATED("ALLDATAVALIDATED_"),
    THRESHOLDVALIDATOR("THRESHOLDVALIDATOR_"),
    MISSINGVALUESVALIDATOR("MISSINGVALUESVALIDATOR_"),
    READINGQUALITIESVALIDATOR("READINGQUALITIESVALIDATOR_"),
    REGISTERINCREASEVALIDATOR("REGISTERINCREASEVALIDATOR_");

    private final String javaFieldName;

    DataValidationKpiMemberTypes(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    public String fieldName() {
        return javaFieldName;
    }
}
