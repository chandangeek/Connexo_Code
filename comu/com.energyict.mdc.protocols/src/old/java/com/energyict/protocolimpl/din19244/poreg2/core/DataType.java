/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.core;

public enum DataType {

    Invalid(0, 0, false),
    Byte(1, 1, false),
    Word(2, 2, false),
    Word24(3, 3, false),
    ULong(4, 4, false),
    SByte(65, 1, true),
    Int(66, 2, true),
    Int24(67, 3, true),
    Long(68, 4, true),
    BCD(129, 0, false),     //Indicating the length is variable, max is 63 bytes
    ASCII(193, 0, false);


    private int id;
    private int length;
    private boolean signed;

    DataType(int id, int length, boolean signed) {
        this.id = id;
        this.length = length;
        this.signed = signed;
    }

    public int getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public boolean isSigned() {
        return signed;
    }

    public static DataType fromId(int id) {
        for (DataType dataType : values()) {
            if (dataType.getId() == id) {
                return dataType;
            }
        }
        return Invalid;
    }
}