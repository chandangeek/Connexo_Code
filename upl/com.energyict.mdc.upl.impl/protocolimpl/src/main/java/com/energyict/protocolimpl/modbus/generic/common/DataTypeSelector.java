package com.energyict.protocolimpl.modbus.generic.common;

/**
 * @author sva
 * @since 18/11/13 - 14:43
 */
public class DataTypeSelector {

    private static final int LITTLE_ENDIAN_MODULO_DISCRIMINATOR = 128;

    private int range;
    private DataType dataType;
    private int dataTypeSelectorCode;
    private boolean bigEndianEncoded;

    private DataTypeSelector(int dataTypeSelector) {
        this.setDataTypeSelectorCode(dataTypeSelector);
        this.setBigEndianEncoded(dataTypeSelector < LITTLE_ENDIAN_MODULO_DISCRIMINATOR);
        dataTypeSelector = dataTypeSelector % 128;

        if (dataTypeSelector >= DataType.ASCII.getDataTypeCode()) {
            this.setDataType(DataType.ASCII);
            this.setRange(dataTypeSelector - DataType.ASCII.getDataTypeCode());
        } else {
            this.setDataType(DataType.getDataType(dataTypeSelector));
            this.setRange(getDataType().getRange());
        }
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public boolean isBigEndianEncoded() {
        return bigEndianEncoded;
    }

    public void setBigEndianEncoded(boolean bigEndianEncoded) {
        this.bigEndianEncoded = bigEndianEncoded;
    }

    public int getDataTypeSelectorCode() {
        return dataTypeSelectorCode;
    }

    public void setDataTypeSelectorCode(int dataTypeSelectorCode) {
        this.dataTypeSelectorCode = dataTypeSelectorCode;
    }

    public static DataTypeSelector getDataTypeSelector(int dataTypeSelectorCode) {
        return new DataTypeSelector(dataTypeSelectorCode);
    }

    public enum DataType {
        BYTE(0, 1),
        REGISTER(1, 1),
        SIGNED_REGISTER(2, 1),
        INTEGER(3, 2),
        SIGNED_INTEGER(4, 2),
        LONG(5, 4),
        SIGNED_LONG(6, 4),
        FLOAT_32BIT(7, 2),
        FLOAT_64BIT(8, 4),
        BCD_32BIT(9, 2),
        BCD_64BIT(10, 4),
        ASCII(16, 1),
        UNKNOWN(-1, 0);

        private final int range;
        private final int dataTypeCode;

        private DataType(int dataTypeCode, int range) {
            this.dataTypeCode = dataTypeCode;
            this.range = range;
        }

        public int getDataTypeCode() {
            return dataTypeCode;
        }

        private int getRange() {
            return range;
        }

        public static DataType getDataType(int dataTypeCode) {
            for (DataType dataType : DataType.values()) {
                if (dataType.dataTypeCode == dataTypeCode) {
                    return dataType;
                }
            }
            return DataType.UNKNOWN;
        }
    }
}
