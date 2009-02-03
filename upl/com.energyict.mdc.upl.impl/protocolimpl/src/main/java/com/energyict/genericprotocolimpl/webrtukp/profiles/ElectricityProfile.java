package com.energyict.genericprotocolimpl.webrtukp.profiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.client.ParseUtils;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.genericprotocolimpl.webrtukp.WebRTUKP;
import com.energyict.genericprotocolimpl.webrtukp.eventhandling.DisconnectControlLog;
import com.energyict.genericprotocolimpl.webrtukp.eventhandling.EventsLog;
import com.energyict.genericprotocolimpl.webrtukp.eventhandling.FraudDetectionLog;
import com.energyict.genericprotocolimpl.webrtukp.eventhandling.MbusLog;
import com.energyict.genericprotocolimpl.webrtukp.eventhandling.PowerFailureLog;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

public class ElectricityProfile {
	
	private boolean DEBUG = true;	// TODO set it back to false
	
	private WebRTUKP webrtu;
	
	public ElectricityProfile(){
	}
	
	public ElectricityProfile(WebRTUKP webrtu){
		this.webrtu = webrtu;
	}
	
	public void getProfile(ObisCode electricityProfile, boolean events) throws IOException, SQLException, BusinessException{
		ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;
		
		try {
			genericProfile = getCosemObjectFactory().getProfileGeneric(electricityProfile);
			List<ChannelInfo> channelInfos = getChannelInfos(genericProfile);
			verifyProfileInterval(genericProfile, channelInfos);
			
			profileData.setChannelInfos(channelInfos);
			Calendar fromCalendar = null;
			Calendar channelCalendar = null;
			Calendar toCalendar = getToCalendar();
			
			for (int i = 0; i < getMeter().getChannels().size(); i++) {
				Channel chn = getMeter().getChannel(i);
				
				//TODO this does not work with the 7.5 version
				
				if(!(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS) && 
						!(chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS)){
					channelCalendar = getFromCalendar(getMeter().getChannel(i));
					if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
						fromCalendar = channelCalendar;
					}
				}
			}
			
			webrtu.getLogger().log(Level.INFO, "Retrieving profiledata from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
			DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
			buildProfileData(dc, profileData, genericProfile);
			profileData.sort();
			
			if(events){
				Date lastLogReading = webrtu.getMeter().getLastLogbook();
				if(lastLogReading == null){
					lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(webrtu.getMeter());
				}
				Calendar fromCal = ProtocolUtils.getCleanCalendar(getTimeZone());
				fromCal.setTime(lastLogReading);
				webrtu.getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + webrtu.getSerialNumber() + ".");
				DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
				DataContainer dcControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getControlLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
				DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
				DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
				DataContainer dcMbusEventLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusEventLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
//				DataContainer dcMbusEventLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusEventLogObject().getObisCode()).getBuffer();
				
				EventsLog standardEvents = new EventsLog(getTimeZone(), dcEvent); 
				FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(getTimeZone(), dcFraudDetection);
				DisconnectControlLog disconnectControl = new DisconnectControlLog(getTimeZone(), dcControlLog);
				MbusLog mbusLogs = new MbusLog(getTimeZone(), dcMbusEventLog);
				PowerFailureLog powerFailure = new PowerFailureLog(getTimeZone(), dcPowerFailure);
				
				profileData.getMeterEvents().addAll(standardEvents.getMeterEvents());
				profileData.getMeterEvents().addAll(fraudDetectionEvents.getMeterEvents());
				profileData.getMeterEvents().addAll(disconnectControl.getMeterEvents());
				profileData.getMeterEvents().addAll(mbusLogs.getMeterEvents());
				profileData.getMeterEvents().addAll(powerFailure.getMeterEvents());
				
				profileData.applyEvents(webrtu.getMeter().getIntervalInSeconds()/60);
			}
			
			// We save the profileData to a tempObject so we can store everything at the end of the communication
			webrtu.getStoreObject().add(getMeter(), profileData);
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	private void verifyProfileInterval(ProfileGeneric genericProfile, List<ChannelInfo> channelInfos) throws IOException{
		Iterator<ChannelInfo> it = channelInfos.iterator();
		while(it.hasNext()){
			ChannelInfo ci = it.next();
			if(getMeter().getChannel(ci.getChannelId()).getIntervalInSeconds() != genericProfile.getCapturePeriod()){
				throw new IOException("Interval mismatch, EIServer: " + getMeter().getIntervalInSeconds() + "s - Meter: " + genericProfile.getCapturePeriod() + "s.");
			}
		}
	}
	
	private List<ChannelInfo> getChannelInfos(ProfileGeneric profile) throws IOException {
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){
				
				if(isKampstrupElectricityObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode()) 
						&& !isProfileStatusObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode())){ // make a channel out of it
					CapturedObject co = ((CapturedObject)profile.getCaptureObjects().get(i));
					ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());
					if((su != null) && (su.getUnitCode() != 0)){
						ci = new ChannelInfo(index, getProfileChannelNumber(index+1), "WebRtuKP_"+index, su.getUnit());
					} else {
						ci = new ChannelInfo(index, getProfileChannelNumber(index+1), "WebRtuKP_"+index, Unit.get(BaseUnit.UNITLESS));
					}
					
					index++;
					if(ParseUtils.isObisCodeCumulative(co.getLogicalName().getObisCode())){
						//TODO need to check the wrapValue
						ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
					}
					channelInfos.add(ci);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to build the channelInfos." + e);
		}
		return channelInfos;
	}

	/**
	 * Read the given object and return the scalerUnit.
	 * If the unit is 0(not a valid value) then return a unitLess scalerUnit.
	 * If you can not read the scalerUnit, then return a unitLess scalerUnit.
	 * @param oc
	 * @return
	 * @throws IOException
	 */
	private ScalerUnit getMeterDemandRegisterScalerUnit(ObisCode oc) throws IOException{
		try {
			ScalerUnit su = getCosemObjectFactory().getCosemObject(oc).getScalerUnit();
			if( su.getUnitCode() == 0){
				su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
			}
			return su;
		} catch (IOException e) {
			e.printStackTrace();
			webrtu.getLogger().log(Level.INFO, "Could not get the scalerunit from object '" + oc + "'.");
		}
		return new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
	}
	
	private int getProfileChannelNumber(int index){
		int channelIndex = 0;
		for(int i = 0; i < getMeter().getChannels().size(); i++){
			
			//TODO does not work with the 7.5 version, only in the 8.X
			
		if(!(getMeter().getChannel(i).getInterval().getTimeUnitCode() == TimeDuration.DAYS) && 
				!(getMeter().getChannel(i).getInterval().getTimeUnitCode() == TimeDuration.MONTHS)){
			channelIndex++;
			if(channelIndex == index){
				return getMeter().getChannel(i).getLoadProfileIndex() -1;
			}
		}
	}
		return -1;
	}
	
	private void buildProfileData(DataContainer dc, ProfileData pd, ProfileGeneric pg) throws IOException{
		
		
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
					cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
				} else {
					if(cal != null){
						cal.add(Calendar.SECOND, webrtu.getMeter().getIntervalInSeconds());
					}
				}
				if(cal != null){		
					
					if(getProfileStatusChannelIndex(pg) != -1){
						profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
					} else {
						profileStatus = 0;
					}
					
					currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg);
					if(currentInterval != null){
						pd.addInterval(currentInterval);
					}
				}
			}
		} else {
			webrtu.getLogger().info("No entries in LoadProfile");
		}
	}
	
	private boolean isProfileStatusObisCode(ObisCode oc) throws IOException{
		return oc.equals(getMeterConfig().getStatusObject().getObisCode());
	}
	
	private IntervalData getIntervalData(DataStructure ds, Calendar cal, int status, ProfileGeneric pg)throws IOException{
		
		IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(isKampstrupElectricityObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())
						&& !isProfileStatusObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())){
					id.addValue(new Integer(ds.getInteger(i)));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
		}
		
		return id;
	}
	
	private boolean isKampstrupElectricityObisCode(ObisCode obisCode){
		if ((obisCode.getA() == 1) && (((obisCode.getB() >= 0) && (obisCode.getB() <= 64)) || (obisCode.getB() == 128)) ) return true;
        else return false;
	}
	
	private int getProfileStatusChannelIndex(ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++){
				if(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(getMeterConfig().getStatusObject().getObisCode())){
					return i;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's status attribute.");
		}
		return -1;
	}
	
	private int getProfileClockChannelIndex(ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(getMeterConfig().getClockObject().getObisCode())){
					return i;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
		}
		return -1;
	}

	private CosemObjectFactory getCosemObjectFactory(){
		return this.webrtu.getCosemObjectFactory();
	}
	
	private Rtu getMeter(){
		return this.webrtu.getMeter();
	}
	
	private Calendar getToCalendar(){
		return this.webrtu.getToCalendar();
	}
	
	private Calendar getFromCalendar(Channel channel){
		return this.webrtu.getFromCalendar(channel);
	}
	
	private DLMSMeterConfig getMeterConfig(){
		return this.webrtu.getMeterConfig();
	}
	
	private TimeZone getTimeZone() throws IOException{
		return this.webrtu.getTimeZone();
	}
}
