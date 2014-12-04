package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;

/**
 * Class for the function field in a frame
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 9:29:10
 */
public class FunctionCode extends AbstractField<FunctionCode> {

    private int functionCode;

    public byte[] getBytes() {
        return getBytesFromInt(functionCode, getLength());
    }

    public FunctionCode parse(byte[] rawData, int offset) {
        functionCode = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public EncryptionStatus getEncryptionStatus() {
        int bits = (functionCode >> 6) & 0x03;        return EncryptionStatus.fromEncryptionBits(bits);
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

    public boolean isQuery() {
        return getFunction().equals(Function.QUERY);
    }

    public boolean isAnswer() {
        return getFunction().equals(Function.ANSWER);
    }

    public boolean isMeterResponse() {
        return isAnswer() || isVoluntary() || isNack() || isAck() || isIdentificationReply();
    }

    private boolean isVoluntary() {
        return getFunction().equals(Function.VOLUNTARY);
    }

    public boolean isNack() {
        return getFunction().equals(Function.NACK);
    }

    public boolean isAck() {
        return getFunction().equals(Function.ACK);
    }

    public boolean isWrite() {
        return getFunction().equals(Function.WRITE);
    }

    public int getLength() {
        return 1;
    }
}

