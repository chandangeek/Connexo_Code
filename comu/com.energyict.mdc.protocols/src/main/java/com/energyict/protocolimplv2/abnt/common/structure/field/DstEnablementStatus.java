package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class DstEnablementStatus extends AbstractField<DstEnablementStatus> {

    public static final int LENGTH = 1;

    private int statusCode;
    private EnablementStatus enablementStatus;

    public DstEnablementStatus() {
        this.enablementStatus = EnablementStatus.UNKNOWN;
    }

    public DstEnablementStatus(EnablementStatus enablementStatus) {
        this.enablementStatus = enablementStatus;
        this.statusCode = enablementStatus.getStatusCode();
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(statusCode, LENGTH);
    }

    @Override
    public DstEnablementStatus parse(byte[] rawData, int offset) throws ParsingException {
        statusCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        enablementStatus = EnablementStatus.fromConditionCode(statusCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        if (this.enablementStatus.equals(EnablementStatus.UNKNOWN)) {
            return (enablementStatus.getMessage() + " " + statusCode);
        }
        return enablementStatus.getMessage();
    }

    public EnablementStatus getStatus() {
        return enablementStatus;
    }

    public static DstEnablementStatus fromStatusCode(int conditionCode) {
        for (EnablementStatus enablementStatus : EnablementStatus.values()) {
            if (enablementStatus.getStatusCode() == conditionCode) {
                return new DstEnablementStatus(enablementStatus);
            }
        }
        return new DstEnablementStatus(EnablementStatus.UNKNOWN);


    }

    public enum EnablementStatus {
        DISABLED(0, "Disabled"),
        ENABLED(1, "Enabled"),
        UNKNOWN(-1, "Unknown DST enablement status");

        private final int statusCode;
        private final String message;

        private EnablementStatus(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getMessage() {
            return message;
        }

        public static EnablementStatus fromConditionCode(int conditionCode) {
            for (EnablementStatus version : EnablementStatus.values()) {
                if (version.getStatusCode() == conditionCode) {
                    return version;
                }
            }
            return EnablementStatus.UNKNOWN;
        }
    }
}