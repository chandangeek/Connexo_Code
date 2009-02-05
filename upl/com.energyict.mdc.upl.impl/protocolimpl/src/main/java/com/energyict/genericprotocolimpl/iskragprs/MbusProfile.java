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
 * GNA |29012009| When the ScalerUnit is (0, 0) then return a Unitless scalerUnit, otherwise you get errors.
 */

public class MbusProfile {
	
	private MbusDevice mbusDevice;
	
	public MbusProfile(){
	}
	
	public MbusProfile(MbusDevice mbusDevice){
		this.mbusDevice = mbusDevice;
	}
	
	public void getProfile(ObisCode mbusProfile) throws IOException, SQLException, BusinessException{
		ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;
		try {
			genericProfile = getCosemObjectFactory().getProfileGeneric(mbusProfile);
			List<ChannelInfo> channelInfos = getMbusChannelInfos(genericProfile);
			
			profileData.setChannelInfos(channelInfos);
			Calendar fromCalendar = Calendar.getInstance(mbusDevice.getIskraDevice().getTimeZone());
			Calendar channelCalendar = null;
			Calendar toCalendar = getToCalendar();
			
			for (int i = 0; i < getMeter().getChannels().size(); i++) {
				Channel chn = getMeter().getChannel(i);
				if(!(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS) && 
						!(chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS)){
					channelCalendar = getFromCalendar(getMeter().getChannel(i));
					if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
						fromCalendar = channelCalendar;
					}
				}
			}
			this.mbusDevice.getLogger().log(Level.INFO, "Retrieving profiledata from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
			DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
			buildProfileData(dc, profileData, genericProfile);
			profileData.sort();
			getMeter().store(profileData, false);
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (SQLException e){
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} catch (BusinessException e){
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}
	}

	private List<ChannelInfo> getMbusChannelInfos(ProfileGeneric profile) throws IOException {
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){
				
				if(mbusDevice.isIskraMbusObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode())){ // make a channel out of it
					CapturedObject co = ((CapturedObject)profile.getCaptureObjects().get(i));
					ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());
					if(su != null) {
						ci = new ChannelInfo(index, getProfileChannelNumber(index+1), "IskraMx372_MBus_"+index, su.getUnit());
					} else {
						ci = new ChannelInfo(index, getProfileChannelNumber(index+1), "IskraMx372_MBus_"+index, Unit.get(BaseUnit.UNITLESS));
					}
					
					index++;
					// We do not do the check because we know it is a cumulative value
//					if(ParseUtils.isObisCodeCumulative(co.getLogicalName().getObisCode())){
						//TODO need to check the wrapValue
						ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
//					}
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
	 * Read the given object and return the scalerUnit
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
			throw new IOException("Could not get the scalerunit from object '" + oc + "'.");
		}
	}
	
	private int getProfileChannelNumber(int index){
		int channelIndex = 0;
		for(int i = 0; i < getMeter().getChannels().size(); i++){
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
		
		//TODO check how this reacts with the profile.
		
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){
		
			for(int i = 0; i < dc.getRoot().getElements().length; i++){
				if(dc.getRoot().getStructure(i).isOctetString(0)){
					cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
				} else {
					//TODO get the interval of the meter itself
	//				cal.add(Calendar.SECOND, 3600);
					if(cal != null){
						cal.add(Calendar.SECOND, mbusDevice.getMbus().getIntervalInSeconds());
					}
				}
				if(cal != null){				
					currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg);
					if(currentInterval != null){
						pd.addInterval(currentInterval);
					}
				}
			}
		} else {
			this.mbusDevice.getLogger().log(Level.INFO, "No entries in LoadProfile");
		}
	}
	
	private IntervalData getIntervalData(DataStructure ds, Calendar cal, int status, ProfileGeneric pg)throws IOException{
		
		IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(mbusDevice.isIskraMbusObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())){
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
		return this.mbusDevice.getIskraDevice().getCosemObjectFactory();
	}
	
	private Rtu getMeter(){
		return this.mbusDevice.getMbus();
	}
	
	private Calendar getToCalendar(){
		return this.mbusDevice.getIskraDevice().getToCalendar();
	}
	
	private Calendar getFromCalendar(Channel channel){
		return this.mbusDevice.getIskraDevice().getFromCalendar(channel);
	}
	
	private DLMSMeterConfig getMeterConfig(){
		return this.mbusDevice.getIskraDevice().getMeterConfig();
	}
	
	private TimeZone getTimeZone(){
		return this.mbusDevice.getIskraDevice().getTimeZone();
	}
}
