/**
 * UNIFLO1200LogRegParser.java
 * 
 * Created on 19-dec-2008, 10:34:16 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.loadprofile.UNIFLO1200ProfileInfo;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200HoldingRegister;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200Registers;

/**
 * @author jme
 *
 */
public class UNIFLO1200ProfileInfoParser {

	private UNIFLO1200HoldingRegister logIdxReg = null;
	private UNIFLO1200HoldingRegister logInfoReg = null;
	private UNIFLO1200HoldingRegister logSizeReg = null;
	private UNIFLO1200HoldingRegister logWidthReg = null;
	private UNIFLO1200HoldingRegister logEepromReg = null;

	private UNIFLO1200ProfileInfo profileInfo = null; 

	private byte[] logInfoValue;
	private int logIdxValue;
	private int logSizeValue;
	private int logWidthValue;

	public UNIFLO1200ProfileInfoParser(UNIFLO1200HoldingRegister logIdxReg,
			UNIFLO1200HoldingRegister logInfoReg,
			UNIFLO1200HoldingRegister logSizeReg,
			UNIFLO1200HoldingRegister logWidthReg,
			UNIFLO1200HoldingRegister logEepromReg,
			UNIFLO1200ProfileInfo profileInfo) throws IOException {
		
		this.profileInfo = profileInfo;
		
		this.logIdxReg = logIdxReg;
		this.logInfoReg = logInfoReg;
		this.logSizeReg = logSizeReg;
		this.logWidthReg = logWidthReg;
		this.logEepromReg = logEepromReg;
		
		this.logIdxValue = (Integer) logIdxReg.value();
		this.logInfoValue = (byte[]) logInfoReg.value();
		this.logSizeValue = (Integer) logSizeReg.value();
		this.logWidthValue = (Integer) logWidthReg.value();
		
	}

	public List buildChannelInfos() throws IOException {
		List channelInfos = new ArrayList();
		int numberOfChannels = getNumberOfChannels();
		
		for (int i = 0; i < numberOfChannels; i++) {
			int channelRegisterID = logInfoValue[i] & 0x000000FF;
			String channelName = "Channel " + i;
			Unit channelUnit = Unit.get(getUNIFLO1200Registers().getUnitString(channelRegisterID));
			if (channelUnit == null) channelUnit = Unit.get("");
			channelInfos.add(new ChannelInfo(i, channelName, channelUnit));
		}
		
		return channelInfos;
	}

	public int getNumberOfChannels() {
		return this.logWidthValue;
	}
	
	private UNIFLO1200ProfileInfo getProfileInfo() {
		return profileInfo;
	}
	
	private UNIFLO1200Registers getUNIFLO1200Registers() {
		return ((UNIFLO1200RegisterFactory)getProfileInfo().getLoadProfile().getUniflo1200().getRegisterFactory()).getFwRegisters();
	}
	
}
