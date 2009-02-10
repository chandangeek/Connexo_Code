package com.energyict.protocolimpl.dlms.elster.ek2xx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.dlms.CapturedObjects;

public class EK2xxProfile {

	private static final int DEBUG 			= 0;
	private static final int STATUS_OK		= 0x80000000;
	
	private EK2xx ek2xx						= null;
	private List channelInfos 				= null;
	private List intervalDatas				= new ArrayList(0);
	private EK2xxLogbook logbook			= new EK2xxLogbook();
	private CapturedObjects capturedObjects	= null;        
	private boolean generateEvents			= false;
	
	/*
	 * Constructors
	 */

	public EK2xxProfile(EK2xx ek2xx) {
		this.channelInfos = null;
		this.ek2xx = ek2xx;
	}
	
	/*
	 * Private getters, setters and methods
	 */

	private void initProfile() {
		this.logbook.clearLogbook();
		this.intervalDatas = new ArrayList(0);
		this.channelInfos = null;
	}

	private void generateChannelInfos(DataStructure ds) throws IOException {
		int numberOfChannels = getEk2xx().getNumberOfChannels();
		for (int channelId=0;channelId<numberOfChannels;channelId++) {
            Unit unit = getEk2xx().readRegister(getCapturedObjects().getProfileDataChannel(channelId)).getQuantity().getUnit();
            getChannelInfos().add(new ChannelInfo(channelId, "CH"+channelId, unit));
        }
	}

	private EK2xx getEk2xx() {
		return ek2xx;
	}
	
	private void parseStructure(DataStructure ds) throws IOException {
		List numbers = new ArrayList(0); 
		if (channelInfos == null) generateChannelInfos(ds);
		
		for (int i = 2; i < ds.getNrOfElements(); i++) {
			if (getCapturedObjects().isChannelData(i-2))
				numbers.add(ds.getElement(i));
		}
		Date timeStamp = ds.getOctetString(0).toDate(getTimeZone());
		Calendar calendar = ProtocolUtils.getCleanCalendar(getTimeZone());
		calendar.setTime(timeStamp);
		
		int meterIntStatus = ds.getInteger(1);
		int eiIntStatus = EK2xxEvents.getEiIntervalStatus(meterIntStatus);
		
		System.out.println(
				" timeStamp = " + timeStamp +
				" meterIntStatus = " + ProtocolUtils.buildStringHex(meterIntStatus, 8) +
				" eiIntStatus = " + ProtocolUtils.buildStringHex(eiIntStatus, 8)
		);
		
        if (!ParseUtils.isOnIntervalBoundary(calendar,getEk2xx().getProfileInterval())) {
    		if (generateEvents) {
    			logbook.addMeterEvent(timeStamp, MeterEvent.OTHER, meterIntStatus, EK2xxEvents.getStatusDescription(meterIntStatus));
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
			IntervalData id = (IntervalData) getIntervalDatas().get(i);
			if (id.getEndTime().compareTo(timestamp) == 0) return id;
		}
		return null;
	}
	
	private void deleteIntervalData(Date timestamp) {
		for (int i = 0; i < getIntervalDatas().size(); i++) {
			IntervalData id = (IntervalData) getIntervalDatas().get(i);
			if (id.getEndTime().compareTo(timestamp) == 0) getIntervalDatas().remove(i);
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

	/*
	 * Public getters and setters
	 */

    public CapturedObjects getCapturedObjects()  throws UnsupportedException, IOException {
        
    	if (capturedObjects == null) {
           byte[] responseData;
           int i;
           DataContainer dataContainer = null;
           try {
               ProfileGeneric profileGeneric = getEk2xx().getCosemObjectFactory().getProfileGeneric(EK2xxRegisters.PROFILE);
               getEk2xx().getMeterConfig().setCapturedObjectList(profileGeneric.getCaptureObjectsAsUniversalObjects());               
               dataContainer = profileGeneric.getCaptureObjectsAsDataContainer();
               
               capturedObjects = new CapturedObjects(dataContainer.getRoot().element.length - 1);
               for (i=0;i<dataContainer.getRoot().element.length - 2;i++) {
                  capturedObjects.add(i,
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
        
        return capturedObjects ;
        
    }

	public Date getDateFromDataContainer(DataContainer dc) {
		Date returnDate = null;
		DataStructure root = dc.getRoot();
		
		if(root.getNrOfElements() > 0) {
			DataStructure ds = root.getStructure(0);
			returnDate = ds.getOctetString(0).toDate(getTimeZone());
		} 
		
		return returnDate;
	}
    
	public List getChannelInfos() {
		if (channelInfos == null) channelInfos = new ArrayList(0);
		return channelInfos;
	}

	public List getIntervalDatas() {
		return intervalDatas;
	}

	public List getMeterEvents() {
		return checkOnOverlappingEvents(logbook.getMeterEvents());
	}

    public static List checkOnOverlappingEvents(List meterEvents) {
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
	
	public void setGenerateEvents(boolean generateEvents) {
		this.generateEvents = generateEvents;
	}

	public void parseDataContainers(List dataContainers) throws IOException {
		initProfile();
		for (int i = 0; i < dataContainers.size(); i++) {
			parseDataContainer((DataContainer) dataContainers.get(i));
		}
	}
	
}
