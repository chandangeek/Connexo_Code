package com.energyict.protocolimpl.dlms.as220.gmeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.EventLogs;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

public class GMeter {

	/** TODO to set correct*/
	private static final ObisCode GAS_PROFILE_OBISCODE = ObisCode.fromString("0.1.24.3.0.255");
	private static final int SEC_PER_MIN = 60;
	
	private final GasValveController	gasValveController;
	private final AS220 				as220;
	private final GasInstallController 	gasInstallController;

	/**
	 * Default Constructor
	 * @param as220
	 */
	public GMeter(AS220 as220) {
		this.gasValveController = new GasValveController(as220);
		this.gasInstallController = new GasInstallController(as220);
		this.as220 = as220;
	}

	/**
	 * Getter for the {@link GasValveController}
	 * @return the gasValveController
	 */
	public GasValveController getGasValveController() {
		return gasValveController;
	}
	
	public AS220 getAs220() {
		return as220;
	}

	/**
     * Read the profile data from the MbusDevice
     *
     * @param from
     * @param to
     * @param includeEvents
     * @return the {@link ProfileData}
	 * @throws IOException 
	 * @throws UnsupportedException 
     * @throws IOException
	 */
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws UnsupportedException, IOException {
		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getAs220().getTimeZone());
		Calendar toCalendar = ProtocolUtils.getCleanCalendar(getAs220().getTimeZone());
		fromCalendar.setTime(from);
		toCalendar.setTime(to);
		ProfileData profileData = new ProfileData();
		ProfileGeneric pg = getAs220().getCosemObjectFactory().getProfileGeneric(GAS_PROFILE_OBISCODE);
		CapturedObjectsHelper coh = pg.getCaptureObjectsHelper();
		GProfileBuilder profileBuilder = new GProfileBuilder((GasDevice) getAs220(), coh);
		ScalerUnit[] scalerunit = profileBuilder.buildScalerUnits();
//		ScalerUnit[] scalerunit = profileBuilder.buildScalerUnits((byte)1);
		
		List<ChannelInfo> channelInfos = profileBuilder.buildChannelInfos(scalerunit);
		profileData.setChannelInfos(channelInfos);

		
		DataContainer dc = pg.getBuffer(fromCalendar, toCalendar);
//
//        // decode the compact array here and convert to a universallist...
//		LoadProfileCompactArray loadProfileCompactArray = new LoadProfileCompactArray();
//		loadProfileCompactArray.parse(pg.getBuffer(fromCalendar, toCalendar));
////		loadProfileCompactArray.parse(pg.getBufferData());
//		List<LoadProfileCompactArrayEntry> loadProfileCompactArrayEntries = loadProfileCompactArray.getLoadProfileCompactArrayEntries();
//
//        List<IntervalData> intervalDatas = profileBuilder.buildIntervalData(scalerunit,loadProfileCompactArrayEntries);
		
		
        profileData.setIntervalDatas(buildProfileData(dc));

        if (includeEvents) {
			EventLogs eventLogs = new EventLogs(getAs220());
			List<MeterEvent> meterEvents = eventLogs.getEventLog(fromCalendar, toCalendar);
			profileData.setMeterEvents(meterEvents);
			profileData.applyEvents(getAs220().getProfileInterval() / SEC_PER_MIN);
        }

        profileData.sort();
        return profileData;
	}
	
	private List<IntervalData> buildProfileData(final DataContainer dc) throws IOException{
		
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){
		
			for(int i = 0; i < dc.getRoot().getElements().length; i++){
				
				//Test
				if(dc.getRoot().getStructure(i) == null){
					dc.printDataContainer();
					System.out.println("Element: " + i);
				}
				
				if(dc.getRoot().getStructure(i).isOctetString(0)){
//					cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
					cal = new AXDRDateTime(new OctetString(dc.getRoot().getStructure(i).getOctetString(0).getArray())).getValue();
				} else {
					if(cal != null){
						cal.add(Calendar.SECOND, 900);	// TODO
					}
				}
				if(cal != null){		
					
//					if(getProfileStatusChannelIndex(pg) != -1){
						profileStatus = dc.getRoot().getStructure(i).getInteger(1);
//					} else {
//						profileStatus = 0;
//					}
					IntervalData id = new IntervalData(cal.getTime(), profileStatus);
					id.addValue(dc.getRoot().getStructure(i).getInteger(2));
					
					intervalDatas.add(id);
//					currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
//					if(currentInterval != null){
//						pd.addInterval(currentInterval);
//					}
				}
			}
		} else {
//			log("No entries in LoadProfile");
		}
		return intervalDatas;
	}
	
	/**
	 * Getter for the {@link GasInstallController}
	 * @return the gasInstallController
	 */
	public GasInstallController getGasInstallController() {
		return gasInstallController;
	}

}
