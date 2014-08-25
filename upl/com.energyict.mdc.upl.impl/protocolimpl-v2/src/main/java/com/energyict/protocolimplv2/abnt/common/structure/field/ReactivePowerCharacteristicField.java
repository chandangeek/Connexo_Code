package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class ReactivePowerCharacteristicField extends AbstractField<ReactivePowerCharacteristicField> {

    public static final int LENGTH = 1;

    private char installationStatusCode;
    private ReactivePowerCharacteristic reactivePowerCharacteristic;

    public ReactivePowerCharacteristicField() {
        this.reactivePowerCharacteristic = ReactivePowerCharacteristic.UNKNOWN;
    }

    public ReactivePowerCharacteristicField(ReactivePowerCharacteristic reactivePowerCharacteristic) {
        this.reactivePowerCharacteristic = reactivePowerCharacteristic;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(installationStatusCode, LENGTH);
    }

    @Override
    public ReactivePowerCharacteristicField parse(byte[] rawData, int offset) throws ParsingException {
        installationStatusCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        reactivePowerCharacteristic = ReactivePowerCharacteristic.fromStatusCode(installationStatusCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getInstallationStatusCode() {
        return installationStatusCode;
    }

    public String getInstallationStatusInfo() {
        if (!this.reactivePowerCharacteristic.equals(ReactivePowerCharacteristic.UNKNOWN)) {
            return reactivePowerCharacteristic.getStatusInfo();
        } else {
            return (reactivePowerCharacteristic.getStatusInfo() + " " + reactivePowerCharacteristic);
        }
    }

    public ReactivePowerCharacteristic getReactivePowerCharacteristic() {
        return reactivePowerCharacteristic;
    }

    public enum ReactivePowerCharacteristic {
        CAPACITIVE("C", "Capacitive"),
        MONO_PHASE("I", "Inductive"),
        UNKNOWN("0", "Unknown reactive power characteristic");

        private final String statusCode;
        private final String statusInfo;

        private ReactivePowerCharacteristic(String statusCode, String statusInfo) {
            this.statusCode = statusCode;
            this.statusInfo = statusInfo;
        }

        public String getStatusInfo() {
            return statusInfo;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public static ReactivePowerCharacteristic fromStatusCode(int statusCode) {
            for (ReactivePowerCharacteristic version : ReactivePowerCharacteristic.values()) {
                if (version.getStatusCode().equals(statusCode)) {
                    return version;
                }
            }
            return ReactivePowerCharacteristic.UNKNOWN;
        }
    }
}