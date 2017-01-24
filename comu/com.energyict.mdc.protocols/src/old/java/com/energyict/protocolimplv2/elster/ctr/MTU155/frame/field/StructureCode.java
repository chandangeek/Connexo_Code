package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;

/**
 * Class for the structure code field in a frame
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 9:29:10
 */
public class StructureCode extends AbstractField<StructureCode> {

    public static final int IDENTIFICATION = 0x30;
    public static final int REGISTER = 0x50;
    public static final int ARRAY = 0x51;
    public static final int TRACE = 0x52;
    public static final int TRACE_C = 0x53;
    public static final int EVENT_ARRAY = 0x56;
    public static final int TABLE_DEC = 0x33;
    public static final int TABLE_DECF = 0x34;

    private int structureCode;

    public StructureCode(int structureCode) {
        this.structureCode = structureCode;
    }

    public StructureCode() {
        this(0);
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(structureCode, getLength());
    }

    public int getStructureCode() {
        return structureCode;
    }

    public void setStructureCode(int structureCode) {
        this.structureCode = structureCode;
    }

    public StructureCode parse(byte[] rawData, int offset) {
        structureCode = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public boolean isIdentification() {
        return getStructureCode() == IDENTIFICATION;
    }

    public boolean isRegister() {
        return getStructureCode() == REGISTER;
    }

    public boolean isArray() {
        return getStructureCode() == ARRAY;
    }

    public boolean isTrace() {
        return getStructureCode() == TRACE;
    }

    public boolean isTrace_C() {
        return getStructureCode() == TRACE_C;
    }

    public boolean isEvent_Array() {
        return getStructureCode() == EVENT_ARRAY;
    }

    public boolean isDECFTable() {
        return getStructureCode() == TABLE_DECF;
    }

    public boolean isDECTable() {
        return getStructureCode() == TABLE_DEC;
    }

}
