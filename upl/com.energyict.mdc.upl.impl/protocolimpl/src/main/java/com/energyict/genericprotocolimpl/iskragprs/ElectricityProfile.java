package com.energyict.genericprotocolimpl.iskragprs;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ScalerUnit;
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
import com.energyict.protocol.ProtocolUtils;

/**
 * 
 * @author gna
 * Changes:
 * GNA |29012009| When the ScalerUnit is (0, 0) then return a Unitless scalerUnit, otherwise you get errors.
 */
public class ElectricityProfile {
	
	private IskraMx37x iskramx37x;
	
	public ElectricityProfile(){
	}
	
	public ElectricityProfile(final IskraMx37x iskra){
		this.iskramx37x = iskra;
	}
	
	public void getProfile(final ObisCode electricityProfile, final boolean events) throws IOException, SQLException, BusinessException{
		final ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;
		try {
			genericProfile = getCosemObjectFactory().getProfileGeneric(electricityProfile);
			final List<ChannelInfo> channelInfos = getChannelInfos(genericProfile);
			
			profileData.setChannelInfos(channelInfos);
			Calendar fromCalendar = null;
			Calendar channelCalendar = null;
			final Calendar toCalendar = getToCalendar();
			
			for (int i = 0; i < getMeter().getChannels().size(); i++) {
				final Channel chn = getMeter().getChannel(i);
				if(!(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS) && 
						!(chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS)){
					channelCalendar = getFromCalendar(getMeter().getChannel(i));
					if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
						fromCalendar = channelCalendar;
					}
				}
			}
			
			iskramx37x.getLogger().log(Level.INFO, "Meter " + this.iskramx37x.getMeter().getSerialNumber() + ": Retrieving profiledata from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
			final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
			buildProfileData(dc, profileData, genericProfile);
			profileData.sort();
			
			if(events){
				Date lastLogReading = iskramx37x.getMeter().getLastLogbook();
				if(lastLogReading == null){
					lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(iskramx37x.getMeter());
				}
				final Calendar fromCal = ProtocolUtils.getCleanCalendar(getTimeZone());
				fromCal.setTime(lastLogReading);
				iskramx37x.getLogger().log(Level.INFO, "Meter " + this.iskramx37x.getMeter().getSerialNumber() + ": Reading EVENTS from meter with serialnumber " + iskramx37x.getSerialNumber() + ".");
				final DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCal, iskramx37x.getToCalendar());
				final Logbook logbook = new Logbook(getTimeZone(), iskramx37x.getLogger());
				profileData.getMeterEvents().addAll(logbook.getMeterEvents(dcEvent));
				profileData.applyEvents(iskramx37x.getMeter().getIntervalInSeconds()/60);
			}
			
			getMeter().store(profileData);
			
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (final SQLException e){
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} catch (final BusinessException e){
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}
	}

	private List<ChannelInfo> getChannelInfos(final ProfileGeneric profile) throws IOException {
		final List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		ChannelInfo ci = null;
		int index = 0;
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){
				
				if(ParseUtils.isElectricityObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode()) 
						&& !isProfileStatusObisCode(((CapturedObject)(profile.getCaptureObjects().get(i))).getLogicalName().getObisCode())){ // make a channel out of it
					final CapturedObject co = ((CapturedObject)profile.getCaptureObjects().get(i));
					final ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());
					if(su != null){
						ci = new ChannelInfo(index, getProfileChannelNumber(index+1), "IskraMx372_"+index, su.getUnit());
					} else {
						ci = new ChannelInfo(index, getProfileChannelNumber(index+1), "IskraMx372_"+index, Unit.get(BaseUnit.UNITLESS));
					}
					
					index++;
					if(ParseUtils.isObisCodeCumulative(co.getLogicalName().getObisCode())){
						//TODO need to check the wrapValue
						ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
					}
					channelInfos.add(ci);
				}
				
			}
		} catch (final IOException e) {
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
	private ScalerUnit getMeterDemandRegisterScalerUnit(final ObisCode oc) throws IOException{
		try {
			ScalerUnit su = getCosemObjectFactory().getCosemObject(oc).getScalerUnit();
			if( su.getUnitCode() == 0){
				su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
			}
			return su;
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException("Could not get the scalerunit from object '" + oc + "'.");
		}
	}
	
	private int getProfileChannelNumber(final int index){
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
	
	private void buildProfileData(final DataContainer dc, final ProfileData pd, final ProfileGeneric pg) throws IOException{
		
		//TODO check how this reacts with the profile.
		
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){
		
			for(int i = 0; i < dc.getRoot().getElements().length; i++){
				if(dc.getRoot().getStructure(i).isOctetString(0)){
					cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
				} else {
					if(cal != null){
						cal.add(Calendar.SECOND, iskramx37x.getMeter().getIntervalInSeconds());
					}
				}
				if(cal != null){		
					
					if(getProfileStatusChannelIndex(pg) != -1){
						profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
					} else {
						profileStatus = 0;
					}
					
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
	
	private boolean isProfileStatusObisCode(final ObisCode oc) throws IOException{
		return oc.equals(getMeterConfig().getStatusObject().getObisCode());
	}
	
	private IntervalData getIntervalData(final DataStructure ds, final Calendar cal, final int status, final ProfileGeneric pg)throws IOException{
		
		final IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
		
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(ParseUtils.isElectricityObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())
						&& !isProfileStatusObisCode(((CapturedObject)(pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())){
					id.addValue(new Integer(ds.getInteger(i)));
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
		}
		
		return id;
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
		return this.iskramx37x.getCosemObjectFactory();
	}
	
	private Rtu getMeter(){
		return this.iskramx37x.getMeter();
	}
	
	private Calendar getToCalendar(){
		return this.iskramx37x.getToCalendar();
	}
	
	private Calendar getFromCalendar(final Channel channel){
		return this.iskramx37x.getFromCalendar(channel);
	}
	
	private DLMSMeterConfig getMeterConfig(){
		return this.iskramx37x.getMeterConfig();
	}
	
	private TimeZone getTimeZone(){
		return this.iskramx37x.getTimeZone();
	}
}
