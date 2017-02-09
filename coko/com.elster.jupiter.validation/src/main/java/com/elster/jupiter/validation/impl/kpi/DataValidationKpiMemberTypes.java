/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

enum DataQualityKpiMemberTypes {
    CHANNEL("CHANNEL_"),
    REGISTER("REGISTER_"),
    SUSPECT("SUSPECT_"),
    MISSINGVALUESVALIDATOR("MISSINGVALUESVALIDATOR_"),
    INFORMATIVE("INFORMATIVE_"),
    ADDED("ADDED_"),
    EDITED("EDITED_"),
    REMOVED("REMOVED_"),
    ESTIMATED("ESTIMATED_"),
    CONFIRMED("CONFIRMED_");

    private final String javaFieldName;

    DataQualityKpiMemberTypes(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    public String fieldName() {
        return javaFieldName;
    }
}
