/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class BatteryStatusField extends AbstractField<BatteryStatusField> {

    public static final int LENGTH = 1;

    private int statusCode;
    private BatteryStatus batteryStatus;

    public BatteryStatusField() {
        this.batteryStatus = BatteryStatus.UNKNOWN;
    }

    public BatteryStatusField(BatteryStatus batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(statusCode, LENGTH);
    }

    @Override
    public BatteryStatusField parse(byte[] rawData, int offset) throws ParsingException {
        statusCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        batteryStatus = BatteryStatus.fromStatusCode(statusCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBatteryStatusMessage() {
        if (!this.batteryStatus.equals(BatteryStatus.UNKNOWN)) {
            return batteryStatus.getMessage();
        } else {
            return (batteryStatus.getMessage() + " " + statusCode);
        }
    }

    public BatteryStatus getBatteryStatus() {
        return batteryStatus;
    }

    public enum BatteryStatus {
        OK(0, "Battery good"),
        PROBLEM(1, "Battery problem"),
        UNKNOWN(-1, "Unknown battery status code");

        private final int statusCode;
        private final String message;

        private BatteryStatus(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getMessage() {
            return message;
        }

        public static BatteryStatus fromStatusCode(int statusCode) {
            for (BatteryStatus version : BatteryStatus.values()) {
                if (version.getStatusCode() == statusCode) {
                    return version;
                }
            }
            return BatteryStatus.UNKNOWN;
        }
    }
}