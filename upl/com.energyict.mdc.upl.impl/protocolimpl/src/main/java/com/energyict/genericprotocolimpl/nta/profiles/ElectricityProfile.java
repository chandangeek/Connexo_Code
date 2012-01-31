package com.energyict.genericprotocolimpl.nta.profiles;

import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.genericprotocolimpl.common.pooling.ChannelFullProtocolShadow;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElectricityProfile extends AbstractNTAProfile{
	
	private final boolean DEBUG = false;
	
	protected AbstractNTAProtocol webrtu;
	
	public ElectricityProfile(){
	}
	
	public ElectricityProfile(final AbstractNTAProtocol webrtu){
		this.webrtu = webrtu;
	}
	
	public ProfileData getProfile(final ObisCode obisCode) throws IOException {
		return getProfile(obisCode, false);
	}
	
	public ProfileData getProfile(final ObisCode electricityProfile, final boolean events) throws IOException {
		final ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;
		
		try {
			genericProfile = getCosemObjectFactory().getProfileGeneric(electricityProfile);
			final List<ChannelInfo> channelInfos = getChannelInfos(genericProfile);
			
			if(channelInfos.size() != 0){
				getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + webrtu.getSerialNumberValue());
				verifyProfileInterval(genericProfile, channelInfos);
				
				profileData.setChannelInfos(channelInfos);
				Calendar fromCalendar = null;
				Calendar channelCalendar = null;
				final Calendar toCalendar = getToCalendar();

                if (webrtu.isRequestOneDay()) {
                    fromCalendar = ProtocolUtils.getCalendar(webrtu.getTimeZone());
                    fromCalendar.add(Calendar.HOUR, -24);
                    webrtu.getLogger().log(Level.INFO, "Requesting One Day - from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
                } else {
                    for (ChannelFullProtocolShadow channelFPS : webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow()) {
                        if (!(channelFPS.getTimeDuration().getTimeUnitCode() == TimeDuration.DAYS) &&
                                !(channelFPS.getTimeDuration().getTimeUnitCode() == TimeDuration.MONTHS)) {
                            channelCalendar = getFromCalendar(channelFPS);
                            if ((fromCalendar == null) || (channelCalendar.before(fromCalendar))) {
                                fromCalendar = channelCalendar;
                            }
                        }
                    }
                    getLogger().log(Level.INFO, "Retrieving profiledata from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
                }

				final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				profileData.sort();
                return profileData;
            }

        } catch (final IOException e) {
            throw new IOException(e.getMessage());
        }
        return null;
	}

    /**
     * @return the used {@link java.util.logging.Logger}
     */
    @Override
    protected Logger getLogger() {
        return webrtu.getLogger();
    }

	private void verifyProfileInterval(final ProfileGeneric genericProfile, final List<ChannelInfo> channelInfos) throws IOException{
		final Iterator<ChannelInfo> it = channelInfos.iterator();
		while(it.hasNext()){
			final ChannelInfo ci = it.next();
			if(webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().get(ci.getId()).getIntervalInSeconds() != genericProfile.getCapturePeriod()){
				throw new IOException("Interval mismatch, EIServer: " + webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().get(ci.getId()).getIntervalInSeconds() + "s - Meter: " + genericProfile.getCapturePeriod() + "s.");
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
							ci = new ChannelInfo(index, channelIndex, "NTA_"+index, su.getEisUnit());
						} else {
							throw new ProtocolException("Meter does not report a proper scalerUnit for all channels of his Electricity LoadProfile, data can not be interpreted correctly.");
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
			throw new IOException("Failed to build the channelInfos." + e);
		}
		return channelInfos;
	}

    protected int getProfileChannelNumber(final int index) {
        int channelIndex = 0;
        for (int i = 0; i < webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().size(); i++) {

            if (!(webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().get(i).getTimeDuration().getTimeUnitCode() == TimeDuration.DAYS) &&
                    !(webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().get(i).getTimeDuration().getTimeUnitCode() == TimeDuration.MONTHS)) {
                channelIndex++;
                if (channelIndex == index) {
                    return webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().get(i).getLoadProfileIndex() - 1;
                }
            }
        }
        return -1;
    }

    /**
     * Construct the profileData object for the received data
     *
     * @param dc
     *          the datacontainer constructed from the received byteArray
     *
     * @param pd
     *          the {@link ProfileData} object to put in the data
     *
     * @param pg
     *          the {@link com.energyict.dlms.cosem.ProfileGeneric} object that contains profile information
     *
     * @throws IOException
     */
	protected void buildProfileData(final DataContainer dc, final ProfileData pd, final ProfileGeneric pg) throws IOException{
		
		
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
					cal = new AXDRDateTime(OctetString.fromByteArray(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
				} else {
					if(cal != null){
						cal.add(Calendar.SECOND, pg.getCapturePeriod());
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
			getLogger().info("No entries in LoadProfile");
		}
	}
	
	protected boolean isProfileStatusObisCode(final ObisCode oc) throws IOException{
		return oc.equals(getMeterConfig().getStatusObject().getObisCode());
	}
	
	protected IntervalData getIntervalData(final DataStructure ds, final Calendar cal, final int status, final ProfileGeneric pg, final List channelInfos)throws IOException{
		
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
	
	protected int getProfileStatusChannelIndex(final ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++){
				if(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(getMeterConfig().getStatusObject().getObisCode())){
					return i;
				}
			}
		} catch (final IOException e) {
			throw new IOException("Could not retrieve the index of the profileData's status attribute.");
		}
		return -1;
	}
	
	protected int getProfileClockChannelIndex(final ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(getMeterConfig().getClockObject().getObisCode())){
					return i;
				}
			}
		} catch (final IOException e) {
			throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
		}
		return -1;
	}

	protected CosemObjectFactory getCosemObjectFactory(){
		return this.webrtu.getCosemObjectFactory();
	}

	private Calendar getToCalendar(){
		return this.webrtu.getToCalendar();
	}
	
	private Calendar getFromCalendar(ChannelFullProtocolShadow channelFPS){
		return this.webrtu.getFromCalendar(channelFPS.getLastReading(), webrtu.getTimeZone());
	}

    /**
     * Protected getter for the {@link com.energyict.dlms.DLMSMeterConfig}
     * @return the DLMSMeterConfig
     */
	protected DLMSMeterConfig getMeterConfig(){
		return this.webrtu.getMeterConfig();
	}

}
