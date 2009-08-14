package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSUtils;

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
	 * @param unitTotalRegisterA
	 */
	public S4sRegisterConfig(byte[] unitTotalRegisterA) {
		this.rawBytes = DLMSUtils.hexStringToByteArray(new String(S4ObjectUtils.switchNibbles(unitTotalRegisterA)));
	}
	
	/**
	 * @return the unit
	 */
	public Unit getUnit(){
		if(this.rawBytes[0] == 11){
			return Unit.getUndefined();
		} else {
			return this.possibleUnits[this.rawBytes[0]&0x0F];
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
		return this.possibleTypes[(this.rawBytes[0]&0xC0)>>6];
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
}
