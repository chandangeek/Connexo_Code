package com.energyict.protocolimpl.modbus.enerdis.enerium200.profile;

import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile {

	private static final int DEBUG = 0;

	private static final int SETTINGS_CHANGE 	= 0x001 | 0x010;
	private static final int TIME_SET 			= 0x002;
	private static final int POWER_FAIL 		= 0x004;

	private ProfileInfo profileInfo = null;
	private List profileParts = null;
	private List<MeterEvent> meterEvents = null;
	private Modbus modBus = null;

	public Profile(Modbus modBus) throws IOException {
		this.modBus = modBus;
		this.profileInfo = new ProfileInfo(this.modBus);
		this.profileParts = profileInfo.generateProfileParts();
	}

	public ProfileInfo getProfileInfo() {
		return profileInfo;
	}

	public List<ChannelInfo> getChannelInfos() {
		List<ChannelInfo> channelInfos = new ArrayList<>();
		channelInfos.add(new ChannelInfo(0, 0, "0.1.128.0.0.255", Unit.get("kW")));			//P+
		channelInfos.add(new ChannelInfo(1, 1, "0.2.128.0.0.255", Unit.get("kW")));			//P-
		channelInfos.add(new ChannelInfo(2, 2, "0.3.128.0.0.255", Unit.get("kVA")));		//S+
		channelInfos.add(new ChannelInfo(3, 3, "0.4.128.0.0.255", Unit.get("kVA")));		//S-
		channelInfos.add(new ChannelInfo(4, 4, "0.5.128.0.0.255", Unit.get("kvar")));		//Q1
		channelInfos.add(new ChannelInfo(5, 5, "0.6.128.0.0.255", Unit.get("kvar")));		//Q4
		channelInfos.add(new ChannelInfo(6, 6, "0.7.128.0.0.255", Unit.get("kvar")));		//Q2
		channelInfos.add(new ChannelInfo(7, 7, "0.8.128.0.0.255", Unit.get("kvar")));		//Q3
		return channelInfos;
	}

	public List<IntervalData> getIntervalDatas(Date from, Date to, boolean generateEvents) throws IOException {
		List<IntervalData> intervalDatas = new ArrayList<>();
		for (int i = 0; i < profileParts.size(); i++) {
			ProfilePart pp = (ProfilePart) profileParts.get(i);
			if (pp.getProfileInfoEntry().getInterval() == getProfileInterval()){
				intervalDatas.addAll(pp.getIntervalDatas(from, to));
			}
		}

		if (generateEvents) {
			this.meterEvents = createEvents(intervalDatas);
		}

		return filterIntervals(intervalDatas);
	}

	public List<MeterEvent> getMeterEvents() {
		return meterEvents;
	}

	private List<MeterEvent> createEvents(List<IntervalData> intervalDatas) {
		List<MeterEvent> meterEvents = new ArrayList<>();
		if (DEBUG >= 1) {
			for (int i = 0; i < intervalDatas.size(); i++) {
				IntervalData id = intervalDatas.get(i);
				System.out.print("Endtime = " + id.getEndTime() + " ");
				System.out.print("Eistatus = " + id.getEiStatus() + " ");
				System.out.print("ProtocolStatus = " + id.getProtocolStatus() + " ");
				System.out.println();
			}
		}

		for (int i = 0; i < intervalDatas.size(); i++) {
			IntervalData id = intervalDatas.get(i);
			int protocolStatus = (id.getProtocolStatus() >> 26) & 0x0F;
			MeterEvent me = null;

			if (DEBUG >= 1) {
				System.out.println("Events: " + id.getEndTime() + " status = " + ProtocolUtils.buildStringHex(protocolStatus, 8) + " original = " + ProtocolUtils.buildStringHex(id.getProtocolStatus(), 8));
			}

			if (protocolStatus != 0x00) {
				String eventMessage = "";
				int eisCode = MeterEvent.OTHER;
				boolean matchedStatus = false;

				if ((protocolStatus & SETTINGS_CHANGE) != 0) {
					eventMessage = "Change of settings. ";
					eisCode = MeterEvent.CONFIGURATIONCHANGE;
					id.setEiStatus(IntervalData.CONFIGURATIONCHANGE);
					me = new MeterEvent(id.getEndTime(), eisCode, protocolStatus, eventMessage);
					meterEvents.add(me);
					matchedStatus = true;
				}

				if ((protocolStatus & POWER_FAIL) != 0) {
					eventMessage = "Auxiliary power interruption. ";
					eisCode = MeterEvent.POWERDOWN;
					id.setEiStatus(IntervalData.POWERDOWN | IntervalData.POWERUP);
					me = new MeterEvent(id.getEndTime(), eisCode, protocolStatus, eventMessage);
					meterEvents.add(me);
					matchedStatus = true;
				}

				if ((protocolStatus & TIME_SET) != 0) {
					eventMessage = "Change of time. ";
					eisCode = MeterEvent.SETCLOCK;
					id.setEiStatus(IntervalData.SHORTLONG);
					me = new MeterEvent(id.getEndTime(), eisCode, protocolStatus, eventMessage);
					meterEvents.add(me);
					matchedStatus = true;
				}

				if (!matchedStatus) {
					eventMessage = "Unknown event: 0x" + ProtocolUtils.buildStringHex(id.getProtocolStatus(), 8);
					eisCode = MeterEvent.OTHER;
					id.setEiStatus(IntervalData.OTHER);
					me = new MeterEvent(id.getEndTime(), eisCode, protocolStatus, eventMessage);
					meterEvents.add(me);
				}
			}
		}
		return checkOnOverlappingEvents(meterEvents);
	}

    private static List<MeterEvent> checkOnOverlappingEvents(List<MeterEvent> meterEvents) {
    	Map<Date, MeterEvent> eventsMap = new HashMap<>();
        int size = meterEvents.size();
	    for (int i = 0; i < size; i++) {
	    	MeterEvent event = meterEvents.get(i);
	    	Date time = event.getTime();
	    	MeterEvent eventInMap = eventsMap.get(time);
	    	while (eventInMap != null) {
	    		time.setTime(time.getTime() + 1000); // add one second
				eventInMap = eventsMap.get(time);
	    	}
	    	MeterEvent newMeterEvent = new MeterEvent(time, event.getEiCode(), event.getProtocolCode(),event.getMessage());
    		eventsMap.put(time, newMeterEvent);
	    }
	    return new ArrayList<>(eventsMap.values());
    }

    private List<IntervalData> roundUpIntervalDatas(List<IntervalData> intervalDatas) throws IOException {
    	List<IntervalData> intervalDatasTemp = new ArrayList<>();
		for (int i = 0; i < intervalDatas.size(); i++) {
			IntervalData id = intervalDatas.get(i);
			Calendar endCal = Utils.getCalendarFromDate(id.getEndTime(), this.modBus);
			ParseUtils.roundUp2nearestInterval(endCal, getProfileInterval());
			id.getEndTime().setTime(endCal.getTimeInMillis());
			intervalDatasTemp.add(id);
		}
		return intervalDatasTemp;
    }

	private List<IntervalData> filterIntervals(List<IntervalData> intervalDatas) throws IOException {
		List<IntervalData> intervalDatasTemp = roundUpIntervalDatas(intervalDatas);
		intervalDatasTemp = Utils.sortIntervalDatas(intervalDatasTemp);

		//dubbele waarden uitfilteren en optellen
		intervalDatasTemp = mergeIntervalDatas(intervalDatasTemp);
		intervalDatasTemp = Utils.sortIntervalDatas(intervalDatasTemp);

		if (DEBUG >= 1) {
			for (int i = 0; i < intervalDatasTemp.size(); i++) {
				IntervalData id = intervalDatasTemp.get(i);
				System.out.print("Endtime = " + id.getEndTime() + " ");
				System.out.print("Eistatus = " + id.getEiStatus() + " ");
				System.out.print("ProtocolStatus = " + id.getProtocolStatus() + " ");
				System.out.println();
			}
		}

		return intervalDatasTemp;
	}

	public int getProfileInterval() {
		return getProfileInfo().getProfileInterval();
	}

	private List<IntervalData> mergeIntervalDatas(List<IntervalData> intervalDatas) throws ProtocolException {
		List<IntervalData> intervalDatasTemp = new ArrayList<>();
		IntervalData previousId = null;

		for (int i = 0; i < intervalDatas.size(); i++) {
			IntervalData currentId = intervalDatas.get(i);

			if (previousId != null) {
				if (previousId.getEndTime().getTime() == currentId.getEndTime().getTime()) {
					previousId = merge(previousId, currentId);
				} else {
					intervalDatasTemp.add(previousId );
					previousId = currentId;
				}
			} else {
				previousId = currentId;
			}
		}

		intervalDatasTemp.add(previousId);
		return intervalDatasTemp;
	}

	private IntervalData merge(IntervalData id1, IntervalData id2) throws ProtocolException {

		if (id1.getValueCount() != id2.getValueCount()) {
			throw new ProtocolException(
					"Profile.mergeIntervalDatas(id1, id2): Two intervaldatas contains different number of values: " +
							id1.getValueCount() + "!=" + id2.getValueCount()
			);
		}

		if (id1.getEndTime().getTime() != id2.getEndTime().getTime()) {
			throw new ProtocolException(
					"Profile.mergeIntervalDatas(id1, id2): Intervaldatas endtime are different from each other: " +
							id1.getEndTime() + "!=" + id2.getEndTime()
			);
		}


		List<IntervalValue> ivList = new ArrayList<>();
		IntervalData idReturn = new IntervalData(id1.getEndTime());
		int ps = id1.getProtocolStatus();

		for (int i = 0; i < id1.getValueCount(); i++) {
			IntervalValue iv1 = id1.getIntervalValues().get(i);
			IntervalValue iv2 = id2.getIntervalValues().get(i);

			Number val1 = iv1.getNumber();
			Number val2 = iv2.getNumber();
//			Number val = (Number)(val1.intValue() + val2.intValue());
			int val = val1.intValue() + val2.intValue();

			int eis1 = iv1.getEiStatus();
			int eis2 = iv2.getEiStatus();
			int eis  = IntervalData.OK;

			if ((eis1 == IntervalData.MISSING) && (eis2 == IntervalData.MISSING)) {
				eis |= IntervalData.MISSING;
			} else {
				eis |= IntervalData.SHORTLONG;
			}

			idReturn.setProtocolStatus(ps);

			IntervalValue iv = new IntervalValue(new Integer(val), ps, eis);
			ivList.add(iv);
		}

		idReturn.setIntervalValues(ivList);
		idReturn.setProtocolStatus(ps);

		return idReturn;
	}

}