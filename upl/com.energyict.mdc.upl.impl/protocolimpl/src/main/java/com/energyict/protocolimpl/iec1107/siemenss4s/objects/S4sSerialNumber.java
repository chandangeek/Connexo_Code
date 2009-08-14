package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocol.ProtocolUtils;

/**
 * Contains the serialNumber of the device
 * @author gna
 *
 */
public class S4sSerialNumber {

	private byte[] rawBytes;
	
	public S4sSerialNumber(byte[] rawBytes){
		byte[] temp = S4ObjectUtils.switchNibbles(rawBytes);
		this.rawBytes = DLMSUtils.hexStringToByteArray(new String(temp));		// strange way of parsing a decimal byteArray to a String, back to an ASCII byteArray...
	}
	
	/**
	 * @return the SerialNumber as a String without the ending zeros
	 */
	public String getSerialNumber(){
		return new String(cutEndingZeros());
	}

	/**
	 * Cut the ending zeros of the byteArray because they are not valid ASCII chars
	 * @return the serialNumbers byteArray without the ending zeros
	 */
	private byte[] cutEndingZeros(){
		int to;
		for(to = 0; to < this.rawBytes.length; to++ ){
			if(this.rawBytes[to] == 0){
				break;
			}
		}
		return ProtocolUtils.getSubArray(this.rawBytes, 0, to-1);
	}
}
