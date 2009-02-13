package com.energyict.protocolimpl.modbus.enerdis.enerium200.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.RegisterFactory;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;

public class ProfilePart {

	private static final int DEBUG 		= 0;
	
	//FIXME: Used for debugging should be cleaned up 
	private static final int READ_EXTRA	= 5;  
	
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
		
		parse(readRawData(readFrom, readTo));
		
		return intervalDatas;
	}

	private void parse(byte[] readRawData) {
		ProtocolUtils.printResponseData(readRawData);
	}

	//FIXME: Method should be cleaned up by reading the EXACT profile block length
	private byte[] readRawData(Date readFrom, Date readTo) throws IOException {
		int intervalsToRead = (int) (((readTo.getTime() - readFrom.getTime()) / (getProfileInfoEntry().getInterval() * 1000)) + 2);
		int readLength = getProfileInfoEntry().getEntryBytes() * (intervalsToRead + READ_EXTRA); 
		setProfilePart(getProfileInfoEntry().getEntryID(), readFrom);
		
		return Utils.readByteValues(getRegisterFactory().readProfileReg.getReg(), readLength / 2, this.modBus);
	}

	private void setProfilePart(int entryID, Date readFrom) throws IOException {
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
