/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

/**
 * Contains the configuration of a Register, or a ChannelInfo object configuration!
 * @author gna
 *
 */
public class S4sRegisterConfig {

	private static Unit[] possibleUnits = new Unit[]{Unit.get(BaseUnit.WATTHOUR),						// Wh
														Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR),		// VArh
														Unit.get(BaseUnit.VOLTAMPEREHOUR),				// VAh
														Unit.get(BaseUnit.WATTHOUR,3),					// kWh
														Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,3),	// kVArh
														Unit.get(BaseUnit.VOLTAMPEREHOUR,3),			// kVAh
														Unit.get(BaseUnit.WATTHOUR,6),					// MWh
														Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,6),	// MVArh
														Unit.get(BaseUnit.VOLTAMPEREHOUR,6)};			// MVAh
	private static String[] possibleTypes = new String[]{"Undefined", "Import", "Export", "Undefined"};

	private byte[] rawBytes;

	/**
	 * Creates new instance of the registerConfigObject
	 * @param unitTotalRegister - rawData of the register configuration
	 * @param unitType - Select the units you want to use in the registerConfiguration
	 */
	public S4sRegisterConfig(byte[] unitTotalRegister) {
		this.rawBytes = S4sObjectUtils.hexStringToByteArray(new String(S4sObjectUtils.switchNibbles(unitTotalRegister)));
		System.out.println(toString());
	}

	/**
	 * @return the unit
	 */
	public Unit getUnit(){
		if(this.rawBytes[0] == 11){
			return Unit.getUndefined();
		} else {
			return possibleUnits[this.rawBytes[0]&0x0F];
		}
	}

	/**
	 * @return the amount of decimals behind the comma
	 */
	public int getDecimals(){
		return (this.rawBytes[0]&0x30)>>4;
	}

	/**
	 * @return the type of the channel/register (import/export/undefined)
	 */
	public String getType(){
		return possibleTypes[(this.rawBytes[0]&0xC0)>>6];
	}

	/**
	 * Check if the register or channelConfig is used or not
	 * @return
	 */
	public boolean isValid(){
		if(this.rawBytes[0] == 11){
			return false;
		}
		return true;
	}

	public String toString(){
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("RegisterDefinition: ");
		strBuff.append("'\t - Unit: " + getUnit());
		strBuff.append("'\t - Decimals: " + getDecimals());
		strBuff.append("'\t - Type: " + getType());
		return strBuff.toString();
	}
}
