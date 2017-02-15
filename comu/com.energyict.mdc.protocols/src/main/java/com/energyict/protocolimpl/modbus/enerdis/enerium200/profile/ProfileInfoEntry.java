/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.enerium200.profile;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium150.Enerium150;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;
import com.energyict.protocolimpl.modbus.enerdis.enerium50.Enerium50;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class ProfileInfoEntry {

	private static final int DEBUG 				= 0;

	private static final int MAX_CHANNELS 		= 8;

	private static final int DATE_LENGTH		= 4;
	private static final int ENTRYID_OFFSET 	= 0;
	private static final int CHANNELS_OFFSET 	= 2;
	private static final int STARTTIME_OFFSET 	= 4;
	private static final int ENDTIME_OFFSET 	= 8;
	private static final int INTERVAL_OFFSET 	= 12;

	private static final int CHANNEL_SIZE 		= 4;
	private static final int STATUS_SIZE 		= 4;
	private static final int TIMESTAMP_SIZE		= 4;

	private Modbus modBus	= null;

	private int entryID		= 0;
	private int channels	= 0;
	private Date startTime	= null;
	private Date endTime	= null;
	private int interval	= 0;
	private int entries		= 0;
	private int entryBytes 	= 0;

	private int numberOfchannels		= 0;
	private boolean startOnBoundary 	= false;
	private boolean endOnBoundary 		= false;

    private short loadCurveStatus = -1;

	/*
	 * Constructors
	 */

	public ProfileInfoEntry(byte[] rawData, Modbus modBus) throws IOException {
		this.modBus = modBus;
		parse(rawData);
	}

	/*
	 * Public methods
	 */

	public ProfileInfoEntry() {
		// TODO Auto-generated constructor stub
	}

	private void parse(byte[] rawData) throws IOException {
		TimeDateParser td_parser = new TimeDateParser(this.modBus.gettimeZone());

		this.entryID = ProtocolUtils.getShort(rawData, ENTRYID_OFFSET);
        loadCurveStatus = ProtocolUtils.getShort(rawData, CHANNELS_OFFSET);

        if (modBus instanceof Enerium50 || modBus instanceof Enerium150) {
            this.channels = (ProtocolUtils.getShort(rawData, CHANNELS_OFFSET) >> 4) & 0x000000FF;
        } else {    // instanceof Enerium200
            this.channels = (ProtocolUtils.getShort(rawData, CHANNELS_OFFSET) >> 8) & 0x000000FF;
        }

        this.numberOfchannels = 0;
		for (int i = 0; i < 8; i++) {
			if ((this.channels & (0x01 << i)) != 0x00) {
				this.numberOfchannels++;
			}
		}

		this.entryBytes = (getNumberOfchannels() * CHANNEL_SIZE) + STATUS_SIZE + TIMESTAMP_SIZE;

		this.startTime = td_parser.parseTime(ProtocolUtils.getSubArray2(rawData, STARTTIME_OFFSET, DATE_LENGTH));
		this.endTime = td_parser.parseTime(ProtocolUtils.getSubArray2(rawData, ENDTIME_OFFSET, DATE_LENGTH));
		this.interval = ProtocolUtils.getShort(rawData, INTERVAL_OFFSET);

		if (this.interval > 0) {
			this.startOnBoundary = ParseUtils.isOnIntervalBoundary(getStartCalendar(), getInterval());
			this.endOnBoundary = ParseUtils.isOnIntervalBoundary(getEndCalendar(), getInterval());

			Calendar tempStartCal = getStartCalendar();
			Calendar tempEndCal = getEndCalendar();
			ParseUtils.roundUp2nearestInterval(tempStartCal, getInterval());
			ParseUtils.roundUp2nearestInterval(tempEndCal, getInterval());

			this.entries = (int) ((tempEndCal.getTimeInMillis() - tempStartCal.getTimeInMillis()) / (getInterval() * 1000)) + 1;
			if (this.entries > 1) this.entries++;
		}

	}

	/*
	 * Public getters and setters
	 */

	public int getEntryID() {
		return entryID;
	}
	public int getChannels() {
		return channels;
	}
	public Date getStartTime() {
		return startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public int getInterval() {
		return interval;
	}
	public int getEntries() {
		return entries;
	}
	public boolean isStartOnBoundary() {
		return startOnBoundary;
	}
	public boolean isEndOnBoundary() {
		return endOnBoundary;
	}
	public Calendar getStartCalendar() {
		return Utils.getCalendarFromDate(getStartTime(), modBus);
	}
	public Calendar getEndCalendar() {
		return Utils.getCalendarFromDate(getEndTime(), modBus);
	}
	public int getNumberOfchannels() {
		return numberOfchannels;
	}
	public int getEntryBytes() {
		return entryBytes;
	}
	public boolean isChannelEnabled(int channelId) throws ProtocolException {
		if ((channelId >= MAX_CHANNELS) || (channelId < 0)) throw new ProtocolException("ProfileInfoEntry.isChannelEnabled(): channelId is out of size: " + channelId);
		channelId = 7 - channelId;
		return ((getChannels() & (1<<channelId)) != 0);
	}
	public Modbus getModBus() {
		return modBus;
	}

	public void setEntryID(int entryID) {
		this.entryID = entryID;
	}
	public void setChannels(int channels) {
		this.channels = channels;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}

    public short getLoadCurveStatus() {
        return loadCurveStatus;
    }

    public String toString() {
	    return "ProfileInfoEntry:" +
			    " entryID=" + ProtocolUtils.buildStringHex(getEntryID(), 4) +
			    " channels=" + ProtocolUtils.buildStringHex(getChannels(), 4) +
			    " nrChannels=" + getNumberOfchannels() +
			    " interval=" + ProtocolUtils.buildStringHex(getInterval(), 4) +
			    " startTime=" + getStartTime() +
			    " endTime=" + getEndTime() +
			    " startOnBound=" + isStartOnBoundary() +
			    " endOnBound=" + isEndOnBoundary() +
			    " entryBytes=" + getEntryBytes() +
			    " entries=" + getEntries();
    }

}
