package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.webrtuz3.WebRTUZ3;
import com.energyict.genericprotocolimpl.webrtuz3.eventhandling.DisconnectControlLog;
import com.energyict.genericprotocolimpl.webrtuz3.eventhandling.EventsLog;
import com.energyict.genericprotocolimpl.webrtuz3.eventhandling.FraudDetectionLog;
import com.energyict.genericprotocolimpl.webrtuz3.eventhandling.MbusLog;
import com.energyict.genericprotocolimpl.webrtuz3.eventhandling.PowerFailureLog;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

/**
 * 
 * @author gna
 */
public class EventProfile {

	private WebRTUZ3 webrtu;
	
	public EventProfile(WebRTUZ3 webrtu){
		this.webrtu = webrtu;
	}
	
	public ProfileData getEvents() throws IOException{
		
		ProfileData profileData = new ProfileData( );
		
		Date lastLogReading = webrtu.getMeter().getLastLogbook();
		if(lastLogReading == null){
			lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(webrtu.getMeter());
		}
		Calendar fromCal = ProtocolUtils.getCleanCalendar(this.webrtu.getTimeZone());
		fromCal.setTime(lastLogReading);
		webrtu.getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + webrtu.getSerialNumber() + ".");
		DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		DataContainer dcControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getControlLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		DataContainer dcMbusEventLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusEventLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		
		EventsLog standardEvents = new EventsLog(dcEvent); 
		FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(dcFraudDetection);
		DisconnectControlLog disconnectControl = new DisconnectControlLog(dcControlLog);
		MbusLog mbusLogs = new MbusLog(dcMbusEventLog);
		PowerFailureLog powerFailure = new PowerFailureLog(dcPowerFailure);
		
		profileData.getMeterEvents().addAll(standardEvents.getMeterEvents());
		profileData.getMeterEvents().addAll(fraudDetectionEvents.getMeterEvents());
		profileData.getMeterEvents().addAll(disconnectControl.getMeterEvents());
		profileData.getMeterEvents().addAll(mbusLogs.getMeterEvents());
		profileData.getMeterEvents().addAll(powerFailure.getMeterEvents());
		
		// Don't create statusbits from the events
//			profileData.applyEvents(webrtu.getMeter().getIntervalInSeconds()/60);
//		webrtu.getStoreObject().add(profileData, getMeter());
		return profileData;
	}
	
	private CosemObjectFactory getCosemObjectFactory(){
		return this.webrtu.getCosemObjectFactory();
	}
	
	private DLMSMeterConfig getMeterConfig(){
		return this.webrtu.getMeterConfig();
	}
}
