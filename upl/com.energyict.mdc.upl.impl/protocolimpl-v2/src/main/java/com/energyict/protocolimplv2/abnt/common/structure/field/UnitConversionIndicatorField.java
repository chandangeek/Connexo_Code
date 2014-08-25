package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class UnitConversionIndicatorField extends AbstractField<UnitConversionIndicatorField> {

    public static final int LENGTH = 1;

    private int unitConversionIndicatorCode;
    private UnitConversionIndicator unitConversionIndicator;

    public UnitConversionIndicatorField() {
        this.unitConversionIndicator = UnitConversionIndicator.UNKNOWN;
    }

    public UnitConversionIndicatorField(UnitConversionIndicator unitConversionIndicator) {
        this.unitConversionIndicator = unitConversionIndicator;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(unitConversionIndicatorCode, LENGTH);
    }

    @Override
    public UnitConversionIndicatorField parse(byte[] rawData, int offset) throws ParsingException {
        unitConversionIndicatorCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        unitConversionIndicator = UnitConversionIndicator.fromStatusCode(unitConversionIndicatorCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getUnitConversionIndicatorCode() {
        return unitConversionIndicatorCode;
    }

    public String getInstallationStatusInfo() {
        if (!this.unitConversionIndicator.equals(UnitConversionIndicator.UNKNOWN)) {
            return unitConversionIndicator.getStatusInfo();
        } else {
            return (unitConversionIndicator.getStatusInfo() + " " + unitConversionIndicator);
        }
    }

    public UnitConversionIndicator getUnitConversionIndicator() {
        return unitConversionIndicator;
    }

    public enum UnitConversionIndicator {
        CAPACITIVE(0, "Capacitive"),
        MONO_PHASE(1, "Inductive"),
        UNKNOWN(-1, "Unknown reactive power characteristic");

        private final int statusCode;
        private final String statusInfo;

        private UnitConversionIndicator(int statusCode, String statusInfo) {
            this.statusCode = statusCode;
            this.statusInfo = statusInfo;
        }

        public String getStatusInfo() {
            return statusInfo;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public static UnitConversionIndicator fromStatusCode(int statusCode) {
            for (UnitConversionIndicator version : UnitConversionIndicator.values()) {
                if (version.getStatusCode() == statusCode) {
                    return version;
                }
            }
            return UnitConversionIndicator.UNKNOWN;
        }
    }
}