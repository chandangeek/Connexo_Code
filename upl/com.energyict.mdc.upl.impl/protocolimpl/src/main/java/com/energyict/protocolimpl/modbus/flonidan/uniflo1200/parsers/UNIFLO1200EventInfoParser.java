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

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.events.UNIFLO1200EventInfo;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.loadprofile.UNIFLO1200ProfileInfo;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200HoldingRegister;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200Registers;

/**
 * @author jme
 *
 */
public class UNIFLO1200EventInfoParser {

	private UNIFLO1200HoldingRegister logIdxReg = null;
	private UNIFLO1200HoldingRegister logInfoReg = null;
	private UNIFLO1200HoldingRegister logSizeReg = null;
	private UNIFLO1200HoldingRegister logWidthReg = null;
	private UNIFLO1200HoldingRegister logEepromReg = null;

	private UNIFLO1200EventInfo profileInfo = null; 

	private byte[] logInfoValue;
	private int logIdxValue;
	private int logSizeValue;
	private int logWidthValue;
	private int logEepromValue;

	/*
	 * Constructors
	 */

	public UNIFLO1200EventInfoParser(UNIFLO1200HoldingRegister logIdxReg,
			UNIFLO1200HoldingRegister logInfoReg,
			UNIFLO1200HoldingRegister logSizeReg,
			UNIFLO1200HoldingRegister logWidthReg,
			UNIFLO1200HoldingRegister logEepromReg,
			UNIFLO1200EventInfo eventInfo) throws IOException {
		
		this.profileInfo = eventInfo;
		
		this.logIdxReg = logIdxReg;
		this.logInfoReg = logInfoReg;
		this.logSizeReg = logSizeReg;
		this.logWidthReg = logWidthReg;
		this.logEepromReg = logEepromReg;
		
		updateLogInfoRegisters();
		
	}

	/*
	 * Private getters, setters and methods
	 */
	
	private UNIFLO1200EventInfo getProfileInfo() {
		return profileInfo;
	}
	
	private UNIFLO1200Registers getUNIFLO1200Registers() {
		return ((UNIFLO1200RegisterFactory)getProfileInfo().getLoadProfile().getUniflo1200().getRegisterFactory()).getFwRegisters();
	}
	
	private UNIFLO1200HoldingRegister getLogIdxReg() {
		return logIdxReg;
	}

	private UNIFLO1200HoldingRegister getLogInfoReg() {
		return logInfoReg;
	}

	private UNIFLO1200HoldingRegister getLogSizeReg() {
		return logSizeReg;
	}

	private UNIFLO1200HoldingRegister getLogWidthReg() {
		return logWidthReg;
	}

	private UNIFLO1200HoldingRegister getLogEepromReg() {
		return logEepromReg;
	}
	
	/*
	 * Public methods
	 */
	
	public List buildChannelInfos() throws IOException {
		List channelInfos = new ArrayList();
		int numberOfChannels = getNumberOfChannels();
		
		for (int channelIndex = 0; channelIndex < numberOfChannels; channelIndex++) {
			int channelRegisterID = logInfoValue[channelIndex] & 0x000000FF;
			String channelName = "Channel " + channelIndex;
			Unit channelUnit = Unit.get(getUNIFLO1200Registers().getUnitString(channelRegisterID));
			if (channelUnit == null) channelUnit = Unit.get("");
			channelInfos.add(new ChannelInfo(channelIndex, channelName, channelUnit));
		}
		
		return channelInfos;
	}

	public List buildChannelRegisters() throws IOException {
		List channelRegisters = new ArrayList();
		UNIFLO1200HoldingRegister reg;

		int numberOfChannels = getNumberOfChannels();
		int offset = UNIFLO1200Parsers.LENGTH_TIME;
		
		for (int channelIndex = 0; channelIndex < numberOfChannels; channelIndex++) {
			int channelRegisterID = logInfoValue[channelIndex] & 0x000000FF;
			int numberOfWords = getUNIFLO1200Registers().getDataLength(channelRegisterID);
			String channelParser = getUNIFLO1200Registers().getParser(channelRegisterID);
			Unit channelUnit = Unit.get(getUNIFLO1200Registers().getUnitString(channelRegisterID));
			
			if (channelUnit == null) channelUnit = Unit.get("");
			String channelName = "TempChannelRegister" + channelIndex;

			reg = new UNIFLO1200HoldingRegister(offset, numberOfWords, channelName, 0, null);
			reg.setParser(channelParser);
			offset += numberOfWords;
			channelRegisters.add(reg);
		}
		
		return channelRegisters;
	}
	
	public void updateLogInfoRegisters() throws IOException {
		this.logIdxValue = (Integer) getLogIdxReg().value();
		this.logInfoValue = (byte[]) getLogInfoReg().value();
		this.logSizeValue = (Integer) getLogSizeReg().value();
		this.logWidthValue = (Integer) getLogWidthReg().value();
		if (getLogEepromReg() != null) {
			this.logEepromValue = (Integer) getLogEepromReg().value();
		} else {
			this.logEepromValue = 1;
		}
	}

	/*
	 * Public getters and setters
	 */
	
	public int getNumberOfChannels() {
		return this.logWidthValue;
	}

	public byte[] getLogInfoValue() {
		return logInfoValue;
	}

	public int getLogIdxValue() {
		return logIdxValue;
	}

	public int getLogSizeValue() {
		return logSizeValue;
	}

	public int getLogWidthValue() {
		return logWidthValue;
	}

	public int getNumberOfLogPoints() {
		return getLogWidthValue();
	}
	
	public int getNumberOfLogs() {
		return getLogEepromValue() * getLogSizeValue();
	}

	public int getLogEepromValue() {
		return logEepromValue;
	}
	
}
