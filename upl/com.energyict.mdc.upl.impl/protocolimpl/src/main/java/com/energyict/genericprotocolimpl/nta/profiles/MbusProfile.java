package com.energyict.genericprotocolimpl.nta.profiles;

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
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MbusProfile extends AbstractNTAProfile{
	
	protected AbstractMbusDevice mbusDevice;
	
	public MbusProfile(){
	}
	
	public MbusProfile(AbstractMbusDevice mbusDevice){
		this.mbusDevice = mbusDevice;
	}

	public void getProfile(ObisCode obisCode) throws IOException, SQLException, BusinessException {
		getProfile(obisCode, false);
	}
	
	public void getProfile(ObisCode mbusProfile, boolean events) throws IOException, SQLException, BusinessException{
		ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;
		
		
		try {
			genericProfile = getCosemObjectFactory().getProfileGeneric(mbusProfile);
			List<ChannelInfo> channelInfos = getMbusChannelInfos(genericProfile);
			
			if(channelInfos.size() != 0){
				
				profileData.setChannelInfos(channelInfos);
				Calendar fromCalendar = null;
				Calendar channelCalendar = null;
				Calendar toCalendar = getToCalendar();
				
				for (int i = 0; i < getMeter().getChannels().size(); i++) {
					Channel chn = getMeter().getChannel(i);
					
					// TODO does not work with the 7.5
					
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
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				profileData.sort();
				
				
				if(mbusDevice.getWebRTU().getMarkedAsBadTime()){
					profileData.markIntervalsAsBadTime();
				}
				// We save the profileData to a tempObject so we can store everything at the end of the communication
				mbusDevice.getWebRTU().getStoreObject().add(getMeter(), profileData);
				
			}
			
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}
	}

	private List<ChannelInfo> getMbusChannelInfos(ProfileGeneric profile) throws IOException {
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		int channelIndex = -1;
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){
				
				// Normally the mbusData is in a separate profile
				if(isMbusRegisterObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode())){
					
					channelIndex = getProfileChannelNumber(index+1);
					if(channelIndex != -1){
						CapturedObject co = ((CapturedObject)profile.getCaptureObjects().get(i));
						ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode(), co.getClassId());
						if((su != null) && (su.getUnitCode() != 0)) {
							ci = new ChannelInfo(index, channelIndex, "WebRtuKP_MBus_"+index, su.getUnit());
						} else {
                            throw new ProtocolException("Meter does not report a proper scalerUnit for all channels of his MBus Hourly LoadProfile, data can not be interpreted correctly.");
						}
						
						index++;
						// We do not do the check because we know it is a cumulative value
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

	protected boolean isMbusRegisterObisCode(ObisCode oc) throws IOException {
        return this.mbusDevice.getObiscodeProvider().getMasterRegisterTotal().equals(oc) ||
                this.mbusDevice.getObiscodeProvider().getMasterRegisterValue1(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.mbusDevice.getObiscodeProvider().getMasterRegisterValue2(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.mbusDevice.getObiscodeProvider().getMasterRegisterValue3(this.mbusDevice.getPhysicalAddress()).equals(oc) ||
                this.mbusDevice.getObiscodeProvider().getMasterRegisterValue4(this.mbusDevice.getPhysicalAddress()).equals(oc);
		}

	private int getProfileChannelNumber(int index){
		int channelIndex = 0;
		for(int i = 0; i < getMeter().getChannels().size(); i++){
			
			//TODO does not work with the 7.5, only in the 8.X
			
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
	
	protected void buildProfileData(DataContainer dc, ProfileData pd, ProfileGeneric pg) throws IOException{
		
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){
		
			for(int i = 0; i < dc.getRoot().getElements().length; i++){
				if(dc.getRoot().getStructure(i).isOctetString(0)){
//					cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
					cal = new AXDRDateTime(new OctetString(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
				} else {
					if(cal != null){
						cal.add(Calendar.SECOND, mbusDevice.getMbus().getIntervalInSeconds());
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
			mbusDevice.getLogger().info("No entries in MbusLoadProfile");
		}
	}
	
	protected IntervalData getIntervalData(DataStructure ds, Calendar cal, int status, ProfileGeneric pg, List channelInfos)throws IOException{
		
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
	
	protected int getProfileClockChannelIndex(ProfileGeneric pg) throws IOException{
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

	protected int getProfileStatusChannelIndex(ProfileGeneric pg) throws IOException{
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

}
