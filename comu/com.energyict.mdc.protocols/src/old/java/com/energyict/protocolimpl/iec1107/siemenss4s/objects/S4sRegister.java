package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import com.energyict.cbo.Quantity;

public class S4sRegister {

	private byte[] rawBytes;
	private S4sRegisterConfig channelInfo;

	/**
	 * Creates a new instance of the S4s register config
	 * @param rawData - contains the value
	 * @param registerConfig - defines the configuration(channelConfig)
	 */
	public S4sRegister(byte[] rawData, S4sRegisterConfig registerConfig){
		this.rawBytes = S4sObjectUtils.revertByteArray(rawData);
		this.channelInfo = registerConfig;
	}

	/**
	 * @return the Quantity from the register
	 */
	public Quantity getRegisterQuantity(){
		return new Quantity(Long.valueOf(new String(rawBytes)), channelInfo.getUnit());
	}

}
