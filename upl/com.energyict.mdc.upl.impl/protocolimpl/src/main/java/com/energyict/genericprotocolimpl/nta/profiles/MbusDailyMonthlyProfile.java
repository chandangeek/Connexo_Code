package com.energyict.genericprotocolimpl.nta.profiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

/**
 * 
 * @author gna
 */
public class MbusDailyMonthlyProfile extends AbstractNTAProfile{

	private AbstractMbusDevice mbusDevice;
	
	public MbusDailyMonthlyProfile(){
	}
	
	public MbusDailyMonthlyProfile(AbstractMbusDevice mbusDevice){
		this.mbusDevice = mbusDevice;
	}
	
	public void getMonthlyProfile(ObisCode mbusProfile) throws IOException{
		ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;
		
		genericProfile = getCosemObjectFactory().getProfileGeneric(mbusProfile);
		List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.MONTHS);
		
		if(channelInfos.size() != 0){
			
			profileData.setChannelInfos(channelInfos);
			Calendar fromCalendar = null;
			Calendar channelCalendar = null;
			Calendar toCalendar = getToCalendar();
			
			for (int i = 0; i < getMeter().getChannels().size(); i++) {
				// TODO check for the from-date of all the daily or monthly channels
				Channel chn = getMeter().getChannel(i);
				if(chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS){ //the channel is a daily channel
					channelCalendar = getFromCalendar(getMeter().getChannel(i));
					if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
						fromCalendar = channelCalendar;
					}
				}
			}
			
			this.mbusDevice.getLogger().log(Level.INFO, "Reading Monthly values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
			DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
			buildProfileData(dc, profileData, genericProfile, TimeDuration.MONTHS);
			ParseUtils.validateProfileData(profileData, toCalendar.getTime());
			ProfileData pd = sortOutProfiledate(profileData, TimeDuration.MONTHS);
			pd.sort();
			if(mbusDevice.getWebRTU().getMarkedAsBadTime()){
				pd.markIntervalsAsBadTime();
			}
			
			mbusDevice.getWebRTU().getStoreObject().add(pd, getMeter());
			
		}
		
	}
	
	private void buildProfileData(DataContainer dc, ProfileData pd, ProfileGeneric pg, int timeDuration) throws IOException{
		
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){
			for(int i = 0; i < dc.getRoot().getElements().length; i++){
				if(dc.getRoot().getStructure(i).isOctetString(0)){
					cal = new AXDRDateTime(new OctetString(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
				} else {
					if(cal != null){
						if(timeDuration == TimeDuration.DAYS){ //the daily profile is constructed from the hourly profile so only add 1 hour to the calendar
							cal.add(Calendar.DAY_OF_MONTH, 1);
						} else if(timeDuration == TimeDuration.MONTHS){
							cal.add(Calendar.MONTH, 1);
						} else {
							throw new ApplicationException("TimeDuration is not correct.");
						}
					}
				}
				if(cal != null){		
					
					if(getProfileStatusChannelIndex(pg) != -1){
						profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
					} else {
						profileStatus = 0;
					}
					
					currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
					if(currentInterval != null){
						pd.addInterval(currentInterval);
					}
				}
			}
		} else {
			this.mbusDevice.getLogger().info("No entries in LoadProfile");
		}
	}
	
	private int getProfileStatusChannelIndex(ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++){
				if(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(getMeterConfig().getMbusStatusObject(this.mbusDevice.getPhysicalAddress()).getObisCode())){
					return i;
				}
			}
		} catch (IOException e) {
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
			throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
		}
		return -1;
	}
	
	private IntervalData getIntervalData(DataStructure ds, Calendar cal, int status, ProfileGeneric pg, List channelInfos)throws IOException{
		
		IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		int index = 0;
		
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(index < channelInfos.size()){
					if(isMbusRegisterObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())){
						id.addValue(new Integer(ds.getInteger(i)));
						index++;
					}
				}
			}
		} catch (IOException e) {
			throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
		}
		
		return id;
	}
	
	private List<ChannelInfo> getDailyMonthlyChannelInfos(ProfileGeneric profile, int timeDuration) throws IOException {
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		int channelIndex = -1;
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){
				
				if(isMbusRegisterObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode())){ // make a channel out of it
					CapturedObject co = ((CapturedObject)profile.getCaptureObjects().get(i));
					ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());
					
					channelIndex = getDMChannelNumber(index+1, timeDuration);
					
					if(channelIndex != -1){
                        if((su != null) && (su.getUnitCode() != 0)){
                            ci = new ChannelInfo(index, channelIndex, "WebRtuKP_Mbus_DayMonth_"+index, su.getUnit());
                        } else {
                            throw new ProtocolException("Meter does not report a proper scalerUnit for all channels of his Mbus DailyMonthly LoadProfile, data can not be interpreted correctly.");
						}
						index++;
						//TODO need to check the wrapValue
						ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
						channelInfos.add(ci);
					}
					
				}
				
			}
		} catch (IOException e) {
			throw new IOException("Failed to build the channelInfos." + e);
		}
		return channelInfos;
	}
	
	private int getDMChannelNumber(int index, int duration){
		int channelIndex = 0;
		for(int i = 0; i < getMeter().getChannels().size(); i++){
			if(getMeter().getChannel(i).getInterval().getTimeUnitCode() == duration){
				channelIndex++;
				if(channelIndex == index){
					return getMeter().getChannel(i).getLoadProfileIndex() -1;
				}
			}
		}
		return -1;
	}
	
	private ProfileData sortOutProfiledate(ProfileData profileData, int timeDuration) {
		ProfileData pd = new ProfileData();
		pd.setChannelInfos(profileData.getChannelInfos());
		Iterator<IntervalData> it = profileData.getIntervalIterator();
		while(it.hasNext()){
			IntervalData id = it.next();
			switch(timeDuration){
			case TimeDuration.DAYS:{
				if(checkDailyBillingTime(id.getEndTime())) {
					pd.addInterval(id);
				}
			}break;
			case TimeDuration.MONTHS:{
				if(checkMonthlyBillingTime(id.getEndTime())) {
					pd.addInterval(id);
				}
			}break;
			}
		}
		return pd;
	}
	
	/**
	 * Checks if the given date is a date at midnight
	 * @param date
	 * @return true or false
	 */
	private boolean checkDailyBillingTime(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(cal.get(Calendar.HOUR_OF_DAY)==0 && cal.get(Calendar.MINUTE)==0 && cal.get(Calendar.SECOND)==0 && cal.get(Calendar.MILLISECOND)==0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the given data is a date at midnight on the first of the month
	 * @param date
	 * @return true or false
	 */
	private boolean checkMonthlyBillingTime(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(checkDailyBillingTime(date) && cal.get(Calendar.DAY_OF_MONTH)==1) {
			return true;
		}
		return false;
	}
	
//	/**
//	 * Read the given object and return the scalerUnit.
//	 * If the unit is 0(not a valid value) then return a unitLess scalerUnit.
//	 * If you can not read the scalerUnit, then return a unitLess scalerUnit.
//	 * @param oc
//	 * @return
//	 * @throws IOException
//	 */
//	private ScalerUnit getMeterDemandRegisterScalerUnit(ObisCode oc) throws IOException{
//		try {
//			ScalerUnit su = getCosemObjectFactory().getCosemObject(oc).getScalerUnit();
//			if(su != null){
//				if(su.getUnitCode() == 0){
//					su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
//				}
//
//			} else {
//				su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
//			}
//			return su;
//		} catch (IOException e) {
//			mbusDevice.getLogger().log(Level.INFO, "Could not get the scalerunit from object '" + oc + "'.");
//		}
//		return new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
//	}
				
	private boolean isMbusRegisterObisCode(ObisCode oc){
//		if((oc.getC() == 24) && (oc.getD() == 2) && (oc.getB() >=1) && (oc.getB() <= 4) && (oc.getE() >= 1) && (oc.getE() <= 4) ){
		if((oc.getC() == 24) && (oc.getD() == 2) && (oc.getB() == (mbusDevice.getPhysicalAddress()+1)) && (oc.getE() >= 1) && (oc.getE() <= 4) ){
			return true;
		} else {
			return false;
		}
	}
	
    /**
     * @return the used {@link java.util.logging.Logger}
     */
    @Override
    protected Logger getLogger() {
        return mbusDevice.getLogger();
    }

    protected CosemObjectFactory getCosemObjectFactory(){
		return this.mbusDevice.getWebRTU().getCosemObjectFactory();
	}
	
	protected Rtu getMeter(){
		return this.mbusDevice.getMbus();
	}
	
	private Calendar getToCalendar(){
		return this.mbusDevice.getWebRTU().getToCalendar();
	}
	
	private Calendar getFromCalendar(Channel channel){
		return this.mbusDevice.getWebRTU().getFromCalendar(channel);
	}
	
	private DLMSMeterConfig getMeterConfig(){
		return this.mbusDevice.getWebRTU().getMeterConfig();
	}
	
	private TimeZone getTimeZone(){
		return this.mbusDevice.getWebRTU().getTimeZone();
	}

	/**
	 * Get the dailyValues by reading the complete intervalProfile(hourly)
	 * @param dailyObisCode - the hourly-interval obisCode
	 * @throws IOException 
	 */
	public void getDailyProfile(ObisCode dailyObisCode) throws IOException {
//		getDailyProfile(null, obisCode);
		ProfileData profileData = new ProfileData();
		ProfileGeneric genericProfile;
		
		genericProfile = getCosemObjectFactory().getProfileGeneric(dailyObisCode);
		List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.DAYS);
		
		if(channelInfos.size() != 0){
			
			profileData.setChannelInfos(channelInfos);
			Calendar fromCalendar = null;
			Calendar channelCalendar = null;
			Calendar toCalendar = getToCalendar();
			
			for (int i = 0; i < getMeter().getChannels().size(); i++) {
				// TODO check for the from-date of all the daily or monthly channels
				Channel chn = getMeter().getChannel(i);
				if(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS){ //the channel is a daily channel
					channelCalendar = getFromCalendar(getMeter().getChannel(i));
					if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
						fromCalendar = channelCalendar;
					}
				}
			}
			
			this.mbusDevice.getLogger().log(Level.INFO, "Reading Daily values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
			DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
			buildProfileData(dc, profileData, genericProfile, TimeDuration.DAYS);
			ParseUtils.validateProfileData(profileData, toCalendar.getTime());
			ProfileData pd = sortOutProfiledate(profileData, TimeDuration.DAYS);
			pd.sort();
			if(mbusDevice.getWebRTU().getMarkedAsBadTime()){
				pd.markIntervalsAsBadTime();
			}
			
			mbusDevice.getWebRTU().getStoreObject().add(pd, getMeter());
		}
	}

	/**
	 * Get the dailyValues from the partial intervalProfile
	 * @param intervalProfileData - the partial profileData object form the hourly intervals
	 * @param obisCode - the hourly-interval obisCode
	 * @throws IOException 
	 * @deprecated We used to get the dailyProfile from the first Hourly value of the day ...
	 */
	public void getDailyProfile(ProfileData intervalProfileData, ObisCode obisCode) throws IOException {
		ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;		
		
		genericProfile = getCosemObjectFactory().getProfileGeneric(obisCode);
		List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.DAYS);
		
		if(channelInfos.size() != 0){
			profileData.setChannelInfos(channelInfos);
			Calendar fromCalendar = null;
			Calendar channelCalendar = null;
			Calendar toCalendar = getToCalendar();
			
			for (int i = 0; i < getMeter().getChannels().size(); i++) {
				Channel chn = getMeter().getChannel(i);
				if(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS){ //the channel is a daily channel
					channelCalendar = getFromCalendar(getMeter().getChannel(i));
					if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
						fromCalendar = channelCalendar;
					}
				}
			}
			
			this.mbusDevice.getLogger().log(Level.INFO, "Reading Daily values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
			DataContainer dc;
			ProfileData pd;
			if((intervalProfileData == null) || (intervalProfileData.getIntervalDatas().size() == 0) ||
					(intervalProfileData.getIntervalData(0).getIntervalValues().size() != profileData.getChannelInfos().size())){ // read the complete data yourself	
				dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile, TimeDuration.DAYS);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				pd = sortOutProfiledate(profileData, TimeDuration.DAYS);
			} else { // use the given intervalProfileData to get the data
				intervalProfileData.sort();
				if(intervalProfileData.getIntervalData(0).getEndTime().after(fromCalendar.getTime())){ // Read profileData using the from to the start of the intervalprofileData
					//TODO TOTEST
					toCalendar.setTime(intervalProfileData.getIntervalData(0).getEndTime());
					dc = genericProfile.getBuffer(fromCalendar, toCalendar);
					buildProfileData(dc, profileData, genericProfile, TimeDuration.DAYS);
					ParseUtils.validateProfileData(profileData, toCalendar.getTime());
					pd = combineProfileBuffers(sortOutProfiledate(profileData, TimeDuration.DAYS), sortOutProfiledate(intervalProfileData, TimeDuration.DAYS));
				} else {
					//TODO TOTEST
					pd = getProfileDataFrom(fromCalendar, sortOutProfiledate(intervalProfileData, TimeDuration.DAYS));
				}
			}
			
			if(mbusDevice.getWebRTU().getMarkedAsBadTime()){
				pd.markIntervalsAsBadTime();
			}
			
			mbusDevice.getWebRTU().getStoreObject().add(pd, getMeter());
		}
	}

	/**
	 * Cut a part from the given profileData
	 * @param fromCalendar - contains the time from where to start 
	 * @param intervalProfileData - the profileData from which we need a part
	 * @return
	 */
	private ProfileData getProfileDataFrom(Calendar fromCalendar, ProfileData intervalProfileData) {
		ProfileData pd = new ProfileData();
		pd.setChannelInfos(intervalProfileData.getChannelInfos());
		Iterator<IntervalData> it = intervalProfileData.getIntervalIterator();
		IntervalData id;
		while(it.hasNext()){
			id = it.next();
			if(!id.getEndTime().before(fromCalendar.getTime())){
				pd.addInterval(id);
			}
		}
		return pd;
	}

	/**
	 * Combine two profileData buffers together
	 * @param profileData the read profileBuffer
	 * @param intervalProfileData the hourly intervalProfile buffer
	 * @return a profileDatabuffer containing both buffers
	 */
	private ProfileData combineProfileBuffers(ProfileData profileData, ProfileData intervalProfileData) {
		ProfileData pd = new ProfileData();
		pd.setChannelInfos(profileData.getChannelInfos());
		Iterator<IntervalData> it = profileData.getIntervalIterator();
		IntervalData id;
		while(it.hasNext()){
			id = it.next();
			pd.addInterval(id);
		}
		it = intervalProfileData.getIntervalIterator();
		while(it.hasNext()){
			id = it.next();
			pd.addInterval(id);
		}
		return pd;
	}
		
}
