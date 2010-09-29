package com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:41:46
 */
public class FunctionCode extends AbstractPacketField {

    public static final int LENGTH = 1;
    public static final int ENCRYPTED_MASK = 0x080;

    private int functionCode = 0x00;

    private static final int NOT_USED = 0x000;
    private static final int CTR_LOWER = 0x070;
    private static final int CTR_UPPER = 0x07E;
    private static final int MANUF_LOWER = 0x070;
    private static final int MANUF_UPPER = 0x07E;
    private static final int RESERVED_LOWER = 0x07F;
    private static final int RESERVED_UPPER = 0x0FF;

    public FunctionCode(int functionCode) {
        this.functionCode = functionCode & 0x0FF;
    }

    public FunctionCode(byte[] rawPacket, int offset) {
        this(rawPacket[offset] & 0x0FF);
    }

    public void setEncrypted(boolean encrypted) {
        if (encrypted) {
            functionCode |= ENCRYPTED_MASK;
        } else {
            functionCode &= ~ENCRYPTED_MASK;
        }
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public FunctionType getFunctionType() {
        return FunctionType.getFunctionType(functionCode & 0x07F);
    }

    public boolean isNotUsed() {
        return functionCode == NOT_USED;
    }

    public boolean isCTRApplication() {
        return (functionCode >= CTR_LOWER) && (functionCode <= CTR_UPPER);
    }

    public boolean isManufacturer() {
        return (functionCode >= MANUF_LOWER) && (functionCode <= MANUF_UPPER);
    }

    public boolean isReserved() {
        return (functionCode >= RESERVED_LOWER) && (functionCode <= RESERVED_UPPER);
    }

    public boolean isEncrypted() {
        return ((functionCode & ENCRYPTED_MASK) != 0);
    }

    public byte[] getBytes() {
        return new byte[] {(byte) (functionCode & 0x0FF)};
    }

    @Override
    public String toString() {
        return super.toString() + " [char='" + getFunctionType().getFunctionTypeValueAsChar() + "', encrypted="+ isEncrypted()+"]";
    }

}
