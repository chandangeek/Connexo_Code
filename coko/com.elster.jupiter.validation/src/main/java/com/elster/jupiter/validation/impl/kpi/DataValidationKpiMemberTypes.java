package com.elster.jupiter.validation.impl.kpi;

public enum DataValidationKpiMemberTypes {

    SUSPECT("SUSPECT_"),
    REGISTER("REGISTER_"),
    CHANNEL("CHANNEL_"),
    ALLDATAVALIDATED("ALLDATAVALIDATED_");

    private final String javaFieldName;

    DataValidationKpiMemberTypes(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    public String fieldName() {
        return javaFieldName;
    }
}
