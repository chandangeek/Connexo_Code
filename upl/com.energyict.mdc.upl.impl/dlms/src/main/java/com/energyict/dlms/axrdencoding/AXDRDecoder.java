/*
 * AXDRDecoder.java
 *
 * Created on 17 oktober 2007, 15:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.DLMSUtils;

import java.io.IOException;

/**
 * Util class to convert (parse) a byte array to a {@link AbstractDataType}
 *
 * @author kvds
 */
public final class AXDRDecoder {

	/**
	 * Util class with static methods, so hide the constructor
	 */
	private AXDRDecoder() {

	}

	/**
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static AbstractDataType decode(byte[] data) throws IOException {
    	return decode(data,0);
    }

    /**
     * @param data
     * @param offset
     * @return
     * @throws IOException
     */
    public static AbstractDataType decode(byte[] data,int offset) throws IOException {
        return decode(data,offset,0);
    }

	/**
	 * @param data
	 * @param offset
	 * @param level
	 * @return
	 * @throws IOException
	 */
	public static AbstractDataType decode(byte[] data, int offset, int level) throws IOException {
		switch (data[offset]) {
			case AxdrType.NULL.getTag():
				return new NullData(data, offset);
			case AxdrType.ARRAY.getTag():
				return new Array(data, offset, level);
			case AxdrType.STRUCTURE.getTag():
				return new Structure(data, offset, level);
			case AxdrType.INTEGER.getTag():
				return new Integer8(data, offset);
			case AxdrType.LONG.getTag():
				return new Integer16(data, offset);
			case AxdrType.DOUBLE_LONG.getTag():
				return new Integer32(data, offset);
			case AxdrType.UNSIGNED.getTag():
				return new Unsigned8(data, offset);
			case AxdrType.LONG_UNSIGNED.getTag():
				return new Unsigned16(data, offset);
			case AxdrType.ENUM.getTag():
				return new TypeEnum(data, offset);
			case AxdrType.BIT_STRING.getTag():
				return new BitString(data, offset);
			case AxdrType.VISIBLE_STRING.getTag():
				return new VisibleString(data, offset);
			case AxdrType.OCTET_STRING.getTag():
				return new OctetString(data, offset);
			case AxdrType.DOUBLE_LONG_UNSIGNED.getTag():
				return new Unsigned32(data, offset);
			case AxdrType.LONG64.getTag():
                return new Integer64(data, offset);
            case AxdrType.LONG64_UNSIGNED.getTag():
                return new Integer64(DLMSUtils.getUnsignedIntFromBytes(data, offset + 1, Integer64.LENGTH));  //+1 to skip the data type byte
            case AxdrType.BOOLEAN.getTag():
                return new BooleanObject(data, offset);
            case AxdrType.FLOAT32.getTag():
                return new Float32(data, offset);
			default:
				throw new IOException("AXDRDecoder, unknown datatype " + data[offset]);
		}
	}
}