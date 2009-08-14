package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import com.energyict.protocol.ProtocolUtils;

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
	
}
