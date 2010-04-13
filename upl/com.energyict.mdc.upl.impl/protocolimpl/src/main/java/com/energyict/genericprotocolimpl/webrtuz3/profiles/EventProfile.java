package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.webrtuz3.eventhandling.EventsLog;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 *
 * @author gna
 */
public class EventProfile {

	private EDevice eDevice;

	public EventProfile(EDevice webrtu){
		this.eDevice = webrtu;
	}

	public ProfileData getEvents() throws IOException{

		try {

		ProfileData profileData = new ProfileData( );

		Date lastLogReading = eDevice.getMeter().getLastLogbook();
		if(lastLogReading == null){
			lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(eDevice.getMeter());
		}
		Calendar fromCal = ProtocolUtils.getCleanCalendar(this.eDevice.getTimeZone());
		fromCal.setTime(lastLogReading);
		eDevice.getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + eDevice.getSerialNumber() + ".");

		ObisCode eventLogObisCode = ProtocolTools.setObisCodeField(ObisCode.fromString("0.0.99.98.0.255"), 1, (byte) eDevice.getPhysicalAddress());
//		ObisCode controlLogbookObisCode = ProtocolTools.setObisCodeField(ObisCode.fromString("0.0.96.3.10.255"), 1, (byte) eDevice.getPhysicalAddress());
//		ObisCode powerFailureObisCode = ProtocolTools.setObisCodeField(ObisCode.fromString("0.0.99.1.0.255"), 1, (byte) eDevice.getPhysicalAddress());
//		ObisCode fraudeDetectObisCode = ProtocolTools.setObisCodeField(ObisCode.fromString("0.0.99.2.0.255"), 1, (byte) eDevice.getPhysicalAddress());
//		ObisCode mbusLogsObisCode = ProtocolTools.setObisCodeField(ObisCode.fromString("0.0.99.3.0.255"), 1, (byte) eDevice.getPhysicalAddress());

		EventsLog standardEvents = new EventsLog(getLogAsDataContainer(eventLogObisCode, fromCal));
//		FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(getLogAsDataContainer(fraudeDetectObisCode, fromCal));
//		DisconnectControlLog disconnectControl = new DisconnectControlLog(getLogAsDataContainer(controlLogbookObisCode, fromCal));
//		MbusLog mbusLogs = new MbusLog(getLogAsDataContainer(mbusLogsObisCode, fromCal));
//		PowerFailureLog powerFailure = new PowerFailureLog(getLogAsDataContainer(powerFailureObisCode, fromCal));

		profileData.getMeterEvents().addAll(standardEvents.getMeterEvents());
//		profileData.getMeterEvents().addAll(fraudDetectionEvents.getMeterEvents());
//		profileData.getMeterEvents().addAll(disconnectControl.getMeterEvents());
//		profileData.getMeterEvents().addAll(mbusLogs.getMeterEvents());
//		profileData.getMeterEvents().addAll(powerFailure.getMeterEvents());

		// Don't create statusbits from the events
//			profileData.applyEvents(webrtu.getMeter().getIntervalInSeconds()/60);
//		webrtu.getStoreObject().add(profileData, getMeter());
		return profileData;

		} catch (Exception e) {
			e.printStackTrace();
			throw new NestedIOException(e);
		}

	}

	private DataContainer getLogAsDataContainer(ObisCode obisCode, Calendar from) throws IOException {
		return getCosemObjectFactory().getProfileGeneric(obisCode).getBuffer(from, eDevice.getToCalendar());
	}

	private CosemObjectFactory getCosemObjectFactory(){
		return this.eDevice.getCosemObjectFactory();
	}

}
