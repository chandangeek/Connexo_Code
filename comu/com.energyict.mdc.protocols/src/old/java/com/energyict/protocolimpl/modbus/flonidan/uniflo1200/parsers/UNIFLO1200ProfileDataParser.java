/**
 * UNIFLO1200ProfileDataParser.java
 *
 * Created on 22-dec-2008, 16:07:03 by jme
 *
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.loadprofile.UNIFLO1200ProfileData;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.loadprofile.UNIFLO1200ProfileInfo;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200HoldingRegister;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author jme
 *
 */
public class UNIFLO1200ProfileDataParser {

	private static final int DEBUG = 0;
	private UNIFLO1200ProfileData profileData;
	private Date timeValue;
	private Number[] channelNumbers;


	/*
	 * Constructors
	 */

	public UNIFLO1200ProfileDataParser(UNIFLO1200ProfileData profileData) {
		this.profileData = profileData;
	}

	/*
	 * Private getters, setters and methods
	 */

	private UNIFLO1200ProfileData getProfileData() {
		return profileData;
	}

	private UNIFLO1200ProfileInfo getProfileInfo() {
		return getProfileData().getLoadProfile().getProfileInfo();
	}

	private int getNumberOfChannels() {
		return getProfileData().getLoadProfile().getNumberOfChannels();
	}

	private Parser getParser(UNIFLO1200HoldingRegister reg) throws IOException {
		return this.getParser(reg.getParser());
	}

	private Parser getParser(String parserName) throws IOException {
		return getRegisterFactory().getParserFactory().get(parserName);
	}

	private UNIFLO1200RegisterFactory getRegisterFactory() {
		return (UNIFLO1200RegisterFactory) getUniflo1200().getRegisterFactory();
	}

	private UNIFLO1200 getUniflo1200() {
		return getProfileData().getLoadProfile().getUniflo1200();
	}

	private int[] parseByteArray2IntArray(byte[] rawData) {
		int[] returnValue = new int[(rawData.length / 2)];
		for (int i = 0; i < returnValue.length; i++) {
			returnValue[i] = (rawData[i*2] & 0x000000FF) << 8;
			returnValue[i] += rawData[(i*2) + 1] & 0x000000FF;
		}
		return returnValue;
	}

    private static int[] getSubArray(int[] data,int offset, int length) {
    	int[] subArray = new int[length];
        for (int i=0;i<subArray.length;i++) {
           subArray[i] = data[i+offset];
        }
        return subArray;
    }

    private String printIntArray(int[] intArray) {
		String returnValue = "";
    	for (int i = 0; i < intArray.length; i++) {
    		returnValue += "$$"+ProtocolUtils.buildStringHex(intArray[i], 4);
		}
    	return returnValue;
    }

	/*
	 * Public methods
	 */

	public void parseData(byte[] rawData) throws IOException {
		List registers = getProfileInfo().getChannelRegisters();
		int[] intData = parseByteArray2IntArray(rawData);
		int noc = getNumberOfChannels();

		if (DEBUG >= 1) {
			System.out.println("RawData = " + ProtocolUtils.outputHexString(rawData));
			System.out.println("IntData = " + printIntArray(intData));
		}

		this.channelNumbers = new Number[noc];
		this.timeValue = (Date) getParser(UNIFLO1200Parsers.PARSER_TIME).val(intData, null);

		for (int i = 0; i < noc; i++) {
			UNIFLO1200HoldingRegister reg = (UNIFLO1200HoldingRegister) registers.get(i);
			registers.get(i);
			try {
				int[] subArray = getSubArray(intData, reg.getReg(), reg.getRange());
				channelNumbers[i] = (Number) getParser(reg).val(subArray, null);
			} catch (NumberFormatException e) {
				channelNumbers[i] = new Integer(0);
			}
		}
	}

	/*
	 * Public getters and setters
	 */

	public Date getTime() {
		return this.timeValue;
	}

	public Number getNumber(int channelIndex) {
		return channelNumbers[channelIndex] ;
	}

}
