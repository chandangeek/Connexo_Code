package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class DailyMonthly extends AbstractDLMSProfile {

	private EDevice eDevice;

    /**
     * Default constructor
     */
    public DailyMonthly(){
	}

    /**
     * Constructor with the eDevice
     * @param eDevice
     */
    public DailyMonthly(final EDevice eDevice){
		this.eDevice = eDevice;
	}

    /**
     * Getter for the eDevice field
     * @return
     */
    public EDevice getEDevice() {
        return eDevice;
    }

    /**
     * Geth the daily profileData
     * @param dailyObisCode
     * @return
     * @throws IOException
     * @throws SQLException
     * @throws BusinessException
     */
    public ProfileData getDailyValues(final ObisCode dailyObisCode) throws IOException, SQLException, BusinessException {
		final ProfileData profileData = new ProfileData( );
        if (!getMeterConfig().isObisCodeInObjectList(dailyObisCode)) {
            getEDevice().getLogger().warning("No daily values found in device. [" + getEDevice().getMeter() + "]");
            return profileData;
        }

		try {
			final ProfileGeneric genericProfile = getCosemObjectFactory().getProfileGeneric(dailyObisCode);
			final List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.DAYS);
			if(channelInfos.size() != 0){

				profileData.setChannelInfos(channelInfos);
				Calendar fromCalendar = null;
				Calendar channelCalendar = null;
				final Calendar toCalendar = getToCalendar();
				for (int i = 0; i < getMeter().getChannels().size(); i++) {
					final Channel chn = getMeter().getChannel(i);
					if(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS){ //the channel is a daily channel
						channelCalendar = getFromCalendar(getMeter().getChannel(i));
						if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
							fromCalendar = channelCalendar;
						}
					}
				}

                eDevice.getLogger().log(Level.INFO, "Retrieving Daily values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
				final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile, TimeDuration.DAYS);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				final ProfileData pd = sortOutProfiledate(profileData, TimeDuration.DAYS);
                pd.sort();
				return pd;
			}

		} catch (final IOException e) {
			e.printStackTrace();
			throw e;
		}
		return profileData;
	}

    /**
     * Get the daily/monthly profile data
     * @param profile
     * @param timeDuration
     * @return
     * @throws IOException
     */
    private List<ChannelInfo> getDailyMonthlyChannelInfos(final ProfileGeneric profile, final int timeDuration) throws IOException {
		final List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		int channelIndex = -1;
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){

                CapturedObject capturedObject = profile.getCaptureObjects().get(i);
                ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
                if(com.energyict.dlms.ParseUtils.isElectricityObisCode(obisCode)){ // make a channel out of it
					final Unit unit = getUnit(obisCode);
					if(timeDuration == TimeDuration.DAYS){
						channelIndex = getDailyChannelNumber(index+1);
					} else if(timeDuration == TimeDuration.MONTHS){
						channelIndex = getMonthlyChannelNumber(index+1);
					}

					if(channelIndex != -1){
						ci = new ChannelInfo(index, channelIndex, "WebRtuKP_DayMonth_"+index, unit);
						index++;
						if(com.energyict.dlms.ParseUtils.isObisCodeCumulative(obisCode)){
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
     * Read the monthly registerValues
     * @param monthlyObisCode
     * @return
     * @throws IOException
     * @throws SQLException
     * @throws BusinessException
     */
    public ProfileData getMonthlyValues(final ObisCode monthlyObisCode) throws IOException, SQLException, BusinessException {
		final ProfileData profileData = new ProfileData( );

        if (!getMeterConfig().isObisCodeInObjectList(monthlyObisCode)) {
            getEDevice().getLogger().warning("No monthly values found in device. [" + getEDevice().getMeter() + "]");
            return profileData;
        }

		try {
			final ProfileGeneric genericProfile = getCosemObjectFactory().getProfileGeneric(monthlyObisCode);
			final List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.MONTHS);
			if(channelInfos.size() != 0){

				profileData.setChannelInfos(channelInfos);
				Calendar fromCalendar = null;
				Calendar channelCalendar = null;
				final Calendar toCalendar = getToCalendar();
				for (int i = 0; i < getMeter().getChannels().size(); i++) {
					final Channel chn = getMeter().getChannel(i);
					if(chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS){ //the channel is a daily channel
						channelCalendar = getFromCalendar(getMeter().getChannel(i));
						if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
							fromCalendar = channelCalendar;
						}
					}
				}

                eDevice.getLogger().log(Level.INFO, "Retrieving Monthly values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
				final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile, TimeDuration.MONTHS);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
                profileData.sort();
				final ProfileData pd = sortOutProfiledate(profileData, TimeDuration.MONTHS);
                pd.sort();
				return pd;
			}

		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		return profileData;
	}

    /**
     * 
     * @param profileData
     * @param timeDuration
     * @return
     */
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
		if((cal.get(Calendar.HOUR)==0) && (cal.get(Calendar.MINUTE)==0) && (cal.get(Calendar.SECOND)==0) && (cal.get(Calendar.MILLISECOND)==0)) {
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
		if(checkDailyBillingTime(date) && (cal.get(Calendar.DAY_OF_MONTH)==1)) {
			return true;
		}
		return false;
	}

    /**
     * Get the daily channel index
     *
     * @param index
     * @return
     */
    private int getDailyChannelNumber(final int index) {
        int channelIndex = 0;
        List<Channel> channelList = getMeter().getChannels();
        for (int i = 0; i < channelList.size(); i++) {
            Channel channel = channelList.get(i);
            if (channel.getInterval().getTimeUnitCode() == TimeDuration.DAYS) {
                channelIndex++;
                if (channelIndex == index) {
                    return channel.getLoadProfileIndex() - 1;
                }
            }
        }
        return -1;
    }

    /**
     * Get the monthly channel index
     * @param index
     * @return
     */
    private int getMonthlyChannelNumber(final int index) {
        int channelIndex = 0;
        List<Channel> channelList = getMeter().getChannels();
        for (int i = 0; i < channelList.size(); i++) {
            Channel channel = channelList.get(i);
            if (channel.getInterval().getTimeUnitCode() == TimeDuration.MONTHS) {
                channelIndex++;
                if (channelIndex == index) {
                    return channel.getLoadProfileIndex() - 1;
                }
            }
        }
        return -1;
    }

    /**
     * Build the monthly/daily profileData
     * @param dc
     * @param pd
     * @param pg
     * @param timeDuration
     * @throws IOException
     */
    private void buildProfileData(final DataContainer dc, final ProfileData pd, final ProfileGeneric pg, final int timeDuration) throws IOException{

		try {
			Calendar cal = null;
			IntervalData currentInterval = null;
			final int profileStatus = 0;
			if(dc.getRoot().getElements().length != 0){
				for(int i = 0; i < dc.getRoot().getElements().length; i++){

					if(dc.getRoot().getStructure(i).isOctetString(0)){
//						cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
						cal = new AXDRDateTime(new OctetString(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
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

					if(cal != null){
						currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
						if(currentInterval != null){
							pd.addInterval(currentInterval);
						}
					}
				}
			} else {
				getEDevice().getLogger().info("No entries in LoadProfile");
			}
		} catch (final ClassCastException e) {
			e.printStackTrace();
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
	private IntervalData getIntervalData(final DataStructure ds, final Calendar cal, final int status, final ProfileGeneric pg, final List channelInfos)throws IOException{

		final IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		int index = 0;

		try {
            List<CapturedObject> captureObjects = pg.getCaptureObjects();
            for(int i = 0; i < captureObjects.size(); i++){
				if(index < channelInfos.size()){
                    CapturedObject capturedObject = captureObjects.get(i);
                    ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
                    if (com.energyict.dlms.ParseUtils.isElectricityObisCode(obisCode) || isValidChannelObisCode(obisCode)) {
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

    /**
     * 
     * @param pg
     * @return
     * @throws IOException
     */
    private int getProfileClockChannelIndex(final ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
                CapturedObject capturedObject = pg.getCaptureObjects().get(i);
                ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
                ObisCode clockObisCode = getMeterConfig().getClockObject().getObisCode();
                if(isSameCorrectedObisCode(obisCode, clockObisCode)){
					return i;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
		}
		return -1;
	}

    /**
     * Getetr for the CosemObjectFactory
     * @return
     */
    protected CosemObjectFactory getCosemObjectFactory(){
		return getEDevice().getCosemObjectFactory();
	}

    /**
     * Geth the EiServer RTU
     * @return
     */
    private Rtu getMeter(){
		return getEDevice().getMeter();
	}

    /**
     * Getter for the toCalendar from the EDevice
     * @return
     */
    private Calendar getToCalendar(){
		return getEDevice().getToCalendar();
	}

    /**
     * Getter for the fromCalendar from the EDevice
     * @param channel
     * @return
     */
    private Calendar getFromCalendar(final Channel channel){
		return getEDevice().getFromCalendar(channel);
	}

    /**
     * Getter for the DLMSMeterConfig
     * @return
     */
    private DLMSMeterConfig getMeterConfig(){
		return getEDevice().getMeterConfig();
	}

    /**
     * Getter for the timeZone
     * @return
     */
    private TimeZone getTimeZone(){
		return getEDevice().getTimeZone();
	}

    /**
     * Change the B-field of the obisCode to select the correct physical address
     * @param obisCode
     * @return
     */
    protected ObisCode getCorrectedObisCode(ObisCode obisCode) {
        return ProtocolTools.setObisCodeField(obisCode, 1, (byte) getEDevice().getPhysicalAddress());
    }

    /**
     * Compare two obisCodes, but use the corrected B-field with the physical address
     * @param obisCode1
     * @param obisCode2
     * @return
     */
    private boolean isSameCorrectedObisCode(ObisCode obisCode1, ObisCode obisCode2) {
        return getCorrectedObisCode(obisCode1).equals(getCorrectedObisCode(obisCode2));
    }

}
