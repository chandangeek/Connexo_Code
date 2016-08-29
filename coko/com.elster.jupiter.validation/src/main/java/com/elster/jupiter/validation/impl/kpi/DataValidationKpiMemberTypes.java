package com.elster.jupiter.validation.impl.kpi;

enum DataValidationKpiMemberTypes {
    SUSPECT("SUSPECT_"),
    REGISTER("REGISTER_"),
    CHANNEL("CHANNEL_"),
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
