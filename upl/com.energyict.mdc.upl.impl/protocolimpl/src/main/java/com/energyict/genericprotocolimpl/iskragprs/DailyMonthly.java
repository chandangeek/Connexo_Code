package com.energyict.genericprotocolimpl.iskragprs;

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

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.client.ParseUtils;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;

/**
 * 
 * @author gna
 * Changes:
 * GNA |23012009| When no there are no entries in the loadprofile, no error is thrown, just logging info.
 */

public class DailyMonthly {
	
	private IskraMx37x iskramx37x;
	
	public DailyMonthly(){
	}
	
	public DailyMonthly(IskraMx37x iskramx37x){
		this.iskramx37x = iskramx37x;
	}

	public void getDailyValues(ObisCode dailyObisCode) throws IOException, SQLException, BusinessException {
		ProfileData profileData = new ProfileData( );
		try {
			ProfileGeneric genericProfile = getCosemObjectFactory().getProfileGeneric(dailyObisCode);
			List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.DAYS);
			
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
			iskramx37x.getLogger().log(Level.INFO, "Reading Daily values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
			DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
			buildProfileData(dc, profileData, genericProfile);
			ProfileData pd = sortOutProfiledate(profileData, TimeDuration.DAYS);
			getMeter().store(pd, false);
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}
		
	}

	private List<ChannelInfo> getDailyMonthlyChannelInfos(ProfileGeneric profile, int timeDuration) throws IOException {
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){
				
				if(ParseUtils.isElectricityObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode())){ // make a channel out of it
					CapturedObject co = ((CapturedObject)profile.getCaptureObjects().get(i));
					ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());
					if(timeDuration == TimeDuration.DAYS){
						ci = new ChannelInfo(index, getDailyChannelNumber(index+1), "IskraMx372_Daily_"+index, su.getUnit());
					} else if(timeDuration == TimeDuration.MONTHS){
						ci = new ChannelInfo(index, getMonthlyChannelNumber(index+1), "IskraMx372_Montly_"+index, su.getUnit());
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

	public void getMonthlyValues(ObisCode monthlyObisCode) throws IOException, SQLException, BusinessException {
		ProfileData profileData = new ProfileData( );
		try {
			ProfileGeneric genericProfile = getCosemObjectFactory().getProfileGeneric(monthlyObisCode);
			List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.MONTHS);
			
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
			
			iskramx37x.getLogger().log(Level.INFO, "Reading Monthly values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
			DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
			buildProfileData(dc, profileData, genericProfile);
			ProfileData pd = sortOutProfiledate(profileData, TimeDuration.MONTHS);
			getMeter().store(pd, false);
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}
	}
	
	/**
	 * Read the given object and return the scalerUnit
	 * @param oc
	 * @return
	 * @throws IOException
	 */
	private ScalerUnit getMeterDemandRegisterScalerUnit(ObisCode oc) throws IOException{
		try {
			return getCosemObjectFactory().getCosemObject(oc).getScalerUnit();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not get the scalerunit from object '" + oc + "'.");
		}
	}
	
	private ProfileData sortOutProfiledate(ProfileData profileData, int timeDuration) {
		ProfileData pd = new ProfileData();
		pd.setChannelInfos(profileData.getChannelInfos());
		Iterator<IntervalData> it = profileData.getIntervalIterator();
		while(it.hasNext()){
			IntervalData id = it.next();
			switch(timeDuration){
			case TimeDuration.DAYS:{
				if(checkDailyBillingTime(id.getEndTime()))
					pd.addInterval(id);
			}break;
			case TimeDuration.MONTHS:{
				if(checkMonthlyBillingTime(id.getEndTime()))
					pd.addInterval(id);
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
		if(cal.get(Calendar.HOUR)==0 && cal.get(Calendar.MINUTE)==0 && cal.get(Calendar.SECOND)==0 && cal.get(Calendar.MILLISECOND)==0)
			return true;
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
		if(checkDailyBillingTime(date) && cal.get(Calendar.DAY_OF_MONTH)==1)
			return true;
		return false;
	}
	
	private int getDailyChannelNumber(int index){
		int channelIndex = 0;
		for(int i = 0; i < getMeter().getChannels().size(); i++){
			if(getMeter().getChannel(i).getInterval().getTimeUnitCode() == TimeDuration.DAYS){
				channelIndex++;
				if(channelIndex == index){
					return getMeter().getChannel(i).getLoadProfileIndex() -1;
				}
			}
		}
		return -1;
	}
	
	private int getMonthlyChannelNumber(int index){
		int channelIndex = 0;
		for(int i = 0; i < getMeter().getChannels().size(); i++){
			if(getMeter().getChannel(i).getInterval().getTimeUnitCode() == TimeDuration.MONTHS){
				channelIndex++;
				if(channelIndex == index){
					return getMeter().getChannel(i).getLoadProfileIndex() -1;
				}
			}
		}
		return -1;
	}
	
	private void buildProfileData(DataContainer dc, ProfileData pd, ProfileGeneric pg) throws IOException{
		
		//TODO check how this reacts with the profile.
		
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){
			for(int i = 0; i < dc.getRoot().getElements().length; i++){
				cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
				if(cal != null){				
					currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg);
					if(currentInterval != null){
						pd.addInterval(currentInterval);
					}
				}
			}
		} else {
			this.iskramx37x.getLogger().log(Level.INFO, "No entries in LoadProfile");
		}
	}
	
	private IntervalData getIntervalData(DataStructure ds, Calendar cal, int status, ProfileGeneric pg)throws IOException{
		
		IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(ParseUtils.isElectricityObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())){
					id.addValue(new Integer(ds.getInteger(i)));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
		}
		
		return id;
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
		return this.iskramx37x.getCosemObjectFactory();
	}
	
	private Rtu getMeter(){
		return this.iskramx37x.getMeter();
	}
	
	private Calendar getToCalendar(){
		return this.iskramx37x.getToCalendar();
	}
	
	private Calendar getFromCalendar(Channel channel){
		return this.iskramx37x.getFromCalendar(channel);
	}
	
	private DLMSMeterConfig getMeterConfig(){
		return this.iskramx37x.getMeterConfig();
	}
	
	private TimeZone getTimeZone(){
		return this.iskramx37x.getTimeZone();
	}
}
