/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * UNIFLO1200ProfileData.java
 *
 * Created on 22-dec-2008, 10:20:00 by jme
 *
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200EventDataParser;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200Parsers;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.UNIFLO1200Profile;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200HoldingRegister;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author jme
 *
 */
public class UNIFLO1200EventData {

	private static final int DEBUG 			= 0;
	private static final int EVENT_SIZE 	= 5;

	private UNIFLO1200Profile loadProfile;

	private int alarmIdx;
	private List eventDatas;
	private int eventLogStartAddress;

	/*
	 * Constructors
	 */

	public UNIFLO1200EventData(UNIFLO1200Profile loadProfile) throws IOException {
		this.loadProfile = loadProfile;
		init();
	}

	/*
	 * Private getters, setters and methods
	 */

	private void init() throws IOException {
		this.eventLogStartAddress = getRegisterFactory().getFwRegisters().getEventLogStartAddress();

		UNIFLO1200HoldingRegister alarmIdxReg =
			(UNIFLO1200HoldingRegister) getLoadProfile()
			.getUniflo1200()
			.getRegisterFactory()
			.findRegister(UNIFLO1200RegisterFactory.REG_ALARM_LOG_INDEX);

		this.alarmIdx = ((Integer) alarmIdxReg.value()).intValue();

	}

	private ModbusConnection getModBusConnection() {
		return getLoadProfile().getUniflo1200().getModbusConnection();
	}

	private UNIFLO1200RegisterFactory getRegisterFactory() {
		return (UNIFLO1200RegisterFactory) getLoadProfile().getUniflo1200().getRegisterFactory();
	}

	private int getAlarmIdx() {
		return alarmIdx;
	}

	private int getEventLogStartAddress() throws IOException {
		return eventLogStartAddress;
	}

	private int buildEventAddress(int start, int idx) throws IOException {
		int returnValue;
		returnValue = start + idx*10;
		return returnValue;
	}

    private static List checkOnOverlappingEvents(List meterEvents) {
    	Map eventsMap = new HashMap();
        int size = meterEvents.size();
	    for (int i = 0; i < size; i++) {
	    	MeterEvent event = (MeterEvent) meterEvents.get(i);
	    	Date time = event.getTime();
	    	MeterEvent eventInMap = (MeterEvent) eventsMap.get(time);
	    	while (eventInMap != null) {
	    		time.setTime(time.getTime() + 1000); // add one second
				eventInMap = (MeterEvent) eventsMap.get(time);
	    	}
	    	MeterEvent newMeterEvent=
	    		new MeterEvent(time, event.getEiCode(), event.getProtocolCode(),event.getMessage());
    		eventsMap.put(time, newMeterEvent);
	    }
	    Iterator it = eventsMap.values().iterator();
		List result = new ArrayList();
	    while (it.hasNext())
	        result.add((MeterEvent) it.next());
		return result;
    }

	/*
	 * Public methods
	 */

	public void debugMemDump() throws IOException {
		byte[] dataBlock;
		int ptr;

		UNIFLO1200HoldingRegister register;
		UNIFLO1200EventDataParser eventDataParser = new UNIFLO1200EventDataParser(this);
		final int base = getEventLogStartAddress();
		final int nol = 100;
		final int idx = getAlarmIdx();

		Date intervalTime = new Date(1);
		int eiEventCode;
		int logType;
		String description;

		ptr = 0;

		System.out.println("alarmIdx = " + getAlarmIdx());

		do {
			register = new UNIFLO1200HoldingRegister(buildEventAddress(base, ptr), EVENT_SIZE, "Temp", getModBusConnection());
			register.setRegisterFactory(getRegisterFactory());
			dataBlock = (byte[]) register.objectValueWithParser(UNIFLO1200Parsers.PARSER_DATABLOCK);
			eventDataParser.parseData(dataBlock);

			intervalTime = eventDataParser.getTime();
			description = eventDataParser.getDescription();
			logType = eventDataParser.getLogType();
			eiEventCode = eventDataParser.getEiserverEventCode();


			System.out.print("logIdx = " + ptr + " address: " + buildEventAddress(base, ptr) + " ");
			System.out.print(intervalTime + " ");
			System.out.print("EISEventCode = " + eiEventCode + " ");
			System.out.print("LogType = " + logType + " ");
			System.out.print("\t" + ProtocolUtils.outputHexString(dataBlock) + " ");
			System.out.print(description + " ");
			System.out.println();

			ptr++;

			if (ptr > nol) break;

		} while(true);

	}


	public List buildEventDatas(Date from, Date to) throws IOException {
		this.eventDatas = new ArrayList();
		byte[] dataBlock;
		int ptr;
		int div;
		final int base = getEventLogStartAddress();
		final int nol = 100;
		final int idx = getAlarmIdx();
		UNIFLO1200HoldingRegister register;
		UNIFLO1200EventDataParser eventDataParser = new UNIFLO1200EventDataParser(this);

		Date intervalTime = new Date(1);
		int eiEventCode;
		int logType;
		String description;

		Date firstTime;
		Date lastTime;
		Date previousTime = new Date(Long.MAX_VALUE);

		register = new UNIFLO1200HoldingRegister(buildEventAddress(base, idx-1), 6, "TempEventStartDate", getModBusConnection());
		register.setRegisterFactory(getRegisterFactory());
		lastTime = (Date) register.objectValueWithParser(UNIFLO1200Parsers.PARSER_TIME);

		if (from.compareTo(lastTime) > 0) return this.eventDatas;

		ptr = idx - 1;

		do {

			register = new UNIFLO1200HoldingRegister(buildEventAddress(base, ptr), EVENT_SIZE, "TempEvent", getModBusConnection());
			register.setRegisterFactory(getRegisterFactory());
			dataBlock = (byte[]) register.objectValueWithParser(UNIFLO1200Parsers.PARSER_DATABLOCK);
			eventDataParser.parseData(dataBlock);

			if (previousTime.before(eventDataParser.getTime()) && (!eventDataParser.isTimeChanged())) {
				if (DEBUG >= 1) System.out.println(
						" break: previousTime.after(eventDataParser.getTime()) = " + previousTime.after(eventDataParser.getTime()) +
						" previousTime = " + previousTime +
						" eventDataParser.getTime() = " + eventDataParser.getTime()
					);
				break;
			}

			if (from.after(eventDataParser.getTime())) {
				if (DEBUG >= 1) System.out.println(
						" break: from.after(eventDataParser.getTime()) = " + from.after(eventDataParser.getTime()) +
						" from = " + from +
						" eventDataParser.getTime() = " + eventDataParser.getTime()
					);
				break;
			}

			previousTime = eventDataParser.getTime();

			MeterEvent event = new MeterEvent(
					eventDataParser.getTime(),
					eventDataParser.getEiserverEventCode(),
					eventDataParser.getLogType(),
					eventDataParser.getDescription()
			);

			if (DEBUG >= 1) System.out.println("Events: ptr = " + ptr + " event = " + event.toString());

			if (!to.before(eventDataParser.getTime())) this.eventDatas.add(event);

			if (--ptr < 0) ptr = 99;

		} while(true);

		return checkOnOverlappingEvents(eventDatas);
	}

	/*
	 * Public getters and setters
	 */

	public UNIFLO1200Profile getLoadProfile() {
		return loadProfile;
	}

}
