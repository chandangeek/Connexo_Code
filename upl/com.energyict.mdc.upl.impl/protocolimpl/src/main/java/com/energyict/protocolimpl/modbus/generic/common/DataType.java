package com.energyict.protocolimpl.modbus.generic.common;

/**
 * @author sva
 * @since 18/03/2015 - 12:05
 */
public class DataType {

    private final String name;
    private final int range;
    private final int dataTypeCode;

    public DataType(String name, int dataTypeCode, int range) {
        this.name = name;
        this.dataTypeCode = dataTypeCode;
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public int getDataTypeCode() {
        return dataTypeCode;
    }

    public int getRange() {
        return range;
    }
}