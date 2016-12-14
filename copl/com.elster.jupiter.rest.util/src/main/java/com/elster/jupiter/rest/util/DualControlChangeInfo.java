package com.elster.jupiter.rest.util;

public class DualControlChangeInfo {
    public String attributeName;
    public String originalValue;
    public String newValue;

    public DualControlChangeInfo() {}

    public DualControlChangeInfo(String attributeName, String originalValue, String newValue) {
        this.attributeName = attributeName;
        this.originalValue = originalValue;
        this.newValue = newValue;
    }
}
