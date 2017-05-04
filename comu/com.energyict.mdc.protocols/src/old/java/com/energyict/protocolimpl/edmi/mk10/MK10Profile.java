/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MK10ProfileData.java
 *
 * Created on 4 april 2006, 14:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.edmi.mk10.eventsurvey.Event;
import com.energyict.protocolimpl.edmi.mk10.eventsurvey.EventSurvey;
import com.energyict.protocolimpl.edmi.mk10.loadsurvey.LoadSurvey;
import com.energyict.protocolimpl.edmi.mk10.loadsurvey.LoadSurveyData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 *
 * @author koen
 */
public class MK10Profile {

	private final int DEBUG=0;

	MK10 mk10;
	LoadSurvey loadSurvey=null;
	EventSurvey eventLog=null;

	/** Creates a new instance of MK10ProfileData */
	public MK10Profile(MK10 mk10) {
		this.mk10=mk10;
	}

	private LoadSurvey getLoadSurvey() throws IOException {
		if (loadSurvey==null) {
			loadSurvey = new LoadSurvey(mk10.getCommandFactory(), mk10.getLoadSurveyNumber());
		}
		return loadSurvey;
	}

	private EventSurvey getEventLog() throws IOException {
		if (eventLog==null) {
			eventLog = new EventSurvey(mk10.getCommandFactory());
		}
		return eventLog;
	}

	public int getProfileInterval() throws IOException {
		return getLoadSurvey().getProfileInterval();
	}

	public int getNumberOfChannels() throws IOException {
		return getLoadSurvey().getNrOfChannels() - 1;
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
		ProfileData profileData=new ProfileData();

		if (DEBUG>=1) {
			System.out.println("KV_DEBUG> From: "+from.toString());
			System.out.println("KV_DEBUG> includeEvents: "+includeEvents);
			System.out.println("");
			System.out.println("KV_DEBUG> "+getLoadSurvey());
		}
		LoadSurveyData loadSurveyData = getLoadSurvey().readFile(from);
		if (DEBUG>=1) {
			System.out.println("KV_DEBUG> "+loadSurveyData);
		}

		profileData.setChannelInfos(buildChannelInfos(loadSurveyData));
		profileData.setIntervalDatas(buildIntervalDatas(loadSurveyData));

		if (includeEvents) {
			if (DEBUG>=1) {
				System.out.println("KV_DEBUG> "+getEventLog());
			}
			LinkedHashSet eventLogData = getEventLog().readFile(from);
			if (DEBUG>=1) {
				System.out.println("KV_DEBUG> "+eventLogData);
			}
			profileData.setMeterEvents(buildMeterEvents(eventLogData));
			profileData.applyEvents(loadSurvey.getProfileInterval()/60);
		}

		return profileData;
	} // public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException

	private List buildChannelInfos(LoadSurveyData loadSurveyData) {
		List channelInfos = new ArrayList();
		for (int channel=0; channel<loadSurveyData.getLoadSurvey().getNrOfChannels() - 1; channel++) {
			ChannelInfo channelInfo = new ChannelInfo(channel,"EDMI MK10 channel "+channel,loadSurveyData.getLoadSurvey().getLoadSurveyChannels()[channel].getUnit());
			channelInfo.setMultiplier(loadSurveyData.getLoadSurvey().getLoadSurveyChannels()[channel].getScalingFactor());
			channelInfos.add(channelInfo);
		} // for (int channel=0; channel<loadSurveyData.getLoadSurvey().getNrOfChannels(); channel++)
		return channelInfos;
	} // private List buildChannelInfos(LoadSurveyData loadSurveyData)

	private List buildIntervalDatas(LoadSurveyData loadSurveyData) throws IOException {
		int infochannel = loadSurveyData.getLoadSurvey().getNrOfChannels() - 1;
		List intervalDatas = new ArrayList();
		Calendar cal = ProtocolUtils.getCleanCalendar(mk10.getTimeZone());
		cal.setTime(loadSurveyData.getFirstTimeStamp());
		for (int interval = 0; interval < loadSurveyData.getNumberOfRecords(); interval++) {
			IntervalData intervalData = new IntervalData(new Date(cal.getTime().getTime()));
			for (int channel = 0; channel < loadSurveyData.getLoadSurvey().getNrOfChannels() - 1; channel++) {
				int protocolStatus = loadSurveyData.getChannelValues(interval)[infochannel].getBigDecimal().intValue();
				int eiStatus = mapProtocolStatus2EiStatus(protocolStatus);
				intervalData.setEiStatus(eiStatus);
				intervalData.setProtocolStatus(protocolStatus);
				intervalData.addValue(loadSurveyData.getChannelValues(interval)[channel].getBigDecimal());
			}
			intervalDatas.add(intervalData);
			cal.add(Calendar.SECOND, loadSurveyData.getLoadSurvey().getProfileInterval());
		}
		return intervalDatas;
	}

	private List buildMeterEvents(LinkedHashSet eventLogData) throws IOException {
		Calendar cal = ProtocolUtils.getCleanCalendar(mk10.getTimeZone());
		List meterEvents = new ArrayList();
		MeterEvent me;
		Iterator it = eventLogData.iterator();
		while(it.hasNext()) {
			Event event = (Event) it.next();
			Date eventdate = event.getEventDate();
			while (duplicateDate(meterEvents, eventdate)) {
				cal.setTime(eventdate);
				cal.add(Calendar.SECOND, 1);
				eventdate = cal.getTime();
			}
			me = new MeterEvent(eventdate, event.getEiServerEventCode(), event.getProtocolEventCode(), event.getEventDescription());
			meterEvents.add(me);
		}

		return meterEvents;
	} // private List buildMeterEvents(LoadSurveyData eventLogData)

	private boolean duplicateDate(List melist, Date date) {
		Iterator it = melist.iterator();
		while(it.hasNext()) {
			MeterEvent me = (MeterEvent) it.next();
			if (me.getTime().equals(date)) {
				return true;
			}
		}
		return false;
	}

	private final int ABSENT_READING=0x0001;
	private final int INCOMPLETE_INTERVAL=0x0002;
	private final int POWER_FAILED_DURING_INTERVAL=0x0004;
	private final int EFA_1=0x0008;
	private final int EFA_2=0x0010;
	private final int EFA_3=0x0020;

	private int mapProtocolStatus2EiStatus(int protocolStatus) {
		int eiStatus=0;

		// ABSENT_READING is inverse.
		// Bit is 1 when normal reading
		// Bit is 0 when absent reading
		if ((protocolStatus & ABSENT_READING) != ABSENT_READING) {
			eiStatus |= IntervalStateBits.MISSING;
		}

		// jme : 18-12-2008 Removed MISSING flag when only incomplete interval

		//		if ((protocolStatus & INCOMPLETE_INTERVAL) == INCOMPLETE_INTERVAL) {
		//			eiStatus |= IntervalStateBits.MISSING;
		//		}

		if ((protocolStatus & POWER_FAILED_DURING_INTERVAL) == POWER_FAILED_DURING_INTERVAL) {
			eiStatus |= IntervalStateBits.POWERDOWN;
		}
		if ((protocolStatus & INCOMPLETE_INTERVAL) == INCOMPLETE_INTERVAL) {
			eiStatus |= IntervalStateBits.SHORTLONG;
		}
		if ((protocolStatus & EFA_1) == EFA_1) {
			eiStatus |= IntervalStateBits.OTHER;
		}
		if ((protocolStatus & EFA_2) == EFA_2) {
			eiStatus |= IntervalStateBits.OTHER;
		}
		if ((protocolStatus & EFA_3) == EFA_3) {
			eiStatus |= IntervalStateBits.OTHER;
		}

		return eiStatus;
	}
}
