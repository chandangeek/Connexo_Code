package com.energyict.protocolimpl.dlms.elster.ek2xx;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.dlms.CapturedObjects;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

class EK2xxProfile {

	private static final int DEBUG = 0;

	private EK2xx ek2xx = null;
	private List<ChannelInfo> channelInfos = null;
	private List<IntervalData> intervalDatas = new ArrayList<>(0);
	private EK2xxLogbook logbook = new EK2xxLogbook();
	private CapturedObjects capturedObjects = null;
	private boolean generateEvents = false;

	EK2xxProfile(EK2xx ek2xx) {
		this.channelInfos = null;
		this.ek2xx = ek2xx;
	}

	private void initProfile() {
		this.logbook.clearLogbook();
		this.intervalDatas = new ArrayList<>(0);
		this.channelInfos = null;
	}

	private void generateChannelInfos() throws IOException {
		int numberOfChannels = getEk2xx().getNumberOfChannels();
		for (int channelId=0;channelId<numberOfChannels;channelId++) {
			Unit unit = getEk2xx().readRegister(getCapturedObjects().getProfileDataChannel(channelId)).getQuantity().getUnit();
			getChannelInfos().add(new ChannelInfo(channelId, "CH"+channelId, unit));
		}
	}

	private EK2xx getEk2xx() {
		return this.ek2xx;
	}

	private void parseStructure(DataStructure ds) throws IOException {
		List numbers = new ArrayList(0);
		if (this.channelInfos == null) {
			generateChannelInfos();
		}

		for (int i = 2; i < ds.getNrOfElements(); i++) {
			if (getCapturedObjects().isChannelData(i-2)) {
				numbers.add(ds.getElement(i));
			}
		}
		Date timeStamp = ds.getOctetString(0).toDate(getTimeZone());
		Calendar calendar = ProtocolUtils.getCleanCalendar(getTimeZone());
		calendar.setTime(timeStamp);

		int meterIntStatus = ds.getInteger(1);
		int eiIntStatus = EK2xxEvents.getEiIntervalStatus(meterIntStatus);

		if (DEBUG >= 1) {
			System.out.println(
					" timeStamp = " + timeStamp +
					" meterIntStatus = " + ProtocolUtils.buildStringHex(meterIntStatus, 8) +
					" eiIntStatus = " + ProtocolUtils.buildStringHex(eiIntStatus, 8)
			);
		}

		if (!ParseUtils.isOnIntervalBoundary(calendar,getEk2xx().getProfileInterval())) {
			if (this.generateEvents) {
				this.logbook.addMeterEvent(timeStamp, MeterEvent.OTHER, meterIntStatus, EK2xxEvents.getStatusDescription(meterIntStatus));
			}
			ParseUtils.roundUp2nearestInterval(calendar,getEk2xx().getProfileInterval());
		}

		IntervalData intervalDataTemp = findIntervalData(timeStamp);
		if (intervalDataTemp != null) {
			eiIntStatus |= intervalDataTemp.getEiStatus();
			deleteIntervalData(timeStamp);
		}

		IntervalData intervalData = new IntervalData(calendar.getTime(), eiIntStatus, meterIntStatus);
		intervalData.addValues(numbers);

		getIntervalDatas().add(intervalData);

	}

	private IntervalData findIntervalData(Date timestamp) {
		for (int i = 0; i < getIntervalDatas().size(); i++) {
			IntervalData id = getIntervalDatas().get(i);
			if (id.getEndTime().compareTo(timestamp) == 0) {
				return id;
			}
		}
		return null;
	}

	private void deleteIntervalData(Date timestamp) {
		for (int i = 0; i < getIntervalDatas().size(); i++) {
			IntervalData id = getIntervalDatas().get(i);
			if (id.getEndTime().compareTo(timestamp) == 0) {
				getIntervalDatas().remove(i);
			}
		}
	}

	/*
	 * Public methods
	 */

	private TimeZone getTimeZone() {
		return getEk2xx().getTimeZone();
	}

	private void parseDataContainer(DataContainer dc) throws IOException {
		DataStructure root = dc.getRoot();
		int rootSize = root.getNrOfElements();
		for (int index = 0; index < rootSize; index++) {
			DataStructure ds = root.getStructure(index);
			parseStructure(ds);
		}
	}

	public CapturedObjects getCapturedObjects() throws IOException {
		if (this.capturedObjects == null) {
			int i;
			DataContainer dataContainer;
			try {
				ProfileGeneric profileGeneric = getEk2xx().getCosemObjectFactory().getProfileGeneric(EK2xxRegisters.PROFILE);
				getEk2xx().getMeterConfig().setCapturedObjectList(profileGeneric.getCaptureObjectsAsUniversalObjects());
				dataContainer = profileGeneric.getCaptureObjectsAsDataContainer();

				this.capturedObjects = new CapturedObjects(dataContainer.getRoot().element.length - 1);
				for (i=0;i<dataContainer.getRoot().element.length - 2;i++) {
					this.capturedObjects.add(i,
							dataContainer.getRoot().getStructure(i+2).getInteger(0),
							dataContainer.getRoot().getStructure(i+2).getOctetString(1).getArray(),
							dataContainer.getRoot().getStructure(i+2).getInteger(2));
				}
			}
			catch (java.lang.ClassCastException e) {
				System.out.println("Error retrieving object: "+e.getMessage());
			}
			catch(java.lang.ArrayIndexOutOfBoundsException e) {
				System.out.println("Index error: "+e.getMessage());
			}

		}

		return this.capturedObjects ;

	}

	Date getDateFromDataContainer(DataContainer dc) {
		Date returnDate = null;
		DataStructure root = dc.getRoot();

		if(root.getNrOfElements() > 0) {
			DataStructure ds = root.getStructure(0);
			returnDate = ds.getOctetString(0).toDate(getTimeZone());
		}

		return returnDate;
	}

	public List<ChannelInfo> getChannelInfos() {
		if (this.channelInfos == null) {
			this.channelInfos = new ArrayList<>(0);
		}
		return this.channelInfos;
	}

	public List<IntervalData> getIntervalDatas() {
		return this.intervalDatas;
	}

	public List<MeterEvent> getMeterEvents() {
		return checkOnOverlappingEvents(this.logbook.getMeterEvents());
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
		Iterator<MeterEvent> it = eventsMap.values().iterator();
		List<MeterEvent> result = new ArrayList<>();
		while (it.hasNext()) {
			result.add(it.next());
		}
		return result;
	}

	public void setGenerateEvents(boolean generateEvents) {
		this.generateEvents = generateEvents;
	}

	void parseDataContainers(List dataContainers) throws IOException {
		initProfile();
		for (int i = 0; i < dataContainers.size(); i++) {
			parseDataContainer((DataContainer) dataContainers.get(i));
		}
	}

}
