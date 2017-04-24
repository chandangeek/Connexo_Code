/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Functional implementation of a GasMeter
 *
 * @author gna
 * @since 19-mrt-2010
 *
 */
public class GMeter {

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
	 */
	@SuppressWarnings("unchecked")
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
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
		profileData.setIntervalDatas(profileBuilder.buildIntervalData(dc));

        if (includeEvents) {

        	ProfileGeneric pgEvents = getMbusEventProfile();
        	DataContainer dcEvents = pgEvents.getBuffer(fromCalendar, toCalendar);
        	if(dcEvents.getRoot() != null){
        		GMetersLog gLog = new GMetersLog(dcEvents);
        		profileData.getMeterEvents().addAll(gLog.getMeterEvents());
        	}
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
	 * Read the number of channels from the current Gas device
	 *
	 * @return the number of channels
	 * @throws IOException if an communication error occurs
	 */
	public int getNrOfChannels() throws IOException {
		int channelCount = 0;
		List<CapturedObject> capturedObjects = getMbusProfile().getCaptureObjects();
		for (CapturedObject capturedObject : capturedObjects) {
			switch (DLMSClassId.findById(capturedObject.getClassId())) {
				case REGISTER:
				case EXTENDED_REGISTER:
                    if(capturedObject.getAttributeIndex() == 2){
					channelCount++;
                    }
					break;
				default:
					break;
			}
		}
		return channelCount;
	}

	/**
	 * The MBus his Event profile
	 *
	 * @return the event profile object
	 *
	 * @throws IOException if the object didn't exist in the objectList
	 */
	public ProfileGeneric getMbusEventProfile() throws IOException {
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
