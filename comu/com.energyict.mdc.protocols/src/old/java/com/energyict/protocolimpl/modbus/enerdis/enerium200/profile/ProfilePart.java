/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.enerium200.profile;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.RegisterFactory;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProfilePart {

	private static final int DEBUG 		= 0;

	//FIXME: Used for debugging should be cleaned up
	private static final int READ_EXTRA	= 1;
	private static final int MAX_READ_PERIOD 	= 12;

	private ProfileInfoEntry profileInfoEntry 	= null;
	private Modbus modBus 						= null;

	/*
	 * Constructors
	 */

	public ProfilePart(ProfileInfoEntry profileInfoEntry, Modbus modBus) {
		this.modBus = modBus;
		this.profileInfoEntry = profileInfoEntry;
	}

	/*
	 * Private getters, setters and methods
	 */

	private Date getStartDate() {
		return getProfileInfoEntry().getStartTime();
	}
	private Date getEndDate() {
		return getProfileInfoEntry().getEndTime();
	}

	private RegisterFactory getRegisterFactory() {
		return (RegisterFactory) this.modBus.getRegisterFactory();
	}

	/*
	 * Public methods
	 */

	public List getIntervalDatas(Date from, Date to) throws IOException {
		List intervalDatas = new ArrayList(0);

		if (from.getTime() > getEndDate().getTime()) return intervalDatas;
		if (to.getTime() < getStartDate().getTime()) return intervalDatas;

		Date readFrom = from;
		Date readTo	= to;

		if (readFrom.getTime() < getStartDate().getTime()) readFrom = getStartDate();
		if (readTo.getTime() > getEndDate().getTime()) readTo = getEndDate();

		Calendar calFrom = Utils.getCalendarFromDate(readFrom, modBus);
		Calendar calTo = Utils.getCalendarFromDate(readFrom, modBus);

		do{
			calTo.add(Calendar.HOUR, MAX_READ_PERIOD);
			if(calTo.getTimeInMillis() > readTo.getTime()) calTo.setTime(readTo);

			if (DEBUG >= 1) System.out.println("Reading from: " + calFrom.getTime() + " to " + calTo.getTime());
			intervalDatas.addAll(parse(readRawData(calFrom.getTime(), calTo.getTime())));

			calFrom.setTime(calTo.getTime());
		} while(calTo.getTimeInMillis() < readTo.getTime());

		intervalDatas = Utils.sortIntervalDatas(intervalDatas);

		Date previousDate = new Date(1L);
		List intervalDatasReturn = new ArrayList(0);
		for (int i = 0; i < intervalDatas.size(); i++) {
			 if (previousDate.getTime() != ((IntervalData)intervalDatas.get(i)).getEndTime().getTime()) {
				 intervalDatasReturn.add((IntervalData)intervalDatas.get(i));
			 }
			 previousDate = ((IntervalData)intervalDatas.get(i)).getEndTime();
		}

		return intervalDatasReturn;
	}

	private List parse(byte[] readRawData) throws ProtocolException {
		List intervalDatas = new ArrayList(0);
		int entryLength = getProfileInfoEntry().getEntryBytes();
		int numberOfEntries = readRawData.length / getProfileInfoEntry().getEntryBytes();

		if ((readRawData.length % entryLength) != 0)
			throw new ProtocolException("ProfilePart.parse() readRawData has wrong length: " + readRawData.length);

		for (int i = 0; i < numberOfEntries; i++) {
			byte[] singleProfileEntry = ProtocolUtils.getSubArray2(readRawData, i * entryLength, entryLength);
			ProfilePartEntry ppe = new ProfilePartEntry(singleProfileEntry, getProfileInfoEntry());
			IntervalData intervalData = ppe.getIntervalData();
			if (intervalData != null) {
				intervalDatas.add(intervalData);
			}
		}

		if (DEBUG >= 1) System.out.println("");

		return intervalDatas;
	}


	//FIXME: Method should be cleaned up by reading the EXACT profile block length
	private byte[] readRawData(Date readFrom, Date readTo) throws IOException {
		int intervalsToRead = (int) (((readTo.getTime() - readFrom.getTime()) / (getProfileInfoEntry().getInterval() * 1000)) + 2);
		int readLength = getProfileInfoEntry().getEntryBytes() * (intervalsToRead + READ_EXTRA);
		setProfilePart(getProfileInfoEntry().getEntryID(), readFrom);
		return Utils.readByteValues(getRegisterFactory().readProfileReg.getReg(), readLength / 2, this.modBus);
	}

	private void setProfilePart(int entryID, Date readFrom) throws IOException {
		if (DEBUG >= 1) System.out.println("DEBUG: Entering ProfilePart, setProfilePart(entryID, readFrom)");
		byte[] rawProfilePardId	= Utils.shortToBytes((short) (entryID & 0x0000FFFF));
		byte[] rawFromDate = TimeDateParser.getBytesFromDate(readFrom);
		rawProfilePardId = ProtocolUtils.concatByteArrays(rawProfilePardId, new byte[] {0x00, 0x00});
		byte[] rawData = ProtocolUtils.concatByteArrays(rawProfilePardId, rawFromDate);
		Utils.writeRawByteValues(getRegisterFactory().writeFunctionReg.getReg(), Utils.SETPROFILEPART, rawData, this.modBus);
	}

	/*
	 * Public getters and setters
	 */

	public ProfileInfoEntry getProfileInfoEntry() {
		return profileInfoEntry;
	}


}
