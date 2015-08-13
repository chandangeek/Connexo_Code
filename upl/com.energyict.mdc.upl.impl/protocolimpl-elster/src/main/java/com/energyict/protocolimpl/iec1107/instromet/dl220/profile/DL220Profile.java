/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.instromet.dl220.Archives;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220Utils;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.DLObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.GenericArchiveObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the general functionality of a LoadProfile.<br>
 * It is possible to configure <b>TWO</b> channels in the loadProfile. The meter can have a HighTarrif and a LowTarrif.
 * It is also possible to put the total and the adjustable value(which will most likely be the same value)
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

	/** The used {@link com.energyict.protocolimpl.iec1107.instromet.dl220.objects.GenericArchiveObject} */
	private GenericArchiveObject archiveObject;

	/** The used {@link DL200MeterEventList} */
	private DL220MeterEventList meterEventList;
	
	/** The {@link DL220IntervalRecordConfig} from the meter*/
	private DL220IntervalRecordConfig dirc;

	/** The List containing all {@link IntervalData}s */
	private List<IntervalData> intervalList = new ArrayList<IntervalData>();
	
	/** The used {@link Unit} (the unit is the same for each channel) */
	private Unit unit;
	
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
		return getIntervalRecordConfig().getNumberOfChannels();
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
	@SuppressWarnings("deprecation")
	public List<ChannelInfo> buildChannelInfos() throws IOException{
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		for(int i = 0; i < getNumberOfChannels(); i++){
			ChannelInfo ci = new ChannelInfo(i, "Channel " + i, getValueUnit());
			ci.setCumulative();
			/* We also use the @deprecated method for 8.3 versions */
			ci.setCumulativeWrapValue(new BigDecimal("1000000000"));
			channelInfos.add(ci);
		}
		return channelInfos;
	}
	
	/**
	 * Initialized getter for the {@link DL220IntervalRecordConfig}
	 * 
	 * @return the meters configuration
	 * 
	 * @throws IOException if something happened during the read
	 */
	public DL220IntervalRecordConfig getIntervalRecordConfig() throws IOException{
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
	protected void setDirc(DL220IntervalRecordConfig dirc){
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
	 * 
	 * @throws IOException when reading the unit failed
	 */
	public Unit getValueUnit() throws IOException{
		if(this.unit ==null){
			String units = getArchive().getUnits();
			String[] splittedUnits = units.split("[(]");
			String correctUnit = splittedUnits[4].substring(0, splittedUnits[4].indexOf(")"));
			this.unit = DL220Utils.getUnitFromString(correctUnit); 
		}
		return this.unit;
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
	 */
	public List<IntervalData> getIntervalData(Date from, Date to) throws IOException {
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
		List<IntervalData> iList = new ArrayList<IntervalData>();
		int offset = 0;
		DL220IntervalRecord dir;
		String recordX;
		IntervalData id;
		int numberOfCapturedObjects = getIntervalRecordConfig().getNumberOfObjectsPerRecord();
		
		do{
			recordX = DL220Utils.getNextRecord(rawData, offset, numberOfCapturedObjects);
			offset = rawData.indexOf(recordX) + recordX.length();
			dir = new DL220IntervalRecord(recordX, getIntervalRecordConfig(), link.getTimeZone());
			id = new IntervalData(dir.getEndTime());
			for(int i = 0 ; i < getNumberOfChannels(); i++){
				id.addValue(new BigDecimal(dir.getValue(i)));
			}
			String status = dir.getStatus();
			id.addEiStatus(DL220IntervalStateBits.intervalStateBits(status));
			if(!archiveEntryEvents[this.index].equalsIgnoreCase(dir.getEvent())){	// if it is more then a normal entry
				getMeterEventList().addRawEvent(dir);
			}
			iList.add(id);
		}while(offset < rawData.length());
		
		return sortOutIntervalList(iList);
	}
	
	/**
	 * Remove intervals that are not on the interval boundary. <br>
	 * We check if the interval-endTime is a multiple of the interval in seconds, if not delete it.
	 * (They are all cumulative values so deletion, or not adding to the new list, is allowed)
	 * 
	 * @param intervalList
	 * 				- the list to shift
	 * 
	 * @throws IOException can occur when the interval needs to be read
	 */
	@SuppressWarnings("unchecked")
	protected List<IntervalData> sortOutIntervalList(List<IntervalData> intervalList) throws IOException{
		for(IntervalData intervalData : intervalList){
			long endTime = intervalData.getEndTime().getTime();
			if(endTime%(getInterval()*1000) == 0){
				this.intervalList.add(intervalData);
			}
		}
		Collections.sort(this.intervalList);
		removeDubbles();
		return this.intervalList;
	}
	
	/**
	 * Removes duplicate intervals from the list.
	 */
	private void removeDubbles(){
		IntervalData previous = null;
		List<IntervalData> templist = new ArrayList<IntervalData>();
		for(IntervalData id : this.intervalList){
			if( (previous == null) || (previous.getEndTime().compareTo(id.getEndTime()) != 0)){
				templist.add(id);
			}
			previous = id;
		}
		this.intervalList.clear();
		this.intervalList.addAll(templist);
	}
	
	/**
	 * Getter for the {@link #intervalList}
	 * @return the {@link #intervalList}
	 */
	public List<IntervalData> getIntervalList(){
		return this.intervalList;
	}

	/**
	 * Getter for the {@link DL200MeterEventList}
	 * 
	 * @return the MeterEventList
	 */
	public DL220MeterEventList getMeterEventList(){
		if(this.meterEventList == null) {
			this.meterEventList = new DL220MeterEventList(this.index);
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
	 * @throws IOException 
	 */
	public List<MeterEvent> getMeterEvents(Date from) throws IOException {
		
		GenericArchiveObject gaoEvents = new GenericArchiveObject(link, Archives.LOGBOOK);
		String capturedObjects = gaoEvents.getCapturedObjects();
		String rawEvents = gaoEvents.getIntervals(from, profileRequestBlockSize);
		buildMeterEventList(rawEvents, capturedObjects);
			
		return getMeterEventList().getEventList();
	}
	
	/**
	 * Build the list of {@link MeterEvent}s
	 * 
	 * @param rawEvents
	 * 			- the raw data from the meter
	 * 
	 * @param capturedObjects
	 * 			- the raw capturedObjects from the meter
	 * 
	 * @throws IOException if the captured objects aren't correct
	 */
	protected void buildMeterEventList(String rawEvents, String capturedObjects) throws IOException{
		DL220EventRecordConfig derc = new DL220EventRecordConfig(capturedObjects);
		int numberOfCapturedObjects = derc.getNumberOfObjectsPerRecord();
		int offset = 0;
		DL220EventRecord der;
		String recordX;
		
		do{
			recordX = DL220Utils.getNextRecord(rawEvents, offset, numberOfCapturedObjects);
			offset = rawEvents.indexOf(recordX) + recordX.length();
			der = new DL220EventRecord(recordX, derc, link.getTimeZone());
			getMeterEventList().addRawEvent(der);
		}while(offset < rawEvents.length());
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
