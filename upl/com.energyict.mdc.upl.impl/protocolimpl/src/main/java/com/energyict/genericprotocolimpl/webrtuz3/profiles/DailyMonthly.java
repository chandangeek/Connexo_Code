package com.energyict.genericprotocolimpl.webrtuz3.profiles;

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

import com.energyict.cbo.ApplicationException;
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
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;

public class DailyMonthly {

	private EDevice webrtu;

	public DailyMonthly(){
	}

	public DailyMonthly(final EDevice eDevice){
		this.webrtu = eDevice;
	}

	public ProfileData getDailyValues(final ObisCode dailyObisCode) throws IOException, SQLException, BusinessException {
		final ProfileData profileData = new ProfileData( );
		try {
			final ProfileGeneric genericProfile = getCosemObjectFactory().getProfileGeneric(dailyObisCode);
			final List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.DAYS);
			if(channelInfos.size() != 0){

				webrtu.getLogger().log(Level.INFO, "Getting daily values for meter with serialnumber: " + getMeter().getSerialNumber());

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
				webrtu.getLogger().log(Level.INFO, "Reading Daily values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
				final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile, TimeDuration.DAYS);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				final ProfileData pd = sortOutProfiledate(profileData, TimeDuration.DAYS);

				// We save the profileData to a tempObject so we can store everything at the end of the communication
//			webrtu.getStoreObject().add(getMeter(), pd);
//				webrtu.getStoreObject().add(pd, getMeter());
				return pd;
			}

		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		return profileData;
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
						ci = new ChannelInfo(index, channelIndex, "WebRtuKP_DayMonth_"+index, su.getUnit());
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

	public ProfileData getMonthlyValues(final ObisCode monthlyObisCode) throws IOException, SQLException, BusinessException {
		final ProfileData profileData = new ProfileData( );
		try {
			final ProfileGeneric genericProfile = getCosemObjectFactory().getProfileGeneric(monthlyObisCode);
			final List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.MONTHS);
			if(channelInfos.size() != 0){

				webrtu.getLogger().log(Level.INFO, "Getting monthly values for meter with serialnumber: " + getMeter().getSerialNumber());

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

				webrtu.getLogger().log(Level.INFO, "Reading Monthly values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
				final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile, TimeDuration.MONTHS);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				final ProfileData pd = sortOutProfiledate(profileData, TimeDuration.MONTHS);

				// We save the profileData to a tempObject so we can store everything at the end of the communication
//			webrtu.getStoreObject().add(getMeter(), pd);
//				if(webrtu.getMarkedAsBadTime()){
//					pd.markIntervalsAsBadTime();
//				}
//				webrtu.getStoreObject().add(pd, getMeter());
				return pd;
			}

		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		return profileData;
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
			if( su.getUnitCode() == 0){
				su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
			}
			return su;
		} catch (final IOException e) {
			e.printStackTrace();
			webrtu.getLogger().log(Level.INFO, "Could not get the scalerunit from object '" + oc + "'.");
		}
		return new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
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

	private int getDailyChannelNumber(final int index){
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

	private int getMonthlyChannelNumber(final int index){
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
				webrtu.getLogger().info("No entries in LoadProfile");
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
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(index < channelInfos.size()){
					if(com.energyict.dlms.ParseUtils.isElectricityObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode()) ||
							isValidChannelObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())){
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
	 * Check if it is a valid channel Obiscode
	 * TODO it is the same method as the one from the {@link ElectricityProfile}, maybe extract an abstract profile class for both ...
	 *
	 * @param obisCode
	 * 				- the {@link ObisCode} to check
	 *
	 * @return true if you know it is a valid channelData obisCode, false otherwise
	 */
	private boolean isValidChannelObisCode(final ObisCode obisCode){
		if ((obisCode.getA() == 1) && (((obisCode.getB() >= 0) && (obisCode.getB() <= 64)) || (obisCode.getB() == 128)) ) {	// Energy channels - Pulse channels (C == 82)
			return true;
		} else if(obisCode.getC() == 96){	// Temperature and Humidity
			if((obisCode.getA() == 0) && ((obisCode.getB() == 0) || (obisCode.getB() == 1)) && (obisCode.getD() == 9) && ((obisCode.getE() == 0) || (obisCode.getE() == 2))){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
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

	private TimeZone getTimeZone(){
		return this.webrtu.getTimeZone();
	}
}
