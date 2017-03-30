/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime;

public enum PrimeManufacturer {

    ZIV("ZIV"),
    ELSTER("ELS"),
    CIRCUTOR("CIR"),
    LANDIS_GYR("LGZ");

    private final String manufacturerId;

    private PrimeManufacturer(final String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    /**
     * @return The 3 char manufacturer id
     */
    public String getManufacturerId() {
        return manufacturerId;
    }

    /**
     * Check if a given serial number matches this {@link PrimeManufacturer}
     *
     * @param serialNumber The serial number to check against the manufacturer id
     * @return True if the serial number matches this manufacturer
     */
    public final boolean matches(final String serialNumber) {
        if (serialNumber != null) {
            return serialNumber.toUpperCase().startsWith(getManufacturerId());
        }
        return false;
    }

}
