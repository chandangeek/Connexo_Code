package com.energyict.protocolimpl.dlms.as220.gmeter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

public class GMeter {

	/** TODO to set correct*/
//	private static final ObisCode GAS_PROFILE_OBISCODE = ObisCode.fromString("0.2.24.3.0.255");
	private static final int SEC_PER_MIN = 60;
	
	private final GasValveController	gasValveController;
	private final AS220 				as220;
	private final GasInstallController 	gasInstallController;

	/**
	 * Default Constructor
	 * @param as220
	 */
	public GMeter(AS220 as220) {
		this.gasValveController = new GasValveController(as220);
		this.gasInstallController = new GasInstallController(as220);
		this.as220 = as220;
	}

	/**
	 * Getter for the {@link GasValveController}
	 * @return the gasValveController
	 */
	public GasValveController getGasValveController() {
		return gasValveController;
	}
	
	public AS220 getAs220() {
		return as220;
	}

	/**
     * Read the profile data from the MbusDevice
     *
     * @param from
     * @param to
     * @param includeEvents
     * @return the {@link ProfileData}
	 * @throws IOException 
	 * @throws UnsupportedException 
     * @throws IOException
	 */
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws UnsupportedException, IOException {
		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getAs220().getTimeZone());
		Calendar toCalendar = ProtocolUtils.getCleanCalendar(getAs220().getTimeZone());
		fromCalendar.setTime(from);
		toCalendar.setTime(to);
		ProfileData profileData = new ProfileData();
		ProfileGeneric pg = getMbusProfile();
		CapturedObjectsHelper coh = pg.getCaptureObjectsHelper();
		GProfileBuilder profileBuilder = new GProfileBuilder((GasDevice) getAs220(), coh);
		ScalerUnit[] scalerunit = profileBuilder.buildScalerUnits();
		
		List<ChannelInfo> channelInfos = profileBuilder.buildChannelInfos(scalerunit);
		profileData.setChannelInfos(channelInfos);
		DataContainer dc = pg.getBuffer(fromCalendar, toCalendar);
		profileData.setIntervalDatas(profileBuilder.buildIntervalData(scalerunit, dc));

        if (includeEvents) {
        	
        	ProfileGeneric pgEvents = GetMbusEventProfile();
        	DataContainer dcEvents = pgEvents.getBuffer(fromCalendar, toCalendar);
        	GMetersLog gLog = new GMetersLog(dcEvents);
        	profileData.getMeterEvents().addAll(gLog.getMeterEvents());
        }

        profileData.sort();
        return profileData;
	}
	
	/**
	 * Getter for the {@link GasInstallController}
	 * @return the gasInstallController
	 */
	public GasInstallController getGasInstallController() {
		return gasInstallController;
	}

	/**
	 * The MBus his {@link ProfileGeneric} object
	 * 
	 * @return the ProfileGeneric Object
	 * 
	 * @throws IOException if the object didn't exist in the objectList
	 */
	public ProfileGeneric getMbusProfile() throws IOException {
		return getAs220().getCosemObjectFactory().getProfileGeneric(getGasDevice().getMeterConfig().getMbusProfile(getGasDevice().getPhysicalAddress()).getObisCode());
	}
	
	/**
	 * The MBus his Event profile
	 * 
	 * @return the event profile object
	 * 
	 * @throws IOException if the object didn't exist in the objectList
	 */
	public ProfileGeneric GetMbusEventProfile() throws IOException {
		return getAs220().getCosemObjectFactory().getProfileGeneric(getGasDevice().getMeterConfig().getMbusEventLogObject().getObisCode());
	}
	
	/**
	 * Getter for the {@link GasDevice}
	 * 
	 * @return the gasDevice
	 */
	private GasDevice getGasDevice(){
		return (GasDevice)getAs220();
	}

}
