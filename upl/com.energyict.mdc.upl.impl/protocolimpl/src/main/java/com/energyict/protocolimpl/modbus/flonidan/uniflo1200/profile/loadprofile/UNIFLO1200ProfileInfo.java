/**
 * UNIFLO1200ProfileInfo.java
 *
 * Created on 17-dec-2008, 09:48:01 by jme
 *
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.loadprofile;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200ProfileInfoParser;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.UNIFLO1200Profile;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200HoldingRegister;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author jme
 *
 */
public class UNIFLO1200ProfileInfo {

	private static final int DEBUG = 0;

	private UNIFLO1200Profile loadProfile;
	private UNIFLO1200ProfileInfoParser profileInfoParser;

	private int numberOfChannels;
	private int logStartAddress;
	private int profileInterval;
	private List channelInfos;

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
		case UNIFLO1200Profile.INTERVALLOG:
			this.profileInterval =
				((Integer) getLoadProfile()
				.getUniflo1200()
				.getRegisterFactory()
				.findRegister(UNIFLO1200RegisterFactory.REG_INTERVAL)
				.value()).intValue();
			this.logStartAddress =
				((UNIFLO1200RegisterFactory)getLoadProfile().getUniflo1200().getRegisterFactory())
				.getFwRegisters()
				.getIntervalLogStartAddress();
			break;
		case UNIFLO1200Profile.DAILYLOG:
			this.profileInterval = 3600 * 24; // 24 hour = 86400 seconds
			this.logStartAddress =
				((UNIFLO1200RegisterFactory)getLoadProfile().getUniflo1200().getRegisterFactory())
				.getFwRegisters()
				.getDailyLogStartAddress();
			break;
		case UNIFLO1200Profile.MONTHLOG:
			this.profileInterval = 3600 * 24 * 30; // 1 month = around 2592000 seconds
			this.logStartAddress =
				((UNIFLO1200RegisterFactory)getLoadProfile().getUniflo1200().getRegisterFactory())
				.getFwRegisters()
				.getMonthLogStartAddress();
			break;
		default:
			throw new ProtocolException("UNIFLO1200ProfileInfo.init(), Invalid loadProfileNumber: " + loadProfile + "!");
		}


		if (DEBUG >= 1) System.out.println(this);

	}

	public List getChannelInfos() {
		return channelInfos;
	}

	public int getLastLogAddress() {
		int returnValue;

		returnValue = getLogStartAddress() + 32;
		returnValue += getProfileInfoParser().getLogIdxValue() * (getProfileInfoParser().getNumberOfLogPoints()*4 + 6);

		return returnValue;
	}

	private void initProfileInfoParser() throws IOException {
		UNIFLO1200HoldingRegister logIdxReg;
		UNIFLO1200HoldingRegister logInfoReg;
		UNIFLO1200HoldingRegister logSizeReg;
		UNIFLO1200HoldingRegister logWidthReg;
		UNIFLO1200HoldingRegister logEepromReg;

		int profileNumber = getLoadProfileNumber();

		switch (profileNumber) {
		case UNIFLO1200Profile.INTERVALLOG:
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

		case UNIFLO1200Profile.DAILYLOG:
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

		case UNIFLO1200Profile.MONTHLOG:
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

	public int getLoadProfileNumber() {
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

	public int getLogStartAddress() {
		return logStartAddress;
	}

	public int getLogIndex() {
		return getProfileInfoParser().getLogIdxValue();
	}

	public List getChannelRegisters() throws IOException {
		return getProfileInfoParser().buildChannelRegisters();
	}

	public String toString() {
		String message =
			"UNIFLO1200ProfileInfo: \r\n" +
			"   DEBUG              = " + DEBUG + "\r\n" +
			"   loadProfileNr      = " + getLoadProfileNumber() + "\r\n" +
			"   profileInterval    = " + getProfileInterval() + " seconds\r\n" +
			"   numberOfChannels   = " + getNumberOfChannels() + "\r\n" +
			"   getNrOfLogPoints   = " + getNumberOfLogPoints() + "\r\n" +
			"   getNumberOfLogs    = " + getNumberOfLogs() + "\r\n" +
			"   getLogIdxValue     = " + getProfileInfoParser().getLogIdxValue() + "\r\n" +
			"   getLogSizeValue    = " + getProfileInfoParser().getLogSizeValue() + "\r\n" +
			"   getLogWidthValue   = " + getProfileInfoParser().getLogWidthValue() + "\r\n" +
			"   getLogEepromValue  = " + getProfileInfoParser().getLogEepromValue() + "\r\n" +
			"   getLogInfoValue    = " + ProtocolUtils.outputHexString(getProfileInfoParser().getLogInfoValue()) + "\r\n" +
			"   getLogStartAddress = 0x" + ProtocolUtils.buildStringHex(getLogStartAddress(), 8) + "\r\n" +
			"   getLastLogAddress  = 0x" + ProtocolUtils.buildStringHex(getLastLogAddress(), 8) + "\r\n" +
			"\r\n";

		message += "   channelInfos:\r\n";
		for (int i = 0; i < channelInfos.size(); i++) {
			ChannelInfo chi = (ChannelInfo) channelInfos.get(i);
			message += "     [" + i + "] ";
			message += "channelID = " + chi.getChannelId();
			message += ", channelName = " + chi.getName();
			message += ", channelUnit = " + chi.getUnit();
			message += "\r\n";
		}

		return message;
	}

	public int getNumberOfLogPoints() {
		return getProfileInfoParser().getNumberOfLogPoints();
	}

	public int getNumberOfLogs() {
		return getProfileInfoParser().getNumberOfLogs();
	}

}
