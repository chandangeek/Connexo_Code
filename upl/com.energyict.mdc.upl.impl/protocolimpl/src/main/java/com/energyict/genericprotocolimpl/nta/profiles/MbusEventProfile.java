package com.energyict.genericprotocolimpl.nta.profiles;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.nta.eventhandling.MbusControlLog;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

/**
 * 
 * @author gna
 */

public class MbusEventProfile {
	
	private AbstractMbusDevice mbusDevice;
	
	public MbusEventProfile(AbstractMbusDevice mbusDevice) {
		this.mbusDevice = mbusDevice;
	}

	public void getEvents() throws IOException{
		
		ProfileData profileData = new ProfileData();
		
		Date lastLogReading = getMeter().getLastLogbook();
		if(lastLogReading == null){
			lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(getMeter());
		}
		Calendar fromCal = ProtocolUtils.getCleanCalendar(getTimeZone());
		fromCal.setTime(lastLogReading);
		mbusDevice.getLogger().log(Level.INFO, "Reading EVENTS from Mbus meter with serialnumber " + mbusDevice.getCustomerID() + ".");
		DataContainer mbusLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusControlLog(mbusDevice.getPhysicalAddress()).getObisCode()).getBuffer(fromCal, mbusDevice.getWebRTU().getToCalendar());
		
		MbusControlLog mbusControlLog = new MbusControlLog(getTimeZone(), mbusLog);
		profileData.getMeterEvents().addAll(mbusControlLog.getMeterEvents());
		
		mbusDevice.getWebRTU().getStoreObject().add(profileData, getMeter());
		
		// Don't create statusbits from the events
//		profileData.applyEvents(mbusDevice.getMbus().getIntervalInSeconds()/60);
	}
	
	private CosemObjectFactory getCosemObjectFactory(){
		return this.mbusDevice.getWebRTU().getCosemObjectFactory();
	}
	
	private Rtu getMeter(){
		return this.mbusDevice.getMbus();
	}
	
	private DLMSMeterConfig getMeterConfig(){
		return this.mbusDevice.getWebRTU().getMeterConfig();
	}
	
	private TimeZone getTimeZone(){
		return this.mbusDevice.getWebRTU().getTimeZone();
	}
}
