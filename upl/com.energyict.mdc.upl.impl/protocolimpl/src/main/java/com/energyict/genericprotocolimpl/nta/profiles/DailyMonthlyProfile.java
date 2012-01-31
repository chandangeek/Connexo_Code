package com.energyict.genericprotocolimpl.nta.profiles;

import com.energyict.cbo.ApplicationException;
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

public class DailyMonthlyProfile extends AbstractNTAProfile{
	
	protected AbstractNTAProtocol webrtu;
	
	public DailyMonthlyProfile(){
	}
	
	public DailyMonthlyProfile(final AbstractNTAProtocol webrtu){
		this.webrtu = webrtu;
	}

	public ProfileData getDailyValues(final ObisCode dailyObisCode) throws IOException{
		final ProfileData profileData = new ProfileData( );
		try {
			final ProfileGeneric genericProfile = getCosemObjectFactory().getProfileGeneric(dailyObisCode);
			final List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.DAYS);
			if(channelInfos.size() != 0){
				
				webrtu.getLogger().log(Level.INFO, "Getting daily values for meter with serialnumber: " + webrtu.getSerialNumberValue());
				
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
                         if (channelFPS.getTimeDuration().getTimeUnitCode() == TimeDuration.DAYS) {
                             channelCalendar = getFromCalendar(channelFPS);
                             if ((fromCalendar == null) || (channelCalendar.before(fromCalendar))) {
                                 fromCalendar = channelCalendar;
                             }
                         }
                     }
                     webrtu.getLogger().log(Level.INFO, "Reading Daily values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
                 }

				final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile, TimeDuration.DAYS);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				final ProfileData pd = sortOutProfiledate(profileData, TimeDuration.DAYS);
				pd.sort();
                return pd;
            }

        } catch (final IOException e) {
            throw new IOException(e.getMessage());
        }
        return null;
	}

	private List<ChannelInfo> getDailyMonthlyChannelInfos(final ProfileGeneric profile, final int timeDuration) throws IOException {
		final List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		int channelIndex = -1;
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){
				
				if(com.energyict.dlms.ParseUtils.isElectricityObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode())){ // make a channel out of it
					final CapturedObject co = ((CapturedObject)profile.getCaptureObjects().get(i));
					final ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());
					if(timeDuration == TimeDuration.DAYS){
						channelIndex = getDailyChannelNumber(index+1);
					} else if(timeDuration == TimeDuration.MONTHS){
						channelIndex = getMonthlyChannelNumber(index+1);
					}
					
					if(channelIndex != -1){
                        if((su != null) && (su.getUnitCode() != 0)){
						ci = new ChannelInfo(index, channelIndex, "WebRtuKP_DayMonth_"+index, su.getEisUnit());
                        } else {
                            throw new ProtocolException("Meter does not report a proper scalerUnit for all channels of his DailyMonthly LoadProfile, data can not be interpreted correctly.");
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

	public ProfileData getMonthlyValues(final ObisCode monthlyObisCode) throws IOException{
		final ProfileData profileData = new ProfileData( );
		try {
			final ProfileGeneric genericProfile = getCosemObjectFactory().getProfileGeneric(monthlyObisCode);
			final List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.MONTHS);
			if(channelInfos.size() != 0){
				
				webrtu.getLogger().log(Level.INFO, "Getting monthly values for meter with serialnumber: " + webrtu.getSerialNumberValue());
				
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
                        if (channelFPS.getTimeDuration().getTimeUnitCode() == TimeDuration.MONTHS) {
                            channelCalendar = getFromCalendar(channelFPS);
                            if ((fromCalendar == null) || (channelCalendar.before(fromCalendar))) {
                                fromCalendar = channelCalendar;
                            }
                        }
                    }
                    webrtu.getLogger().log(Level.INFO, "Reading Monthly values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
                }

				final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile, TimeDuration.MONTHS);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				final ProfileData pd = sortOutProfiledate(profileData, TimeDuration.MONTHS);
				pd.sort();
                return pd;
            }

        } catch (final IOException e) {
            throw new IOException(e.getMessage());
        }
        return null;
	}

	private ProfileData sortOutProfiledate(final ProfileData profileData, final int timeDuration) {
		final ProfileData pd = new ProfileData();
		pd.setChannelInfos(profileData.getChannelInfos());
		final Iterator<IntervalData> it = profileData.getIntervalIterator();
		while(it.hasNext()){
			final IntervalData id = it.next();
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
	private boolean checkDailyBillingTime(final Date date){
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(cal.get(Calendar.HOUR)==0 && cal.get(Calendar.MINUTE)==0 && cal.get(Calendar.SECOND)==0 && cal.get(Calendar.MILLISECOND)==0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the given data is a date at midnight on the first of the month
	 * @param date
	 * @return true or false
	 */
	private boolean checkMonthlyBillingTime(final Date date){
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(checkDailyBillingTime(date) && cal.get(Calendar.DAY_OF_MONTH)==1) {
			return true;
		}
		return false;
	}
	
	private int getDailyChannelNumber(final int index){
        return getChannelNumberForTimeDuration(index, TimeDuration.DAYS);
	}

    private int getMonthlyChannelNumber(final int index) {
        return getChannelNumberForTimeDuration(index, TimeDuration.MONTHS);
    }

    private int getChannelNumberForTimeDuration(final int index, final int timeDuration) {
        int channelIndex = 0;
        for (int i = 0; i < webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().size(); i++) {
            if (webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().get(i).getTimeDuration().getTimeUnitCode() == timeDuration) {
                channelIndex++;
                if (channelIndex == index) {
                    return webrtu.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().get(i).getLoadProfileIndex() - 1;
                }
            }
        }
        return -1;
    }
	
	protected void buildProfileData(final DataContainer dc, final ProfileData pd, final ProfileGeneric pg, final int timeDuration) throws IOException{
		
		try {
			Calendar cal = null;
			IntervalData currentInterval = null;
			int profileStatus = 0;
			if(dc.getRoot().getElements().length != 0){
				for(int i = 0; i < dc.getRoot().getElements().length; i++){
					
					if(dc.getRoot().getStructure(i).isOctetString(0)){
//						cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
						cal = new AXDRDateTime(OctetString.fromByteArray(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
					} else {
						if(cal != null){
							if(timeDuration == TimeDuration.DAYS){
								cal.add(Calendar.DAY_OF_MONTH, 1);
							} else if(timeDuration == TimeDuration.MONTHS){
								cal.add(Calendar.MONTH, 1);
							} else {
								throw new ApplicationException("TimeDuration is not correct.");
							}
						}
					}
					if(getProfileStatusChannelIndex(pg) != -1){
						profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
					} else {
						profileStatus = 0;
					}
					
					if(cal != null){				
						currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
						if(currentInterval != null){
							pd.addInterval(currentInterval);
						}
					}
				}
			} else {
				webrtu.getLogger().info("No entries in LoadProfile");
			}
		} catch (final ClassCastException e) {
			throw new ClassCastException("Configuration of the profile probably not correct.");
		}
	}
	
	/**
	 * Add a value to the intervalData when it is a valid ObisCode.
	 * The number of intervals has to be the same as the number of channelInfos
	 * @param ds - The dataStructre containing the values
	 * @param cal - The time of the interval
	 * @param status - The status of the interval
	 * @param pg - The genericProfile object
	 * @param channelInfos - The list of channelInfos
	 * @return the intervalData
	 * @throws IOException
	 */
    protected IntervalData getIntervalData(final DataStructure ds, final Calendar cal, final int status, final ProfileGeneric pg, final List channelInfos)throws IOException{
		
		final IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		int index = 0;
		
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(index < channelInfos.size()){
					if(com.energyict.dlms.ParseUtils.isElectricityObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())){
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

    /**
     * @return the used {@link java.util.logging.Logger}
     */
    @Override
    protected Logger getLogger() {
        return this.webrtu.getLogger();
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
	
	protected DLMSMeterConfig getMeterConfig(){
		return this.webrtu.getMeterConfig();
	}
}
