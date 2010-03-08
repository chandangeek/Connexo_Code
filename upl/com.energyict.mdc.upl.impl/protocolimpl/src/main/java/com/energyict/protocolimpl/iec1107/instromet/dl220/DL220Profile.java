/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
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
	
	/** The index of the measurement */
	private final int index;
	/** The used {@link ProtocolLink}*/
	private final ProtocolLink link;
	/** The used {@link Archives} */
	private final Archives archive;

	/** The used {@link GenericArchiveObject} */
	private GenericArchiveObject archiveObject;
	
	/** The {@link DL220IntervalRecordConfig} from the meter*/
	private DL220IntervalRecordConfig dirc;
	
	private int numberOfChannels = -1;
	private int profileRequestBlockSize;
	private int numberOfAvailableIntervals = 0;
	
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
		DLObject measurementPeriod = DLObject.constructObject(link, DLObject.SA_PROFILEMEASUREMENT_PERIOD);
		String[] quantity = (measurementPeriod.getValue((index==0)?5:6)).split("[*]");
		return DL220Utils.convertQuantityToSeconds(quantity);
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
	public DL220IntervalRecordConfig getIntervalRecordConfig() throws IOException{
		if(this.dirc == null){
			this.dirc = new DL220IntervalRecordConfig(getArchive().getCapturedObjects());
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
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public List getIntervalData(Date from, Date to) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		this.numberOfAvailableIntervals = Integer.parseInt(getArchive().getNumberOfIntervals(from));
//		int numbOfRequests = numbOfInts/profileRequestBlockSize + ((numbOfInts%profileRequestBlockSize)>0?1:0);
		StringBuilder intervalsString = new StringBuilder();
//		File file = new File("C:\\DL220Profile.bin");
//		FileOutputStream fos = new FileOutputStream(file);
//		fos.write(getArchive().getIntervals(from, profileRequestBlockSize).getBytes());
		System.out.println(intervalsString.append(getArchive().getIntervals(from, profileRequestBlockSize)));
//		fos.close();
		return null;
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
		
		do{
			recordX = DL220Utils.getNextRecord(rawData, offset);
			offset = rawData.indexOf(recordX) + recordX.length();
			dir = new DL220IntervalRecord(recordX, this.dirc);
			id = new IntervalData(dir.getEndTime());
			id.addValue(Integer.parseInt(dir.getValue()));
			//TODO add the status
			
			intervalList.add(id);
		}while(offset < rawData.length());
		
		return intervalList;
	}

	/**
	 * @param from
	 * @return
	 */
	public List getMeterEventList(Date from) {
		// TODO Auto-generated method stub
		return null;
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

}
