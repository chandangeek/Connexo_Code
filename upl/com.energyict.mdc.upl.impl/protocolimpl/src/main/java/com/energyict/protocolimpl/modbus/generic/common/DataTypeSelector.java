package com.energyict.protocolimpl.modbus.generic.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 18/11/13 - 14:43
 */
public class DataTypeSelector {

    private static final int ASCII_DATA_TYPE_CODE = 16;
    private static final int LITTLE_ENDIAN_MODULO_DISCRIMINATOR = 128;
    protected static final List<DataType> dataTypes = new ArrayList<DataType>();

    public static final DataType BYTE_DATA_TYPE = new DataType("BYTE", 0, 1);
    public static final DataType REGISTER_DATA_TYPE = new DataType("REGISTER", 1, 1);
    public static final DataType SIGNED_REGISTER_DATA_TYPE = new DataType("SIGNED_REGISTER", 2, 1);
    public static final DataType INTEGER_DATA_TYPE = new DataType("INTEGER", 3, 2);
    public static final DataType SIGNED_INTEGER_DATA_TYPE = new DataType("SIGNED_INTEGER", 4, 2);
    public static final DataType LONG_DATA_TYPE = new DataType("LONG", 5, 4);
    public static final DataType SIGNED_LONG_DATA_TYPE = new DataType("SIGNED_LONG", 6, 4);
    public static final DataType FLOAT_32_BIT_DATA_TYPE = new DataType("FLOAT_32BIT", 7, 2);
    public static final DataType FLOAT_64_BIT_DATA_TYPE = new DataType("FLOAT_64BIT", 8, 4);
    public static final DataType BCD_32_BIT_DATA_TYPE = new DataType("BCD_32BIT", 9, 2);
    public static final DataType BCD_64_BIT_DATA_TYPE = new DataType("BCD_64BIT", 10, 4);
    public static final DataType ASCII_DATA_TYPE = new DataType("ASCII", 16, 1);
    public static final DataType UNKNOWN_DATA_TYPE = new DataType("UNKNOWN", -1, 0);

    static {
        dataTypes.add(BYTE_DATA_TYPE);
        dataTypes.add(REGISTER_DATA_TYPE);
        dataTypes.add(SIGNED_REGISTER_DATA_TYPE);
        dataTypes.add(INTEGER_DATA_TYPE);
        dataTypes.add(SIGNED_INTEGER_DATA_TYPE);
        dataTypes.add(LONG_DATA_TYPE);
        dataTypes.add(SIGNED_LONG_DATA_TYPE);
        dataTypes.add(FLOAT_32_BIT_DATA_TYPE);
        dataTypes.add(FLOAT_64_BIT_DATA_TYPE);
        dataTypes.add(BCD_32_BIT_DATA_TYPE);
        dataTypes.add(BCD_64_BIT_DATA_TYPE);
        dataTypes.add(ASCII_DATA_TYPE);
    }

    private int range;
    private DataType dataType;
    private int dataTypeSelectorCode;
    private boolean bigEndianEncoded;

    protected DataTypeSelector(int dataTypeSelector) {
        this.setDataTypeSelectorCode(dataTypeSelector);
        this.setBigEndianEncoded(dataTypeSelector < LITTLE_ENDIAN_MODULO_DISCRIMINATOR);
        dataTypeSelector = dataTypeSelector % 128;

        if (dataTypeSelector >= ASCII_DATA_TYPE_CODE) {
            this.setDataType(ASCII_DATA_TYPE);
            this.setRange(dataTypeSelector - ASCII_DATA_TYPE_CODE);
        } else {
            this.setDataType(dataTypeSelector);
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

    public void setDataType(int dataTypeSelector) {
        this.dataType = searchCorrespondingDataType(dataTypeSelector);
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

    protected DataType searchCorrespondingDataType(int dataTypeSelector) {
        for (DataType dataType : dataTypes) {
            if (dataType.getDataTypeCode() == dataTypeSelector) {
                return dataType;
            }
        }
        return UNKNOWN_DATA_TYPE;
    }
}
