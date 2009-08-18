package com.energyict.dlms.aso;


public class ConformanceBlock{
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
	public static final int BIT_READ = 3;
	public static final int BIT_WRITE = 4;
	public static final int BIT_UNCONFIRMED_WRITE = 5;
	public static final int BIT_ATTRB0_SUPP_SET = 8;
	public static final int BIT_PRIORITY_MGMT_SUPP = 9;
	public static final int BIT_ATTRB0_SUPP_GET = 10;
	public static final int BIT_BLOCK_TRANSF_GET = 11;
	public static final int BIT_BLOCK_TRANSF_SET = 12;
	public static final int BIT_BLOCK_TRANSF_ACTION = 13;
	public static final int BIT_MULTIPLE_REFS = 14;
	public static final int BIT_INFORMATION_REPORT = 15;
	public static final int BIT_PARAMETERIZED_ACCESS = 18;
	public static final int BIT_GET = 19;
	public static final int BIT_SET = 20;
	public static final int BIT_SELECTIVE_ACCESS = 21;
	public static final int BIT_EVENT_NOTIFY = 22;
	public static final int BIT_ACTION = 23;

	private byte[] block = new byte[24];

	public ConformanceBlock(){}

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
	private void updateBlock(long proposedConformance){
		for(int i = 0; i < this.block.length ; i++){
			this.block[i] = (byte)((proposedConformance>>(this.block.length-1-i))&0x1);
		}
	}

	/**
	 * Construct the long value from the byteArry
	 * @return the conformance long-value
	 */
	protected long getValue(){
		long value = 0;
		for(int i = 0; i < this.block.length; i++){
			value += this.block[i]*Math.pow(2, (this.block.length-1-i));
		}
		return value;
	}

	/**
	 * Set a certain bit in the byteArray
	 * @param bit - the number of the bit you want to set
	 */
	public void setBit(int bit){
		this.block[bit] = 1;
	}
	/**
	 * Clear a certain bit in the byteArray
	 * @param bit - the number of bit you want to clear
	 */
	public void clearBit(int bit){
		this.block[bit] = 0;
	}
}
