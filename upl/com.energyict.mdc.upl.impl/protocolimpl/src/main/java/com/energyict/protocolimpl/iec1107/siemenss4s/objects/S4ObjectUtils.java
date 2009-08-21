package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import com.energyict.protocol.ProtocolUtils;

/**
 * Useful utils for operations with the S4s data
 * @author gna
 *
 */
public class S4ObjectUtils {

	/**
	 * Switches the nibbles in the byteArray
	 * @param lsbNibbleByte
	 * @return
	 */
	public static byte[] switchNibbles(byte[] lsbNibbleByte){
		int offset = 0;
		StringBuffer strBuff = new StringBuffer();
		StringBuffer strBuff2 = new StringBuffer();
		for(int i = 0; i < lsbNibbleByte.length/2; i++){
			strBuff = new StringBuffer();
			strBuff.append(new String(ProtocolUtils.getSubArray2(lsbNibbleByte, i+offset+1, 1)));
			strBuff.append(new String(ProtocolUtils.getSubArray2(lsbNibbleByte, i+offset, 1)));
			strBuff2.append(strBuff.toString());
			offset += 1;
		}
		return strBuff2.toString().getBytes();
	}
	
	/**
	 * Revert from LSB-MBS to MSB-LSB
	 * @param array - the Array to revert
	 * @return an inverted array
	 */
	public static byte[] revertByteArray(byte[] array){
		byte[] reverse = new byte[array.length];
		int offset = array.length-1;
		for(int i = 0; i < array.length; i++,offset--){
			reverse[i] = array[offset];
		}
		return reverse;
	}
	
}
