/*
 * OctetString.java
 * Created on 16 oktober 2007, 11:35
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;

/**
 * @author kvds
 * Changes:
 * GNA |26012009| Added and changed some functions to make fixed OctetStrings.
 * Size is not returned in the encodedData
 */
public class OctetString extends AbstractDataType {

	private byte[]	octetStr;
	private int		size;
	private int		offsetBegin, offsetEnd;
	private boolean	fixed;

    /**
     * Creates a new instance of OctetString from the raw BER encoded bytes
     *
     * @param berEncodedData the raw BER encoded byte array
     * @param offset         The offset in the BER bytes
     * @throws IOException If the berEncoded data is not an octetString
     */
    public OctetString(byte[] berEncodedData, int offset) throws IOException {
        int workingOffset = offset;
        offsetBegin = workingOffset;
        if (berEncodedData[workingOffset] != AxdrType.OCTET_STRING.getTag()) {
            throw new ProtocolException("OctetString, invalid identifier " + berEncodedData[workingOffset]);
        }
        workingOffset++;
        size = DLMSUtils.getAXDRLength(berEncodedData, workingOffset);
        workingOffset += DLMSUtils.getAXDRLengthOffset(berEncodedData, workingOffset);
        octetStr = ProtocolUtils.getSubArray2(berEncodedData, workingOffset, size);
        workingOffset += size;
        offsetEnd = workingOffset;
        this.fixed = false;
    }

    /**
     * Creates a new instance of a fixed length OctetString from the raw BER encoded bytes
     *
     * @param berEncodedData the raw BER encoded byte array
     * @param offset         The offset in the BER bytes
     * @param fixed          The length of the OctetString
     * @throws IOException If the berEncoded data is not an octetString
     */
    public OctetString(byte[] berEncodedData, int offset, boolean fixed) throws IOException {
        offsetBegin = offset;
        if (berEncodedData[offset] != AxdrType.OCTET_STRING.getTag()) {
            throw new ProtocolException("OctetString, invalid identifier " + berEncodedData[offset]);
        }
        size = berEncodedData.length - 1;
        offset += DLMSUtils.getAXDRLengthOffset(berEncodedData, offset);
        octetStr = ProtocolUtils.getSubArray2(berEncodedData, offset, size);
        offset += size;
        offsetEnd = offset;
        this.fixed = fixed;
    }

    /**
	 * Create a variable length OctetString
	 *
	 * @param octetStr
	 */
	public OctetString(byte[] octetStr) {
		this(octetStr, octetStr.length, 0);
	}

	/**
	 * It is possible to create a fixed length OctetString
	 *
	 * @param octetStr
	 * @param fixed a boolean to indicate whether it is fixed or not
	 */
	public OctetString(byte[] octetStr, boolean fixed) {
		this(octetStr, octetStr.length, (fixed ? 1 : 0));
	}

	protected OctetString(byte[] octetStr, int size, int dummy) {
		this.setOctetStr(octetStr);
		this.size = size;
		this.fixed = (dummy == 1);
		this.offsetBegin = 0;
		this.offsetEnd = size + (fixed ? 1 : 2);
	}

	public String stringValue() {
		return new String(getOctetStr());
	}

	protected byte[] doGetBEREncodedByteArray() {

		byte[] encodedLength;
		if (this.fixed) {
			encodedLength = new byte[0];
		} else {
			encodedLength = DLMSUtils.getAXDRLengthEncoding(size);
		}
		byte[] data = new byte[size + 1 + encodedLength.length];
		data[0] = AxdrType.OCTET_STRING.getTag();
		for (int i = 0; i < encodedLength.length; i++) {
			data[1 + i] = encodedLength[i];
		}
		for (int i = 0; i < (data.length - (1 + encodedLength.length)); i++) {
			if (i < getOctetStr().length) {
				data[(1 + encodedLength.length) + i] = getOctetStr()[i];
			} else {
				data[(1 + encodedLength.length) + i] = 0;
			}
		}
		return data;
	}

	protected int size() {
		return offsetEnd - offsetBegin;
	}

    /**
     * Deprecated, because the behavior is not consistent and clashes with doGetBEREncodedByteArray.
     * This method is only valid if getOctetStr() contains the <b>BER Encoded</b> octetString. <br>
     *
     * @return the contentBytes
     */
    @Deprecated
	public byte[] getContentBytes() {
		byte[] content = new byte[getOctetStr().length - 2];
		System.arraycopy(getOctetStr(), fixed ? 1 : 2, content, 0, content.length);
		return content;
	}

	public byte[] toByteArray() {
		return getOctetStr();
	}

	public byte[] getOctetStr() {
		return octetStr;
	}

	public void setOctetStr(byte[] octetStr) {
		this.octetStr = octetStr.clone();
	}

	public BigDecimal toBigDecimal() {
		BigDecimal result = BigDecimal.ZERO;
		for (int i = 0; i < octetStr.length; i++) {
			int idx = octetStr.length - i - 1;
			BigDecimal temp = new BigDecimal(octetStr[idx]).movePointRight(i);
			result = result.add(temp);

		}
		return result;
	}

	public int intValue() {
		return -1;
	}

	public long longValue() {
		return -1;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public DateTime getDateTime(TimeZone tz) {
		try {
			if ((getBEREncodedByteArray() == null) || (getBEREncodedByteArray().length != 14)) {
				throw new ProtocolException("AXDRDateTime is expecting an OctetString with a data length of 12.");
			}
			if (tz == null) {
				return new DateTime(this);
			} else {
				return new DateTime(this, tz);
			}
		} catch (IOException e) {
			return null;
		}
	}

    /**
     * Create a new OctetString from a given String.
     * If the string is 'null' you'll get an empty string.
     *
     * @param string The String that should be used to construct the OctetString
     * @return The new com.energyict.dlms.axrdencoding.OctetString
     */
    public static OctetString fromString(String string) {
        if (string == null) {
            return OctetString.fromByteArray(new byte[0]);
        } else {
            return OctetString.fromByteArray(string.getBytes());
        }
    }

    /**
     * Create a new OctetString from a given String.
     * If the string is 'null' you'll get an empty string.
     *
     * @param string The String that should be used to construct the OctetString
     * @param size
     * @return The new com.energyict.dlms.axrdencoding.OctetString
     */
    public static OctetString fromString(String string, int size) {
        return new OctetString(string.getBytes(), size, 0);
    }

    /**
     *
     * @param string
     * @param size
     * @param fixed
     * @return
     */
    public static OctetString fromString(String string, int size, boolean fixed) {
        return new OctetString(string.getBytes(), size, (fixed ? 1 : 0));
    }

    /**
     * Create an OctetString with the content of an IP-address
     * The IP address should have the standard dotted format for example "192.168.1.20"
     *
     * @param ipAddress the IP-address to parse
     * @return a new OctetString with 6 fields
     * @throws IllegalArgumentException If the IPv4 address is invalid
     */
    public static OctetString fromIPv4Address(String ipAddress) throws IllegalArgumentException {
        String[] ipFields = ipAddress.split("\\.");
        if (ipFields.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 address [" + ipAddress + "]");
        }
        byte[] ipBytes = new byte[ipFields.length];
        for (int i = 0; i < ipBytes.length; i++) {
            int ipField = Integer.parseInt(ipFields[i]);
            if (ipField > 255) {
                throw new IllegalArgumentException("Invalid IPv4 address [" + ipAddress + "]");
            }
            ipBytes[i] = (byte) ipField;
        }
        return OctetString.fromByteArray(ipBytes);
    }

    /**
     * Create a new OctetString from a given ObisCode.
     * Ex: 1.2.3.4.5.255 -> 09060102030405FF
     *
     * @param obisCode
     * @return a new OctetString with 6 fields (A.B.C.D.E.F)
     */
    public static OctetString fromObisCode(ObisCode obisCode) {
        return OctetString.fromByteArray(obisCode.getLN());
    }

    /**
     * Create a new OctetString from a given string that represents a valid obisCode.
     * Ex: "1.2.3.4.5.255" -> 09060102030405FF
     *
     * @param obisCodeAsString The obiscode as string (for example "1.2.3.4.5.255")
     * @return a new OctetString with 6 fields (A.B.C.D.E.F)
     */
    public static OctetString fromObisCode(String obisCodeAsString) {
        return OctetString.fromByteArray(ObisCode.fromString(obisCodeAsString).getLN());
    }

    /**
     * Create a new OctetString with the given bytes as content,
     * from contentBytes[0] up to contentBytes[length-1]
     * <p/>
     * Ex: 01020304050607, with length 4 -> 090401020304
     *
     * @param contentBytes The raw bytes as content of the OctetString
     * @param length       The number of bytes to use from the contentBytes.
     * @return new OctetString with the given bytes as content
     */
    public static OctetString fromByteArray(byte[] contentBytes, int length) {
        return new OctetString(contentBytes, length, 0);
    }

    /**
     * Create a new OctetString with the given bytes as content
     * Ex: 01020304050607 -> 090701020304050607
     *
     * @param contentBytes The raw bytes as content of the OctetString
     * @return new OctetString with the given bytes as content
     */
    public static OctetString fromByteArray(byte[] contentBytes) {
        return OctetString.fromByteArray(contentBytes, contentBytes.length);
    }

    /**
     * Convert the old {@link com.energyict.dlms.OctetString} to the new AXDR version.
     * This method simply takes the content of the old octet string as byte array, and creates a new
     * one using the same bytes.
     *
     * @param oldOctetString The old com.energyict.dlms.OctetString
     * @return The new com.energyict.dlms.axrdencoding.OctetString
     */
    public static OctetString fromOldOctetString(com.energyict.dlms.OctetString oldOctetString) {
        return OctetString.fromByteArray(oldOctetString.getArray());
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i = 0; i < getLevel(); i++) {
            strBuffTab.append("  ");
        }
        return strBuffTab.toString() + "OctetString=" + ProtocolUtils.outputHexString(getOctetStr()) + "\n";
    }

}
