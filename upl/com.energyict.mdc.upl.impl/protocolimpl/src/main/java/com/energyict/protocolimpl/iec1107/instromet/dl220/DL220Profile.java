/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.DLObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.GenericArchiveObject;

/**
 * Implementation of the general functionality of a LoadProfile 
 * 
 * @author gna
 * @since 4-mrt-2010
 *
 */
public class DL220Profile {
	
	private final static String[] archiveEntryEvents = new String[]{"0x8105", "0x8106"};
	
	/** The index of the measurement */
	private final int index;
	/** The used {@link ProtocolLink}*/
	private final ProtocolLink link;
	/** The used {@link Archives} */
	private final Archives archive;

	/** The used {@link GenericArchiveObject} */
	private GenericArchiveObject archiveObject;

	/** The used {@link DL200MeterEventList} */
	private DL220MeterEventList meterEventList;
	
	/** The {@link DL220IntervalRecordConfig} from the meter*/
	private DL220RecordConfig dirc;
	
	private int numberOfChannels = -1;
	private int interval = -1;
	private int profileRequestBlockSize;

	private String capturedObjects = "";
	
	/**
	 * Default constructor
	 * 
	 * @param link
	 * 			- the use {@link ProtocolLink}
	 * 
	 * @param meterIndex
	 * 			- indicates which input is used
	 * 
	 * @param archive
	 * 			- indicates which {@link Archives} is used
	 * 
	 * @param profileRequestBlockSize
	 * 			- the size of the profileRequestBlocks 
	 */
	public DL220Profile(ProtocolLink link, int meterIndex, Archives archive, int profileRequestBlockSize){
		this.index = meterIndex;
		this.link = link;
		this.archive = archive;
		this.profileRequestBlockSize = profileRequestBlockSize;
	}

	/**
	 * @return the Number of channels
	 * @throws IOException 
	 */
	public int getNumberOfChannels() throws IOException {
		// TODO Auto-generated method stub
		
		if(numberOfChannels == -1){
			numberOfChannels = 1;
		}
		
		return numberOfChannels;
	}

	/**
	 * @return the interval of the Profile
	 * 
	 * @throws IOException when something happens during the read 
	 */
	public int getInterval() throws IOException {
		if(this.interval == -1){
			DLObject measurementPeriod = DLObject.constructObject(link, DLObject.SA_PROFILEMEASUREMENT_PERIOD);
			String[] quantity = (measurementPeriod.getValue((index==0)?5:6)).split("[*]");
			this.interval = DL220Utils.convertQuantityToSeconds(quantity);
		}
		return this.interval;
	}
	
	/**
	 * Setter for the interval
	 * 
	 * @param interval
	 * 			- the interval to set
	 */
	protected void setInterval(int interval){
		this.interval = interval;
	}

	/**
	 * Construct the channelInfos 
	 * 
	 * @return a list of {@link ChannelInfo}s
	 * 
	 * @throws IOException if an error occurred during the read of the {@link ChannelInfo}s 
	 */
	public List<ChannelInfo> buildChannelInfos() throws IOException{
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = new ChannelInfo(0, "Consumption", getValueUnit());
		ci.setCumulative();
		channelInfos.add(ci);
		return channelInfos;
	}
	
	/**
	 * Initialized getter for the {@link DL220IntervalRecordConfig}
	 * 
	 * @return the meters configuration
	 * 
	 * @throws IOException if something happened during the read
	 */
	public DL220RecordConfig getIntervalRecordConfig() throws IOException{
		if(this.dirc == null){
			this.dirc = new DL220IntervalRecordConfig(getCapturedObjects());
		}
		return this.dirc;
	}
	
	/**
	 * Setter for the {@link DL220IntervalRecordConfig}
	 * 
	 * @param dirc
	 * 			- the {@link DL220IntervalRecordConfig} to set
	 */
	protected void setDirc(DL220RecordConfig dirc){
		this.dirc = dirc;
	}
	
	/**
	 * @return the capturedObject String
	 * 
	 * @throws IOException if we could not read the objects from the meter
	 */
	private String getCapturedObjects() throws IOException{
		if(this.capturedObjects.equalsIgnoreCase("")){
			this.capturedObjects = getArchive().getCapturedObjects();
		}
		return this.capturedObjects;
	}
	
	/**
	 * Setter for the capturedObjects
	 * 
	 * @param capturedObjects
	 * 				- the capturedObjects to set
	 */
	protected void setCapturedObjects(String capturedObjects) {
		this.capturedObjects = capturedObjects;
	}
	
	/**
	 * Get the Unit list from the device and return the {@link Unit}
	 * 
	 * @return the {@link Unit} for the channel
	 * @throws IOException 
	 */
	private Unit getValueUnit() throws IOException{
		String units = getArchive().getUnits();
		String[] splittedUnits = units.split("[(]");
		String correctUnit = splittedUnits[4].substring(0, splittedUnits[4].indexOf(")"));
		int scaler = 0;
		if(correctUnit.equalsIgnoreCase("m3")){
			return Unit.get(BaseUnit.CUBICMETER);
		} else if (correctUnit.indexOf("Wh") > -1){
			scaler = (correctUnit.indexOf("k") > -1)?3:0;
			return Unit.get(BaseUnit.WATTHOUR, scaler);
		} else if (correctUnit.indexOf("W") > -1){
			scaler = (correctUnit.indexOf("k") > -1)?3:0;
			return Unit.get(BaseUnit.WATTHOUR, scaler);
		} else {
			return Unit.getUndefined();
		}
		
	}

	/**
	 * Get interval data within the request period
	 * 
	 * @param from
	 * 			- the initial date for the intervaldata
	 * 
	 * @param to
	 * 			- the end date for the intervaldata
	 * 
	 * @return the requested intervaldata
	 * 
	 * @throws IOException when reading of the data failed 
	 * 
	 * @throws NumberFormatException when the returned number of intervals ins't a number
	 */
	public List<IntervalData> getIntervalData(Date from, Date to) throws NumberFormatException, IOException {
		return buildIntervalData(getArchive().getIntervals(from, to, profileRequestBlockSize));
	}
	
	/**
	 * Build the list of IntervalData
	 * 
	 * @param rawData
	 * 			- the raw data returned from the device
	 * @return
	 * 			a list of {@link IntervalData}
	 * 
	 * @throws IOException if an exception occurred during on of the read requests
	 */
	protected List<IntervalData> buildIntervalData(String rawData) throws IOException{
		List<IntervalData> intervalList = new ArrayList<IntervalData>();
		
		int offset = 0;
		DL220IntervalRecord dir;
		String recordX;
		IntervalData id;
		String capturedObjects = getCapturedObjects();
		int numberOfCapturedObjects = DL220Utils.getNumberOfObjects(capturedObjects);
		
		do{
			recordX = DL220Utils.getNextRecord(rawData, offset, numberOfCapturedObjects);
			offset = rawData.indexOf(recordX) + recordX.length();
			dir = new DL220IntervalRecord(recordX, getIntervalRecordConfig(), link.getTimeZone());
			id = new IntervalData(dir.getEndTime());
			id.addValue(Integer.parseInt(dir.getValue()));
			String status = dir.getStatus();
			id.addEiStatus(DL220IntervalStateBits.intervalStateBits(status));
			if(!archiveEntryEvents[this.index].equalsIgnoreCase(dir.getEvent())){	// if it is more then a normal entry
				getMeterEventList().addRawEvent(dir);
			}
			intervalList.add(id);
		}while(offset < rawData.length());
		
		return sortOutIntervalList(intervalList);
	}
	
	/**
	 * Remove intervals that are not on the interval boundary. <br>
	 * We check if the interval-endTime is a multiple of the interval in seconds, if not delete it.
	 * (They are all cumulative values so deletion is allowed)
	 * 
	 * @param intervalList
	 * 				- the list to shift
	 * 
	 * @throws IOException can occur when the interval needs to be read
	 */
	protected List<IntervalData> sortOutIntervalList(List<IntervalData> intervalList) throws IOException{
		List<IntervalData> newList = new ArrayList<IntervalData>();
		for(IntervalData intervalData : intervalList){
			long endTime = intervalData.getEndTime().getTime();
			if(endTime%(getInterval()*1000) == 0){
				newList.add(intervalData);
			}
		}
		return newList;
	}

	/**
	 * Getter for the {@link DL200MeterEventList}
	 * 
	 * @return the MeterEventList
	 */
	public DL220MeterEventList getMeterEventList(){
		if(this.meterEventList == null) {
			this.meterEventList = new DL220MeterEventList();
		}
		return this.meterEventList;
	}
	
	/**
	 * Getter for the {@link GenericArchiveObject}
	 * 
	 * @return the genericArchiveObject
	 */
	protected GenericArchiveObject getArchive(){
		if(this.archiveObject == null){
			this.archiveObject = new GenericArchiveObject(link, archive);
		}
		return this.archiveObject;
	}

	/**
	 * Get a list of meterEvents starting from the given fromDate
	 * 
	 * @param from
	 * 			- the date to start reading from
	 * 
	 * @return
	 */
	public List<MeterEvent> getMeterEvents(Date from) {
		// TODO Auto-generated method stub
		
		try {
			GenericArchiveObject gaoEvents = new GenericArchiveObject(link, Archives.LOGBOOK);
			String capturedObjects = gaoEvents.getCapturedObjects();
			DL220EventRecordConfig derc = new DL220EventRecordConfig(capturedObjects);
			String rawEvents = gaoEvents.getIntervals(from, profileRequestBlockSize);
			int numberOfCapturedObjects = DL220Utils.getNumberOfObjects(capturedObjects);
			int offset = 0;
			DL220EventRecord der;
			String recordX;
			
			do{
				recordX = DL220Utils.getNextRecord(rawEvents, offset, numberOfCapturedObjects);
				offset = rawEvents.indexOf(recordX) + recordX.length();
				der = new DL220EventRecord(recordX, derc, link.getTimeZone());
				getMeterEventList().addRawEvent(der);
			}while(offset < rawEvents.length());			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getMeterEventList().getEventList();
	}
	
	
    /** 
     * Set the interval status based on the {@link MeterEvent}s form the meter.
     * 
     * @param list
     * 			- the list of meterEvents
     * 
     * @throws IOException 
     * @throws UnsupportedException 
     */    
    @SuppressWarnings("unchecked")
	public void applyEvents(ProfileData profileData) throws UnsupportedException, IOException {
        Iterator<MeterEvent> eventIterator = profileData.getEventIterator();
        while (eventIterator.hasNext()) {
            applyEvent(eventIterator.next(), profileData.getIntervalDatas());
        }
    }
    
    /** 
     * Updates the interval status based on the information of a single event.
     * 
     * @param event 
     * 			- the event to convert to intervalStatus
     * 
     * @param list
     * 			- the list of intervalDatas
     *  
     * @throws IOException 
     * @throws UnsupportedException 
     */    
    private void applyEvent(MeterEvent event, List<IntervalData> list) throws UnsupportedException, IOException {
        Iterator<IntervalData> intervalIterator = list.iterator();
        while (intervalIterator.hasNext()) {
             intervalIterator.next().apply(event, getInterval()/60);
        }
    }

}
