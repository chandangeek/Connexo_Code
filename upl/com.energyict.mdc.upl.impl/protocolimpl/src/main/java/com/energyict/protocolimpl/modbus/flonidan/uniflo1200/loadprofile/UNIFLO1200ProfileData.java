/**
 * UNIFLO1200ProfileData.java
 * 
 * Created on 22-dec-2008, 10:20:00 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.loadprofile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200Parsers;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200ProfileDataParser;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200HoldingRegister;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;

/**
 * @author jme
 *
 */
public class UNIFLO1200ProfileData {

	private static final int DEBUG 			= 1;
	private static final int PROFILE_SIZE 	= 46;
	
	private UNIFLO1200Profile loadProfile;
	private List intervalDatas;

	/*
	 * Constructors
	 */

	public UNIFLO1200ProfileData(UNIFLO1200Profile loadProfile) throws IOException {
		this.loadProfile = loadProfile;
	}

	/*
	 * Private getters, setters and methods
	 */

	private ModbusConnection getModBusConnection() {
		return getLoadProfile().getUniflo1200().getModbusConnection();
	}
	
	private UNIFLO1200RegisterFactory getRegisterFactory() {
		return (UNIFLO1200RegisterFactory) getLoadProfile().getUniflo1200().getRegisterFactory();
	}

	private UNIFLO1200ProfileInfo getProfileInfo() {
		return getLoadProfile().getProfileInfo();
	}
	
	private int buildLogAddress(int start, int numberOfLogPoints, int idx) {
		int returnValue;
		returnValue = start + 32 + idx*(numberOfLogPoints*4 + 6);
		return returnValue;

	}
		
	/*
	 * Public methods
	 */

	public List buildIntervalDatas(Date from, Date to) throws IOException {
		byte[] dataBlock;
		int ptr;
		int div;
		IntervalData intervalData;
		this.intervalDatas = new ArrayList();

		UNIFLO1200HoldingRegister register;
		UNIFLO1200ProfileDataParser profileDataParser = new UNIFLO1200ProfileDataParser(this);
		final int base = getProfileInfo().getLogStartAddress();
		final int nolp = getProfileInfo().getNumberOfLogPoints();
		final int nol = getProfileInfo().getNumberOfLogs();
		final int idx = getProfileInfo().getLoadProfile().getProfileInfo().getLogIndex();

		Date firstTime;
		Date lastTime;
		Date previousTime = new Date(0);
		Date intervalTime = new Date(1);
		
		register = new UNIFLO1200HoldingRegister(buildLogAddress(base, nolp, idx-1), 6, "TempStartDate", getModBusConnection());
		register.setRegisterFactory(getRegisterFactory());
		lastTime = (Date) register.objectValueWithParser(UNIFLO1200Parsers.PARSER_TIME);

		if (to == null) {
			div = 0;
		} else {
			div = (int)((lastTime.getTime() - to.getTime()) / 1000) / getProfileInfo().getProfileInterval();
		}

		if (DEBUG >= 1) System.out.println("div: " + div);

		if (div <= 0) {
			ptr = idx - 1;
		} else {
			ptr = idx - div;
			if (ptr < 0) {
				ptr += nol;
				if (ptr <= idx) return intervalDatas;
			}
		}
		
		do {
			register = new UNIFLO1200HoldingRegister(buildLogAddress(base, nolp, ptr), 6, "TempStartDate", getModBusConnection());
			register.setRegisterFactory(getRegisterFactory());
			firstTime = (Date) register.objectValueWithParser(UNIFLO1200Parsers.PARSER_TIME);
			if (DEBUG >= 1) System.out.println("Searching matching 'to' interval [" + to + "] <> [" + firstTime + "] ...");
			
			if (firstTime.compareTo(lastTime) > 0) {
				if (DEBUG >= 1) System.out.println("firstTime = " + firstTime +	" lastTime = " + lastTime +	"  firstTime.compareTo(lastTime) = " + firstTime.compareTo(lastTime));
				return intervalDatas;
			}
			
			if (firstTime.compareTo(to) <= 0) {
				if (DEBUG >= 1)	System.out.println("firstTime = " + firstTime + " to = " + to +	"  firstTime.compareTo(to) = " + firstTime.compareTo(to));
				break;
			}
			
			if (--ptr < 0) {
				ptr = nol - 1;
				if (DEBUG >= 1) System.out.println("ptr < 0,  ptr = nol - 1 = " + ptr);
			}
			
		} while(true);

		if (DEBUG >= 1) System.out.println("First date: " + firstTime);
		if (DEBUG >= 1) System.out.println("Last date:  " + lastTime);
		
		do {
			register = new UNIFLO1200HoldingRegister(buildLogAddress(base, nolp, ptr), PROFILE_SIZE, "Temp", getModBusConnection());
			register.setRegisterFactory(getRegisterFactory());
			dataBlock = (byte[]) register.objectValueWithParser(UNIFLO1200Parsers.PARSER_DATABLOCK);
			profileDataParser.parseData(dataBlock);
			
			intervalTime = profileDataParser.getTime();
			
			if (intervalTime.compareTo(lastTime) > 0) break;
			if (intervalTime.compareTo(from) <= 0) break;
			
			intervalData = new IntervalData(intervalTime);
			for (int j = 0; j < getProfileInfo().getNumberOfChannels(); j++) {
				intervalData.addValue(profileDataParser.getNumber(j));
			}
			
			if (!previousTime.equals(intervalTime)) {
				intervalDatas.add(intervalData);
			} else {
				if (DEBUG >= 1) System.out.println("WARNING: Duplicate log entry for time " + previousTime + " !!!");
			}
			
			previousTime = intervalTime;
			
			if (DEBUG >= 1) System.out.print("logIdx = " + ptr + " address: " + buildLogAddress(base, nolp, ptr) + " ");
			if (DEBUG >= 1) System.out.print(intervalTime + " ");
			if (DEBUG >= 1) System.out.print(ProtocolUtils.outputHexString(dataBlock));
			if (DEBUG >= 1) System.out.println();
			
			if (--ptr < 0) ptr = nol - 1;
			
		} while(true);
		
		return intervalDatas;
	}

	/*
	 * Public getters and setters
	 */

	public UNIFLO1200Profile getLoadProfile() {
		return loadProfile;
	}

}
