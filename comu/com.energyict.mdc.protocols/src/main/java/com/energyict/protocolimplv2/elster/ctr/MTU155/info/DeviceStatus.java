/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.info;

public enum DeviceStatus {

    TO_BE_CONFIGURED(0, "To be configured"),
    NORMAL(1, "Normal"),
    UNDER_MAINTENANCE(2, "Under maintenance"),
    RESERVED_3(3, "Reserved [3]"),
    RESERVED_4(4, "Reserved [4]"),
    RESERVED_5(5, "Reserved [5]"),
    RESERVED_6(6, "Reserved [6]"),
    RESERVED_7(7, "Reserved [7]"),
    RESERVED_8(8, "Reserved [8]"),
    AVAILABLE_9(9, "Available [9]"),
    AVAILABLE_10(10, "Available [10]"),
    AVAILABLE_11(11, "Available [11]"),
    AVAILABLE_12(12, "Available [12]"),
    AVAILABLE_13(13, "Available [13]"),
    AVAILABLE_14(14, "Available [14]"),
    RESERVED_15(15, "Reserved [8]"),
    UNKNOWN(-1, "Reserved");

    private final int statusCode;
    private final String description;

    /**
     * Private constructor for enum
     *
     * @param statusCode
     * @param description
     */
    private DeviceStatus(int statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }

    /**
     * Getter for the description
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for the int value of the device status code
     *
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get a DeviceStatus from a given statusCode. If the status code
     *
     * @param statusCode
     * @return
     */
    public static DeviceStatus fromStatusCode(int statusCode) {
        for (DeviceStatus status : DeviceStatus.values()) {
            if ((statusCode & 0x0F) == status.getStatusCode()) {
                return status;
            }
        }
        return UNKNOWN;
    }

}
