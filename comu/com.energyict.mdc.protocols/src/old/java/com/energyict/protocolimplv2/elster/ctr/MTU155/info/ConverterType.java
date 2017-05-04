/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.info;

public enum ConverterType {

    VOL1("Type 1 vol"),
    VOL2("Type 2 vol"),
    VEN1("Type 1 ven"),
    VEN2("Type 2 ven"),
    INVALID("Invalid converter type"),
    UNKNOWN("Unknown converter type");

    /**
     * A human readable description of the ConverterType
     */
    private final String description;

    private ConverterType(String description) {
        this.description = description;
    }

    /**
     * Get a ConverterType from a given string.
     *
     * @param typeName the name of the ConverterType
     * @return the ConverterType or INVALID if not found
     */
    public static ConverterType fromString(String typeName) {
        if ((typeName != null) && (typeName.length() > 0)) {
            for (ConverterType type : values()) {
                if (type.name().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
        }
        return INVALID;
    }

    /**
     * This method checks if the ConverterType is a valid value.
     * With a valid value we mean a value that could be read or written to a device.
     * Not valid: INVALID and UNKNOWN
     *
     * @return true if valid
     */
    public boolean isValid() {
        return !(equals(INVALID) || equals(UNKNOWN));
    }
}
