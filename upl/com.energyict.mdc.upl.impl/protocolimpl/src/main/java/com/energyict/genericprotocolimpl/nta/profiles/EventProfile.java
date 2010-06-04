package com.energyict.genericprotocolimpl.nta.profiles;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.genericprotocolimpl.nta.eventhandling.*;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * 
 * @author gna
 */
public class EventProfile {

	protected AbstractNTAProtocol webrtu;
	
	public EventProfile(AbstractNTAProtocol webrtu){
		this.webrtu = webrtu;
	}
	
	public void getEvents() throws IOException{
		
		ProfileData profileData = new ProfileData( );
		
		Date lastLogReading = webrtu.getMeter().getLastLogbook();
		if(lastLogReading == null){
			lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(webrtu.getMeter());
		}
		Calendar fromCal = ProtocolUtils.getCleanCalendar(getTimeZone());
		fromCal.setTime(lastLogReading);
		webrtu.getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + webrtu.getSerialNumber() + ".");
		DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		DataContainer dcControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getControlLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		DataContainer dcMbusEventLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusEventLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
		
		EventsLog standardEvents = new EventsLog(getTimeZone(), dcEvent); 
		FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(getTimeZone(), dcFraudDetection);
		DisconnectControlLog disconnectControl = new DisconnectControlLog(getTimeZone(), dcControlLog);
		MbusLog mbusLogs = new MbusLog(getTimeZone(), dcMbusEventLog);
		PowerFailureLog powerFailure = new PowerFailureLog(getTimeZone(), dcPowerFailure);
		
		profileData.getMeterEvents().addAll(standardEvents.getMeterEvents());
		profileData.getMeterEvents().addAll(fraudDetectionEvents.getMeterEvents());
		profileData.getMeterEvents().addAll(disconnectControl.getMeterEvents());
		profileData.getMeterEvents().addAll(mbusLogs.getMeterEvents());
		profileData.getMeterEvents().addAll(powerFailure.getMeterEvents());
		
		// Don't create statusbits from the events
//			profileData.applyEvents(webrtu.getMeter().getIntervalInSeconds()/60);
		webrtu.getStoreObject().add(profileData, getMeter());
	}
	
	protected Rtu getMeter(){
		return this.webrtu.getMeter();
	}
	
	protected CosemObjectFactory getCosemObjectFactory(){
		return this.webrtu.getCosemObjectFactory();
	}
	
	protected DLMSMeterConfig getMeterConfig(){
		return this.webrtu.getMeterConfig();
	}

	protected TimeZone getTimeZone() throws IOException{
//		return this.webrtu.getTimeZone();
		return this.webrtu.getMeterTimeZone();
	}
}
