/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.aso;

import com.energyict.dlms.DLMSUtils;

public class ConformanceBlock {
	/**
	 * Default conformance block with LongName referencing:
	 * <pre>
	 * Meaning:
	 * - priority_management is supported (bit9)
	 * - attribute_0 supported with GET (bit10)
	 * - block transfer with GET supported (bit11)
	 * - block transfer with SET supported (bit12)
	 * - block transfer with ACTION supported (bit13)
	 * - multiple references are supported (bit14)
	 * - GET, SET, ACTION, EventNotification are supported (bit19,20,22,23)
	 * - selective access is supported (bit21)
	 * </pre>
	 */
	public static final long DEFAULT_LN_CONFORMANCE_BLOCK = 32287;
	/**
	 * Default conformance block with ShortName referencing:
	 * <pre>
	 * Meaning:
	 * <pre>
	 * - READ, WRITE, UNCONFIRMED_WRITE, INFORMATIONREPORT are supported (bit3,4,5,15)
	 * - multiple references are supported (bit14)
	 * - parameterized_access is supported (bit18)
	 * </pre>
	 */
	public static long DEFAULT_SN_CONFORMANCE_BLOCK = 1835808;

	/**
	 * These bits can be used to set or clear a bit in the conformance block
	 */

//    public static final int BIT_RESERVED_ZERO = 0;
    public static final int BIT_GENERAL_PROTECTION = 1;
    public static final int BIT_GENERAL_BLOCK_TRANSFER = 2;
	public static final int BIT_READ = 3;
	public static final int BIT_WRITE = 4;
	public static final int BIT_UNCONFIRMED_WRITE = 5;
//    public static final int BIT_RESERVED_SIX = 6;
//    public static final int BIT_RESERVED_SEVEN = 7;
	public static final int BIT_ATTRB0_SUPP_SET = 8;
	public static final int BIT_PRIORITY_MGMT_SUPP = 9;
	public static final int BIT_ATTRB0_SUPP_GET = 10;
	public static final int BIT_BLOCK_TRANSF_GET = 11;
	public static final int BIT_BLOCK_TRANSF_SET = 12;
	public static final int BIT_BLOCK_TRANSF_ACTION = 13;
	public static final int BIT_MULTIPLE_REFS = 14;
	public static final int BIT_INFORMATION_REPORT = 15;
    public static final int BIT_DATA_NOTIFICATION = 16;
    public static final int BIT_ACCESS = 17;
	public static final int BIT_PARAMETERIZED_ACCESS = 18;
	public static final int BIT_GET = 19;
	public static final int BIT_SET = 20;
	public static final int BIT_SELECTIVE_ACCESS = 21;
	public static final int BIT_EVENT_NOTIFY = 22;
	public static final int BIT_ACTION = 23;

    private static final String CRLF = "\r\n";

    private boolean[] block = new boolean[24];

    private static final String[] NAMES = {
            "RESERVED_ZERO",
            "GENERAL_PROTECTION",
            "GENERAL_BLOCK_TRANSFER",
            "READ",
            "WRITE",
            "UNCONFIRMED_WRITE",
            "RESERVED_SIX",
            "RESERVED_SEVEN",
            "ATTRIBUTE0_SUPPORTED_WITH_SET",
            "PRIORITY_MGMT_SUPPORTED",
            "ATTRIBUTE0_SUPPORTED_WITH_GET",
            "BLOCK_TRANSFER_WITH_GET_OR_READ",
            "BLOCK_TRANSFER_WITH_SET_OR_WRITE",
            "BLOCK_TRANSFER_WITH_ACTION",
            "MULTIPLE_REFERENCES",
            "INFORMATION_REPORT",
            "RESERVED_SIXTEEN",
            "RESERVED_SEVENTEEN",
            "PARAMETERIZED_ACCESS",
            "GET",
            "SET",
            "SELECTIVE_ACCESS",
            "EVENT_NOTIFICATION",
            "ACTION"
    };

	public ConformanceBlock(){}

	/**
	 * Get the {@link Long} value as a byte array of 4 bytes
	 * @return
	 */
	private byte[] getByteValue() {
		byte[] bytes = new byte[4];
		for (int i = 0; i < bytes.length; i++) {
			bytes[bytes.length - (i+1)] = (byte)(getValue() >> (8*i));
		}
		return bytes;
	}

	/**
	 * Constructor with a given conformance. It is advisable to use one of the default LN or SN conformance blocks
	 * @param proposedConformance - a long value representing a bitString according to the required conformance
	 */
	public ConformanceBlock(long proposedConformance){
		updateBlock(proposedConformance);
	}

	/**
	 * @return the AXDR encoded byteArray of the conformance Block
	 */
	public byte[] getAXDREncodedConformanceBlock(){
		byte[] b = new byte[7];
		b[0] = (byte)0x5F;	// Encoding of the [APPLICATION 31]tag (part1)
		b[1] = (byte)0x1F;	// Encoding of the [APPLICATION 31]tag (part2)
		b[2] = (byte)0x04;	// length of the contents field in octet
		b[3] = (byte)0x00;	// Unused bits in final octet of bitstring
		for(int i = 0; i < 3; i++){
			b[b.length-1-i] = (byte)(getValue() >> (8*i));
		}
		return b;
	}

	/**
	 * Fill in the byteArray with '0' and '1' to indicate which bit is set or not
	 * @param proposedConformance - the long value representing the bitString according to the required conformance
	 */
    private void updateBlock(long proposedConformance) {
        for (int i = 0; i < this.block.length; i++) {
            this.block[i] = ((proposedConformance >> (this.block.length - 1 - i)) & 0x01) == 0x01;
        }
    }

    /**
	 * Construct the long value from the byteArry
	 * @return the conformance long-value
	 */
	public long getValue(){
		long value = 0;
		for(int i = 0; i < this.block.length; i++){
            value += (this.block[i] ? 1 : 0) * Math.pow(2, (this.block.length - 1 - i));
        }
		return value;
	}

	/**
	 * Set a certain bit in the byteArray
	 * @param bit - the number of the bit you want to set
	 */
	public void setBit(int bit){
		this.block[bit] = true;
	}

    public void setBit(int bitNr, boolean bitValue){
        this.block[bitNr] = bitValue;
    }

    /**
	 * Clear a certain bit in the byteArray
	 * @param bit - the number of bit you want to clear
	 */
	public void clearBit(int bit){
		this.block[bit] = false;
	}

    public boolean isAction() {
        return block[BIT_ACTION];
    }

    public boolean isEventNotification() {
        return block[BIT_EVENT_NOTIFY];
    }

    public boolean isSelectiveAccess() {
        return block[BIT_SELECTIVE_ACCESS];
    }

    public boolean isSet() {
        return block[BIT_SET];
    }

    public boolean isGet() {
        return block[BIT_GET];
    }

    public boolean isParameterizedAccess() {
        return block[BIT_PARAMETERIZED_ACCESS];
    }

    public boolean isInformationReport() {
        return block[BIT_INFORMATION_REPORT];
    }

    public boolean isMultipleReferences() {
        return block[BIT_MULTIPLE_REFS];
    }

    public boolean isBlockTransferWithAction() {
        return block[BIT_BLOCK_TRANSF_ACTION];
    }

    public boolean isBlockTransferWithSetOrWrite() {
        return block[BIT_BLOCK_TRANSF_SET];
    }

    public boolean isBlockTransferWithGetOrRead() {
        return block[BIT_BLOCK_TRANSF_GET];
    }

    public boolean isAttribute0SupportedWithGet() {
        return block[BIT_ATTRB0_SUPP_GET];
    }

    public boolean isPriorityMgmtSupported() {
        return block[BIT_PRIORITY_MGMT_SUPP];
    }

    public boolean isAttribute0SupportedWithSet() {
        return block[BIT_ATTRB0_SUPP_SET];
    }

    public boolean isUnconfirmedWrite() {
        return block[BIT_UNCONFIRMED_WRITE];
    }

    public boolean isDataNotification() {
        return block[BIT_DATA_NOTIFICATION];
    }

    public boolean isAccess() {
        return block[BIT_ACCESS];
    }


    public boolean isWrite() {
        return block[BIT_WRITE];
    }

    public boolean isRead() {
        return block[BIT_READ];
    }

    public void setRead(boolean value) {
        setBit(BIT_READ, value);
    }

    public void setWrite(boolean value) {
        setBit(BIT_WRITE, value);
    }

    public boolean isGeneralProtection() {
        return block[BIT_GENERAL_PROTECTION];
    }

    public void setGeneralProtection(boolean value) {
        setBit(BIT_GENERAL_PROTECTION, value);
    }

    public boolean isGeneralBlockTransfer(){
        return block[BIT_GENERAL_BLOCK_TRANSFER];
    }

    public void setGeneralBlockTransfer(boolean value) {
        setBit(BIT_GENERAL_BLOCK_TRANSFER, value);
    }

    public void setUnconfirmedWrite(boolean value) {
        setBit(BIT_UNCONFIRMED_WRITE, value);
    }

    public void setAttribute0SupportedWithSet(boolean value) {
        setBit(BIT_ATTRB0_SUPP_SET, value);
    }

    public void setPriorityManagementSupported(boolean value) {
        setBit(BIT_PRIORITY_MGMT_SUPP, value);
    }

    public void setAttribute0SupportedWithGet(boolean value) {
        setBit(BIT_ATTRB0_SUPP_GET, value);
    }

    public void setBlockTransferWithGetOrRead(boolean value) {
        setBit(BIT_BLOCK_TRANSF_GET, value);
    }

    public void setBlockTransferWithSetOrWrite(boolean value) {
        setBit(BIT_BLOCK_TRANSF_SET, value);
    }

    public void setBlockTransferWithAction(boolean value) {
        setBit(BIT_BLOCK_TRANSF_ACTION, value);
    }

    public void setMultipleReferences(boolean value) {
        setBit(BIT_MULTIPLE_REFS, value);
    }

    public void setInformationReport(boolean value) {
        setBit(BIT_INFORMATION_REPORT, value);
    }

    public void setParameterizedAccess(boolean value) {
        setBit(BIT_PARAMETERIZED_ACCESS, value);
    }

    public void setGet(boolean value) {
        setBit(BIT_GET, value);
    }

    public void setSet(boolean value) {
        setBit(BIT_SET, value);
    }

    public void setSelectiveAccess(boolean value) {
        setBit(BIT_SELECTIVE_ACCESS, value);
    }

    public void setEventNotification(boolean value) {
        setBit(BIT_EVENT_NOTIFY, value);
    }

    public void setAction(boolean value) {
        setBit(BIT_ACTION, value);
    }

    public void setAccess(boolean value) {
        setBit(BIT_ACCESS, value);
    }

    public void setDataNotification(boolean value) {
        setBit(BIT_DATA_NOTIFICATION, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConformanceBlock{ rawValue = ").append(DLMSUtils.getHexStringFromBytes(getByteValue())).append(CRLF);
        for (int i = 0; i < NAMES.length; i++) {
            String name = NAMES[i];
            sb.append(block[i] ? "  * " : "    ").append("[").append(DLMSUtils.addPadding(String.valueOf(i), '0', 2, false)).append("] ");
            sb.append(DLMSUtils.addPadding(name, ' ', 33, true)).append(" = ").append(block[i]).append(CRLF);
        }
        sb.append("}");
        return sb.toString();
    }

}
