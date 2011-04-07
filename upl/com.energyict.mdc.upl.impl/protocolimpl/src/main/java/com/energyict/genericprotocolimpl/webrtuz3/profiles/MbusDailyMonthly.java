package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.genericprotocolimpl.webrtuz3.MbusDevice;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 
 * @author gna
 *
 * Reminder:
 * The dailyProfile is completely constructed in the protocol. It uses the hourly values to get the midnight intervals.
 */
public class MbusDailyMonthly extends AbstractDLMSProfile {

	private MbusDevice mbusDevice;
	
	public MbusDailyMonthly(){
	}
	
	public MbusDailyMonthly(MbusDevice mbusDevice){
		this.mbusDevice = mbusDevice;
	}
	
	public ProfileData getMonthlyProfile(ObisCode monthlyObiscode) throws IOException{
		ProfileData profileData = new ProfileData( );

        if (!getMeterConfig().isObisCodeInObjectList(monthlyObiscode)) {
            mbusDevice.getLogger().warning("No monthly values found in device. [" + getMeter() + "]");
            return profileData;
        }

		ProfileGeneric genericProfile;
		
		genericProfile = getCosemObjectFactory().getProfileGeneric(monthlyObiscode);
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
			
			DataContainer dc = genericProfile.getBuffer(fromCalendar);
			buildProfileData(dc, profileData, genericProfile, TimeDuration.MONTHS);
			ParseUtils.validateProfileData(profileData, toCalendar.getTime());
			ProfileData pd = sortOutProfiledate(profileData, TimeDuration.MONTHS);
			
            pd.sort();

//			if(mbusDevice.getWebRTU().getMarkedAsBadTime()){
//				pd.markIntervalsAsBadTime();
//			}
			
//			mbusDevice.getWebRTU().getStoreObject().add(pd, getMeter());
			return pd;
			
		}
		return profileData;
		
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
			e.printStackTrace();
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
            List<CapturedObject> captureObjects = profile.getCaptureObjects();
            for(int i = 0; i < captureObjects.size(); i++){

                CapturedObject co = captureObjects.get(i);

                if(isMbusRegisterObisCode(co.getLogicalName().getObisCode())){ // make a channel out of it
					Unit unit = getUnit(co.getLogicalName().getObisCode());
					channelIndex = getDMChannelNumber(index+1, timeDuration);

//					if(timeDuration == TimeDuration.DAYS){
//						channelIndex = getDMChannelNumber(index+1);
//					} else if(timeDuration == TimeDuration.MONTHS){
//						channelIndex = getDMChannelNumber(index+1);
//					}

					if(channelIndex != -1){
						ci = new ChannelInfo(index, channelIndex, "WebRtuKP_Mbus_DailyMonthly_"+index, unit);
						index++;
						//TODO need to check the wrapValue
						ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
						channelInfos.add(ci);
					}
					
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
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
	
	private boolean isMbusRegisterObisCode(ObisCode oc){
//		if((oc.getC() == 24) && (oc.getD() == 2) && (oc.getB() >=1) && (oc.getB() <= 4) && (oc.getE() >= 1) && (oc.getE() <= 4) ){
		if((oc.getC() == 24) && (oc.getD() == 2) && (oc.getB() == mbusDevice.getPhysicalAddress()) && (oc.getE() >= 1) && (oc.getE() <= 4) ){
			return true;
		} else {
			return false;
		}
	}
	
	public CosemObjectFactory getCosemObjectFactory(){
		return this.mbusDevice.getWebRTU().getCosemObjectFactory();
	}
	
	private Rtu getMeter(){
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

	/**
	 * Get the dailyValues by reading the complete intervalProfile(hourly)
	 * @param dailyObisCode - the hourly-interval obisCode
	 * @throws IOException 
	 */
	public ProfileData getDailyProfile(ObisCode dailyObisCode) throws IOException {
		ProfileData profileData = new ProfileData();

        if (!getMeterConfig().isObisCodeInObjectList(dailyObisCode)) {
            mbusDevice.getLogger().warning("No daily values found in device. [" + getMeter() + "]");
            return profileData;
        }

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
			
			DataContainer dc = genericProfile.getBuffer(fromCalendar);
			buildProfileData(dc, profileData, genericProfile, TimeDuration.DAYS);
			ParseUtils.validateProfileData(profileData, toCalendar.getTime());
			ProfileData pd = sortOutProfiledate(profileData, TimeDuration.DAYS);
			
            pd.sort();

//			if(mbusDevice.getWebRTU().getMarkedAsBadTime()){
//				pd.markIntervalsAsBadTime();
//			}
//			
//			mbusDevice.getWebRTU().getStoreObject().add(pd, getMeter());
			return pd;
		}
		return profileData;
	}

		
}
