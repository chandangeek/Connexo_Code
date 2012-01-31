package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.genericprotocolimpl.webrtuz3.TicDevice;
import com.energyict.mdw.core.Channel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

public class TicProfile {

	private final TicDevice ticDevice;
	
	public TicProfile(final TicDevice ticDevice) {
		this.ticDevice = ticDevice;
	}

	public ProfileData getProfileData(final ObisCode obisCode) throws IOException {
		final ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;
		
		genericProfile = getCosemObjectFactory().getProfileGeneric(obisCode);
		final List<ChannelInfo> channelInfos = getChannelInfos(genericProfile);
		
		if(channelInfos.size() != 0){
			
			verifyProfileInterval(genericProfile);
			
			profileData.setChannelInfos(channelInfos);
			Calendar fromCalendar = null;
			Calendar channelCalendar = null;
			final Calendar toCalendar = getToCalendar();
			
			for (int i = 0; i < this.ticDevice.getMeter().getChannels().size(); i++) {
				final Channel chn = this.ticDevice.getMeter().getChannel(i);
				channelCalendar = getFromCalendar(chn);
				
				if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
					fromCalendar = channelCalendar;
				}
			}
			
			this.ticDevice.getWebRTU().getLogger().log(Level.INFO, "Retrieving profiledata from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
			
			final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
			buildProfileData(dc, profileData, genericProfile);
			ParseUtils.validateProfileData(profileData, toCalendar.getTime());
			profileData.sort();
		}
		
		return profileData;
	}
	
	private void buildProfileData(final DataContainer dc, final ProfileData pd, final ProfileGeneric pg) throws IOException{
		
		
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){
		
			for(int i = 0; i < dc.getRoot().getElements().length; i++){
				
				if(dc.getRoot().getStructure(i).isOctetString(0)){
					cal = new AXDRDateTime(OctetString.fromByteArray(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
				} else {
					if(cal != null){
						cal.add(Calendar.SECOND, this.ticDevice.getWebRTU().getMeter().getIntervalInSeconds());
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
			this.ticDevice.getWebRTU().getLogger().info("No entries in LoadProfile");
		}
	}
	
	private IntervalData getIntervalData(final DataStructure ds, final Calendar cal, final int status, final ProfileGeneric pg, final List channelInfos)throws IOException{
		
		final IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		int index = 0;
		
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(index < channelInfos.size()){
					if(isValidProfileObisCode((CapturedObject)pg.getCaptureObjects().get(i))){
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
	
	private int getProfileClockChannelIndex(final ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(this.ticDevice.getWebRTU().getMeterConfig().getClockObject().getObisCode())){
					return i;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
		}
		return -1;
	}
	
	private int getProfileStatusChannelIndex(final ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++){
				if(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(this.ticDevice.getWebRTU().getMeterConfig().getStatusObject().getObisCode())){
					return i;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's status attribute.");
		}
		return -1;
	}
	
	private Calendar getToCalendar(){
		return this.ticDevice.getWebRTU().getToCalendar();
	}
	
	private Calendar getFromCalendar(final Channel channel){
		return this.ticDevice.getWebRTU().getFromCalendar(channel);
	}
	
	private void verifyProfileInterval(final ProfileGeneric genericProfile) throws IOException{
		final Iterator<Channel> it = this.ticDevice.getMeter().getChannels().iterator();
		while(it.hasNext()){
			final Channel channel = it.next();
			if(channel.getIntervalInSeconds() != genericProfile.getCapturePeriod()){
				throw new IOException("Interval mismatch, Channel: " + channel + " has a different interval(" + channel.getIntervalInSeconds() +
						"s) as configured in the meter " + genericProfile.getCapturePeriod());
			}
		}
	}

	/**
	 * Construct a list of channelInfo objects
	 * @param genericProfile
	 * @return the list
	 * @throws IOException if we can't get the captured objects
	 */
	private List<ChannelInfo> getChannelInfos(final ProfileGeneric genericProfile) throws IOException {
		final List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		int channelIndex = -1;
		try{
			for(int i = 0; i < genericProfile.getCaptureObjects().size(); i++){
				final CapturedObject co = ((CapturedObject)genericProfile.getCaptureObjects().get(i));
				if(isValidProfileObisCode(co)){
					
					channelIndex = getProfileChannelNumber(index+1);
					if(channelIndex != -1){
						final ScalerUnit su = getScalerUnit(co);
						if((su != null) && (su.getUnitCode() != 0)){
							ci = new ChannelInfo(index, channelIndex, "TicDevice_"+index, su.getEisUnit());
						} else {
							ci = new ChannelInfo(index, channelIndex, "TicDevice_"+index, Unit.get(BaseUnit.UNITLESS));
						}
						index++;
						
						if(com.energyict.dlms.ParseUtils.isObisCodeCumulative(co.getLogicalName().getObisCode())){
//							ci.setCumulative();
							ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
						}
						
						channelInfos.add(ci);
					}
				}
			}
		} catch (final IOException e){
			e.printStackTrace();
			throw new IOException("Failed to build the channelInfos." + e);
		}
		return channelInfos;
	}
	
	/**
	 * Get the rtu channel which corresponds with the loadProfile index channel
	 * @param index
	 * @return the channel number
	 */
	private int getProfileChannelNumber(final int index){
		for(int i = 0; i < this.ticDevice.getMeter().getChannels().size(); i++){
			
			if(this.ticDevice.getMeter().getChannel(i).getLoadProfileIndex() == index){
				return i;
			}
		}
		return -1;
	
	}
	
	/**
	 * Get the scalerUnit from an Object, if it's not a valid scalerUnit, then return a unitLess scalerUnit
	 * @param capturedObject
	 * @return the scalerUnit
	 */
	private ScalerUnit getScalerUnit(final CapturedObject capturedObject){
		try {
			
			if(capturedObject.getLogicalName().getObisCode().toString().equalsIgnoreCase("0.0.96.14.0.255")){
				System.out.println("Test");
			}
			
			ScalerUnit su = getCosemObjectFactory().getCosemObject(capturedObject.getLogicalName().getObisCode()).getScalerUnit();
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
			this.ticDevice.getWebRTU().getLogger().log(Level.INFO, "Could not get the scalerunit from object '" + capturedObject.getLogicalName().getObisCode() + "'.");
		}
		return new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
	}

	/**
	 * Checks if the object is an object that can contain a measurement data
	 * @param capturedObject
	 * @return
	 * @throws IOException 
	 */
	private boolean isValidProfileObisCode(final CapturedObject capturedObject) throws IOException {
		if(((capturedObject.getClassId() == 1) || // DATA
				(capturedObject.getClassId() == 3) || // Register
				(capturedObject.getClassId() == 4) || // Extended register
				(capturedObject.getClassId() == 5) // Demand register
				) && !isProfileStatusObisCode(capturedObject.getLogicalName().getObisCode())){
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isProfileStatusObisCode(final ObisCode oc) throws IOException{
		return oc.equals(this.ticDevice.getWebRTU().getMeterConfig().getStatusObject().getObisCode());
	}

	private CosemObjectFactory getCosemObjectFactory(){
		return this.ticDevice.getWebRTU().getCosemObjectFactory();
	}
}
