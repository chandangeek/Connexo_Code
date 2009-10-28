package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
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
import com.energyict.genericprotocolimpl.webrtuz3.WebRTUZ3;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;

public class ElectricityProfile {
	
	private final boolean DEBUG = false;
	
	private WebRTUZ3 webrtu;
	
	public ElectricityProfile(){
	}
	
	public ElectricityProfile(final WebRTUZ3 webrtu){
		this.webrtu = webrtu;
	}
	
	public ProfileData getProfile(final ObisCode obisCode) throws IOException, SQLException, BusinessException {
		return getProfile(obisCode, false);
	}
	
	public ProfileData getProfile(final ObisCode electricityProfile, final boolean events) throws IOException, SQLException, BusinessException{
		final ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;
		
		try {
			genericProfile = getCosemObjectFactory().getProfileGeneric(electricityProfile);
			final List<ChannelInfo> channelInfos = getChannelInfos(genericProfile);
			
			if(channelInfos.size() != 0){
				webrtu.getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getMeter().getSerialNumber());
				verifyProfileInterval(genericProfile, channelInfos);
				
				profileData.setChannelInfos(channelInfos);
				Calendar fromCalendar = null;
				Calendar channelCalendar = null;
				final Calendar toCalendar = getToCalendar();
				
				for (int i = 0; i < getMeter().getChannels().size(); i++) {
					final Channel chn = getMeter().getChannel(i);
					
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
				final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				profileData.sort();
				
//				if(webrtu.getMarkedAsBadTime()){
//					profileData.markIntervalsAsBadTime();
//				}
				// We save the profileData to a tempObject so we can store everything at the end of the communication
//				webrtu.getStoreObject().add(getMeter(), profileData);
				return profileData;
				
			}
			
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		return profileData;
	}

	private void verifyProfileInterval(final ProfileGeneric genericProfile, final List<ChannelInfo> channelInfos) throws IOException{
		final Iterator<ChannelInfo> it = channelInfos.iterator();
		while(it.hasNext()){
			final ChannelInfo ci = it.next();
			if(getMeter().getChannel(ci.getId()).getIntervalInSeconds() != genericProfile.getCapturePeriod()){
				throw new IOException("Interval mismatch, EIServer: " + getMeter().getIntervalInSeconds() + "s - Meter: " + genericProfile.getCapturePeriod() + "s.");
			}
		}
	}
	
	private List<ChannelInfo> getChannelInfos(final ProfileGeneric profile) throws IOException {
		final List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		int channelIndex = -1;
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){
				
				if(isKampstrupElectricityObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode()) 
						&& !isProfileStatusObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode())){ // make a channel out of it
					final CapturedObject co = ((CapturedObject)profile.getCaptureObjects().get(i));
					final ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());
					channelIndex = getProfileChannelNumber(index+1);
					if(channelIndex != -1){
						if((su != null) && (su.getUnitCode() != 0)){
							ci = new ChannelInfo(index, channelIndex, "WebRtuKP_"+index, su.getUnit());
						} else {
							ci = new ChannelInfo(index, channelIndex, "WebRtuKP_"+index, Unit.get(BaseUnit.UNITLESS));
						}
						
						index++;
						if(com.energyict.dlms.ParseUtils.isObisCodeCumulative(co.getLogicalName().getObisCode())){
							//TODO need to check the wrapValue
							ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
						}
						channelInfos.add(ci);
					}
				}
				
			}
		} catch (final IOException e) {
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
	private ScalerUnit getMeterDemandRegisterScalerUnit(final ObisCode oc) throws IOException{
		try {
			ScalerUnit su = getCosemObjectFactory().getCosemObject(oc).getScalerUnit();
			if(su != null){
				if(su.getUnitCode() == 0){
					su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
				}
				
			} else {
				su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
			}
			return su;
		} catch (final IOException e) {
			e.printStackTrace();
			webrtu.getLogger().log(Level.INFO, "Could not get the scalerunit from object '" + oc + "'.");
		}
		return new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
	}
	
	private int getProfileChannelNumber(final int index){
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
	
	private void buildProfileData(final DataContainer dc, final ProfileData pd, final ProfileGeneric pg) throws IOException{
		
		
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
					cal = new AXDRDateTime(new OctetString(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
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
					
					currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
					if(currentInterval != null){
						pd.addInterval(currentInterval);
					}
				}
			}
		} else {
			webrtu.getLogger().info("No entries in LoadProfile");
		}
	}
	
	private boolean isProfileStatusObisCode(final ObisCode oc) throws IOException{
		return oc.equals(getMeterConfig().getStatusObject().getObisCode());
	}
	
	private IntervalData getIntervalData(final DataStructure ds, final Calendar cal, final int status, final ProfileGeneric pg, final List channelInfos)throws IOException{
		
		final IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		int index = 0;
		
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(index < channelInfos.size()){
					if(isKampstrupElectricityObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())
							&& !isProfileStatusObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())){
						id.addValue(new Integer(ds.getInteger(i)));
						index++;
					}
					
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
		}
		
		return id;
	}
	
	private boolean isKampstrupElectricityObisCode(final ObisCode obisCode){
		if ((obisCode.getA() == 1) && (((obisCode.getB() >= 0) && (obisCode.getB() <= 64)) || (obisCode.getB() == 128)) ) {
			return true;
		} else {
			return false;
		}
	}
	
	private int getProfileStatusChannelIndex(final ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++){
				if(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(getMeterConfig().getStatusObject().getObisCode())){
					return i;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's status attribute.");
		}
		return -1;
	}
	
	private int getProfileClockChannelIndex(final ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(getMeterConfig().getClockObject().getObisCode())){
					return i;
				}
			}
		} catch (final IOException e) {
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
	
	private Calendar getFromCalendar(final Channel channel){
		return this.webrtu.getFromCalendar(channel);
	}
	
	private DLMSMeterConfig getMeterConfig(){
		return this.webrtu.getMeterConfig();
	}

}
