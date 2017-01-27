package com.energyict.protocolimpl.modbus.enerdis.enerium200.profile;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Date;

public class ProfilePartEntry {

	private static final int DEBUG = 0;

	private static final int MAX_CHANNELS 		= 8;

	private static final int TIMESTAMP_OFFSET 	= 0;
	private static final int TIMESTAMP_LENGTH 	= 4;
	private static final int STATUS_OFFSET 		= 4;
	private static final int STATUS_LENGTH 		= 4;
	private static final int CHANNEL_OFFSET 	= 8;
	private static final int CHANNEL_LENGTH 	= 4;

	private IntervalData intervalData 			= null;
	private ProfileInfoEntry profileInfoEntry 	= null;
	private Modbus modBus						= null;
	private Date timeStamp 						= null;

	private int meterStatusValue				= 0;

	/*
	 * Constructors
	 */

	public ProfilePartEntry() {}

	public ProfilePartEntry(byte[] singleProfileEntry, ProfileInfoEntry profileInfoEntry) throws ProtocolException {
		this.modBus = profileInfoEntry.getModBus();
		this.profileInfoEntry = profileInfoEntry;
		this.intervalData = parseSingleEntry(singleProfileEntry);

		if (DEBUG >= 2) System.out.println(this);

	}

	/*
	 * Private getters, setters and methods
	 */

	private ProfileInfoEntry getProfileInfoEntry() {
		return profileInfoEntry;
	}

	private IntervalData parseSingleEntry(byte[] singleProfileEntry) throws ProtocolException {
		int channelPos = 0;
		TimeDateParser td_parser = new TimeDateParser(modBus.gettimeZone());
		Date timeStamp = td_parser.parseTime(ProtocolUtils.getSubArray2(singleProfileEntry , TIMESTAMP_OFFSET, TIMESTAMP_LENGTH));

		this.timeStamp = timeStamp;

		this.meterStatusValue = ProtocolUtils.getInt(ProtocolUtils.getSubArray2(singleProfileEntry, STATUS_OFFSET, STATUS_LENGTH));
		if (this.meterStatusValue == 0) return null;

		IntervalData intervalData = new IntervalData(timeStamp, IntervalData.OK, this.meterStatusValue);

		for (int i = 0; i < MAX_CHANNELS; i++) {
			if (getProfileInfoEntry().isChannelEnabled(i)) {
				byte[] rawChannelValue = ProtocolUtils.getSubArray2(singleProfileEntry, (channelPos * CHANNEL_LENGTH) + CHANNEL_OFFSET,	CHANNEL_LENGTH);
				int channelValue = ProtocolUtils.getInt(rawChannelValue);
				intervalData.addValue(new Integer(channelValue));
				intervalData.setEiStatus(i, IntervalData.OK);
				channelPos++;
			} else {
				intervalData.addValue(new Integer(0));
				intervalData.setEiStatus(i, IntervalData.MISSING);
			}
		}

		if (DEBUG >= 1) {
			System.out.print("endTime = " + intervalData.getEndTime() + " ");
			System.out.print("protocol = " + ProtocolUtils.buildStringHex(meterStatusValue, 8)  + " ");
			for (int i = 0; i < intervalData.getValueCount(); i++) {
				Number value = intervalData.get(i);
				System.out.print("CH" + i + " = " + value.intValue() + "\t");
			}
			System.out.println("");
		}

		return intervalData;
	}

	/*
	 * Public getters and setters
	 */

	public IntervalData getIntervalData() {
		return intervalData;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public int getMeterStatusValue() {
		return meterStatusValue;
	}

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ProfilePartEntry:");
        strBuff.append(" profilePartId="+getProfileInfoEntry().getEntryID());
        strBuff.append(" entries="+getProfileInfoEntry().getEntries());
        strBuff.append(" timeStamp="+getTimeStamp());
        strBuff.append(" meterStatusValue="+ProtocolUtils.buildStringHex(getMeterStatusValue(), 8));
        return strBuff.toString();
    }

}
