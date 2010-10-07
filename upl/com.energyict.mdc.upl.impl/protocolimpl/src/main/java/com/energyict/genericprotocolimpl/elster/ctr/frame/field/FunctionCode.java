package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 9:29:10
 */
public class FunctionCode extends AbstractField<FunctionCode> {

    public static final int LENGTH = 1;
    private int functionCode;

    public byte[] getBytes() {
        return getBytesFromInt(functionCode, LENGTH);
    }

    public FunctionCode parse(byte[] rawData, int offset) {
        functionCode = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public EncryptionStatus getEncryptionStatus() {
        int bits = (functionCode >> 6) & 0x03;
        return EncryptionStatus.fromEncryptionBits(bits);
    }

    public void setEncryptionStatus(EncryptionStatus encryptionStatus) {
        if (encryptionStatus == EncryptionStatus.INVALID_ENCRYPTION) {
            throw new IllegalArgumentException("Could not set encryption status to " + encryptionStatus);
        }
        int statusBits = (encryptionStatus.getEncryptionStateBits() << 6) & 0x0C0;
        functionCode &= 0x03F;
        functionCode |= statusBits;
    }

    public Function getFunction() {
        return Function.fromFunctionCode(functionCode);
    }

    public void setFunction(Function function) {
        functionCode &= 0xC0;
        functionCode |= function.getFunctionCode() & 0x3F;
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(int functionCode) {
        this.functionCode = functionCode;
    }

    public boolean isIdentificationReply() {
        return getFunction().equals(Function.IDENTIFICATION_REPLY);
    }

    public boolean isIdentificationRequest() {
        return getFunction().equals(Function.IDENTIFICATION_REQUEST);
    }

}
