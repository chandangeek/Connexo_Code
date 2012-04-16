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
        final AxdrType axdrType = AxdrType.fromTag(data[offset]);
        switch (axdrType) {
			case NULL:
				return new NullData(data, offset);
			case ARRAY:
				return new Array(data, offset, level);
			case STRUCTURE:
				return new Structure(data, offset, level);
			case INTEGER:
				return new Integer8(data, offset);
			case LONG:
				return new Integer16(data, offset);
			case DOUBLE_LONG:
				return new Integer32(data, offset);
			case UNSIGNED:
				return new Unsigned8(data, offset);
			case LONG_UNSIGNED:
				return new Unsigned16(data, offset);
			case ENUM:
				return new TypeEnum(data, offset);
			case BIT_STRING:
				return new BitString(data, offset);
			case VISIBLE_STRING:
				return new VisibleString(data, offset);
			case OCTET_STRING:
				return new OctetString(data, offset);
			case DOUBLE_LONG_UNSIGNED:
				return new Unsigned32(data, offset);
			case LONG64:
                return new Integer64(data, offset);
            case LONG64_UNSIGNED:
                return new Integer64(DLMSUtils.getUnsignedIntFromBytes(data, offset + 1, Integer64.LENGTH));  //+1 to skip the data type byte
            case BOOLEAN:
                return new BooleanObject(data, offset);
            case FLOAT32:
                return new Float32(data, offset);
			default:
				throw new IOException("AXDRDecoder, unknown datatype " + data[offset]);
		}
	}
}