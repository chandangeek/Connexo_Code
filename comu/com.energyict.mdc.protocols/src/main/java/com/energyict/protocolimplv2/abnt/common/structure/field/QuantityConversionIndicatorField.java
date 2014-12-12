package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class QuantityConversionIndicatorField extends AbstractField<QuantityConversionIndicatorField> {

    public static final int LENGTH = 1;

    private int quantityConversionIndicatorCode;
    private QuantityConversionIndicator quantityConversionIndicator;

    public QuantityConversionIndicatorField() {
        this.quantityConversionIndicator = QuantityConversionIndicator.UNKNOWN;
    }

    public QuantityConversionIndicatorField(QuantityConversionIndicator quantityConversionIndicator) {
        this.quantityConversionIndicator = quantityConversionIndicator;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(quantityConversionIndicatorCode, LENGTH);
    }

    @Override
    public QuantityConversionIndicatorField parse(byte[] rawData, int offset) throws ParsingException {
        quantityConversionIndicatorCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        quantityConversionIndicator = QuantityConversionIndicator.fromStatusCode(quantityConversionIndicatorCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getQuantityConversionIndicatorCode() {
        return quantityConversionIndicatorCode;
    }

    public String getQuantityConversionIndicatorInfo() {
        if (!this.quantityConversionIndicator.equals(QuantityConversionIndicator.UNKNOWN)) {
            return quantityConversionIndicator.getInfo();
        } else {
            return (quantityConversionIndicator.getInfo() + " " + quantityConversionIndicator);
        }
    }

    public QuantityConversionIndicator getQuantityConversionIndicator() {
        return quantityConversionIndicator;
    }

    public enum QuantityConversionIndicator {
        ELECTRICAL_SYSTEM(0, "The quantities indicated correspond to the electrical system according to the connection type"),
        METER_ELEMENTS(1, "The indicated quantities correspond to the meter elements"),
        UNKNOWN(-1, "Unknown quantity conversion indicator");

        private final int code;
        private final String info;

        private QuantityConversionIndicator(int code, String info) {
            this.code = code;
            this.info = info;
        }

        public String getInfo() {
            return info;
        }

        public int getCode() {
            return code;
        }

        public static QuantityConversionIndicator fromStatusCode(int statusCode) {
            for (QuantityConversionIndicator version : QuantityConversionIndicator.values()) {
                if (version.getCode() == statusCode) {
                    return version;
                }
            }
            return QuantityConversionIndicator.UNKNOWN;
        }
    }
}