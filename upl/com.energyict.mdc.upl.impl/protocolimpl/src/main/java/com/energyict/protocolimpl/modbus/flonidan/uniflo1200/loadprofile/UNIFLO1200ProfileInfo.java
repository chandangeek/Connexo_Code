/**
 * UNIFLO1200ProfileInfo.java
 * 
 * Created on 17-dec-2008, 09:48:01 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.loadprofile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200ProfileInfoParser;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200HoldingRegister;

/**
 * @author jme
 *
 */
public class UNIFLO1200ProfileInfo {

	private static final int DEBUG 			= 1;
	private static final int INTERVALLOG 	= 1;
	private static final int DAILYLOG 		= 2;
	private static final int MONTHLOG 		= 3;

	private UNIFLO1200Profile loadProfile;

	private int numberOfChannels;
	private int profileInterval;
	private List channelInfos;

	private UNIFLO1200ProfileInfoParser profileInfoParser;

	
	
	public UNIFLO1200ProfileInfo(UNIFLO1200Profile profile) throws IOException {
		this.loadProfile = profile;
		init();
	}

	private void init() throws IOException {
		
		initProfileInfoParser();
		
		this.numberOfChannels = getProfileInfoParser().getNumberOfChannels();
		this.channelInfos = getProfileInfoParser().buildChannelInfos();
		int profileNumber = getLoadProfileNumber();
		
		switch (profileNumber) {
		case INTERVALLOG: 
			this.profileInterval = 				
				(Integer) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_INTERVAL)
				.value();
			break;
		case DAILYLOG:
			this.profileInterval = 3600 * 24; // 24 hour = 86400 seconds
			break;
		case MONTHLOG: 
			this.profileInterval = 3600 * 24 * 30; // 1 month = around 2592000 seconds
			break;
		default:
			throw new ProtocolException("UNIFLO1200ProfileInfo.init(), Invalid loadProfileNumber: " + loadProfile + "!");
		}
		
		System.out.println(this);
		
	}

	public List getChannelInfos() {
		return channelInfos;
	}
	
	private void initProfileInfoParser() throws IOException {
		UNIFLO1200HoldingRegister logIdxReg;
		UNIFLO1200HoldingRegister logInfoReg;
		UNIFLO1200HoldingRegister logSizeReg;
		UNIFLO1200HoldingRegister logWidthReg;
		UNIFLO1200HoldingRegister logEepromReg;

		int profileNumber = getLoadProfileNumber();
		
		switch (profileNumber) {
		case INTERVALLOG: 
			logIdxReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_INTERVAL_LOG_INDEX);
			logInfoReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_INTERVAL_LOG_REG);
			logSizeReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_INTERVAL_LOG_SIZE);
			logWidthReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_INTERVAL_LOG_WIDTH);
			logEepromReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_INTERVAL_LOG_EEPROM);
			break;
		
		case DAILYLOG: 
			logIdxReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_DAILY_LOG_INDEX);
			logInfoReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_DAILY_LOG_REG);
			logSizeReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_DAILY_LOG_SIZE);
			logWidthReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_DAILY_LOG_WIDTH);
			logEepromReg = null; 
			break;
		
		case MONTHLOG: 
			logIdxReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_MONTH_LOG_INDEX);
			logInfoReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_MONTH_LOG_REG);
			logSizeReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_MONTH_LOG_SIZE);
			logWidthReg = 
				(UNIFLO1200HoldingRegister) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_MONTH_LOG_WIDTH);
			logEepromReg = null; 
			break;

		default:
			throw new ProtocolException("UNIFLO1200ProfileInfo.init(), Invalid loadProfileNumber: " + loadProfile + "!");
		}
		
		profileInfoParser = new UNIFLO1200ProfileInfoParser(logIdxReg, logInfoReg, logSizeReg, logWidthReg, logEepromReg, this);

	}

	private int getLoadProfileNumber() {
		return getLoadProfile().getLoadProfileNumber();
	}

	private UNIFLO1200ProfileInfoParser getProfileInfoParser() {
		return profileInfoParser;
	}
	
	public UNIFLO1200Profile getLoadProfile() {
		return loadProfile;
	}

	public int getProfileInterval() {
		return this.profileInterval;
	}
	
	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	public String toString() {
		String message = 
			"UNIFLO1200ProfileInfo: \r\n" +
			"   loadProfileNr    = " + getLoadProfileNumber() + "\r\n" +
			"   numberOfChannels = " + getNumberOfChannels() + "\r\n";

		if (DEBUG != 0) 
			message += "   DEBUG            = " + DEBUG + "\r\n";
		
		message += "   channelInfos:\r\n";
		for (int i = 0; i < channelInfos.size(); i++) {
			ChannelInfo chi = (ChannelInfo) channelInfos.get(i);
			message += "     [" + i + "] ";
			message += "channelID = " + chi.getChannelId();
			message += ", channelName = " + chi.getName();
			message += ", channelUnit = " + chi.getUnit();
			message += ", profileInterval = " + getProfileInterval();
			message += "\r\n";
		}
		
		return message;
	}
}
