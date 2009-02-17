package com.energyict.protocolimpl.modbus.enerdis.enerium200.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

public class Profile {

	private static final int DEBUG = 0;

	private ProfileInfo profileInfo = null;
	private List profileParts		= null;
	private Modbus modBus			= null;
	
	/*
	 * Constructors
	 */

	public Profile(Modbus modBus) throws IOException {
		this.modBus = modBus;
		this.profileInfo = new ProfileInfo(this.modBus);
		this.profileParts = profileInfo.generateProfileParts();
	}
	
	/*
	 * Public getters and setters
	 */

	public ProfileInfo getProfileInfo() {
		return profileInfo;
	}

	public List getChannelInfos() {
		List channelInfos = new ArrayList(0);

		channelInfos.add(new ChannelInfo(0, 0, "P+", Unit.get("kW")));
		channelInfos.add(new ChannelInfo(1, 1, "P-", Unit.get("kW")));
		channelInfos.add(new ChannelInfo(2, 2, "S+", Unit.get("kVA")));
		channelInfos.add(new ChannelInfo(3, 3, "S-", Unit.get("kVA")));
		channelInfos.add(new ChannelInfo(4, 4, "Q1", Unit.get("kvar")));
		channelInfos.add(new ChannelInfo(5, 5, "Q2", Unit.get("kvar")));
		channelInfos.add(new ChannelInfo(6, 6, "Q3", Unit.get("kvar")));
		channelInfos.add(new ChannelInfo(7, 7, "Q4", Unit.get("kvar")));
		
		return channelInfos;
	}

	public List getIntervalDatas(Date from, Date to) throws IOException {
		List intervalDatas = new ArrayList(0);
		for (int i = 0; i < profileParts.size(); i++) {
			ProfilePart pp = (ProfilePart) profileParts.get(i);
			if (pp.getProfileInfoEntry().getInterval() == getProfileInterval()){
				intervalDatas.addAll(pp.getIntervalDatas(from, to));
			}
		}
		
		return filterIntervals(intervalDatas);
	}
	
	public List createEvents(List intervalDatas) {
		List meterEvents = new ArrayList(0);
		ProfileData profileData = new ProfileData();

		for (int i = 0; i < intervalDatas.size(); i++) {
			IntervalData id = (IntervalData) intervalDatas.get(i);
			MeterEvent me = new MeterEvent(id.getEndTime(), MeterEvent.OTHER, id.getProtocolStatus());
			//meterEvents.add(me);
		}

		return checkOnOverlappingEvents(meterEvents);
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

	private List filterIntervals(List intervalDatas) throws IOException {
		List intervalDatasTemp = new ArrayList(0);

		for (int i = 0; i < intervalDatas.size(); i++) {
			IntervalData id = (IntervalData) intervalDatas.get(i);
			Calendar endCal = Utils.getCalendarFromDate(id.getEndTime(), this.modBus);
			ParseUtils.roundUp2nearestInterval(endCal, getProfileInterval());
			id.getEndTime().setTime(endCal.getTimeInMillis());
			intervalDatasTemp.add(id);

		}

		ProfileData profileData = new ProfileData();
		profileData.setIntervalDatas(intervalDatas);
		profileData.sort();
		intervalDatas = profileData.getIntervalDatas();
		
		//FIXME aanpassen (dubbele waarden uitfilteren en optellen)
		
		IntervalData previousId = null;
		
		for (int i = 0; i < intervalDatas.size(); i++) {
			IntervalData currentId = (IntervalData) intervalDatas.get(i);
			Date timeStamp = currentId.getEndTime();
			
			if (previousId != null) {
				if (previousId.getEndTime().getTime() == currentId.getEndTime().getTime()) {
					previousId = mergeIntervalDatas(previousId, currentId);
				} else {
					previousId = currentId;
					intervalDatasTemp.add(previousId );
				}
			} else {
				previousId = currentId;
			}
			
		}
		
		return intervalDatasTemp;
	}

	public int getProfileInterval() {
		return getProfileInfo().getProfileInterval();
	}

	private IntervalData mergeIntervalDatas(IntervalData id1, IntervalData id2) throws ProtocolException {

		if (id1.getValueCount() != id2.getValueCount()) 
			throw new ProtocolException(
					"Profile.mergeIntervalDatas(id1, id2): Two intervaldatas contains different number of values: " +
					id1.getValueCount()+"!="+id2.getValueCount()
			);

		if (id1.getEndTime().getTime() != id2.getEndTime().getTime()) 
			throw new ProtocolException(
					"Profile.mergeIntervalDatas(id1, id2): Intervaldatas endtime are different from each other: " +
					id1.getEndTime()+"!="+id2.getEndTime()
			);

		
		IntervalData idReturn = new IntervalData(id1.getEndTime());

		for (int i = 0; i < id1.getValueCount(); i++) {
			IntervalValue iv1 = (IntervalValue) id1.getIntervalValues().get(i);
			IntervalValue iv2 = (IntervalValue) id2.getIntervalValues().get(i);
			
			Number val1 = iv1.getNumber();
			Number val2 = iv2.getNumber();
			Number val = val1.intValue() + val2.intValue();

			int eis1 = iv1.getEiStatus();
			int eis2 = iv2.getEiStatus();
			int eis  = IntervalData.OK;
			
			if ((eis1 == IntervalData.MISSING) && (eis2 == IntervalData.MISSING)) eis = IntervalData.MISSING;  
			if ((eis1 == IntervalData.MISSING) ^ (eis2 == IntervalData.MISSING)) eis = IntervalData.SHORTLONG;  
			
			int ps1 = iv1.getProtocolStatus();
			int ps2 = iv2.getProtocolStatus();
			int ps = ps1 | ps2;
			
			IntervalValue iv = new IntervalValue(val, ps, eis);
			idReturn.addValue(val);
		}
		
		
		return idReturn;
	}
	
}
