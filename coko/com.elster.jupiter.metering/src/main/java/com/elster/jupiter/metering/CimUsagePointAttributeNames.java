package com.elster.jupiter.metering;

public enum CimUsagePointAttributeNames {
    CIM_USAGE_POINT_NAME("CimUsagePointName"),
    CIM_USAGE_POINT_MR_ID("CimUsagePointMRID");

    private final String attributeName;

    CimUsagePointAttributeNames(String nameType) {
        this.attributeName = nameType;
    }

    public String getAttributeName() {
        return attributeName;
    }
}

