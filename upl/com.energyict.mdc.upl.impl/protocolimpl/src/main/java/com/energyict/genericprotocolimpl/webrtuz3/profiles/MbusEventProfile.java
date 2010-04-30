package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.webrtuz3.MbusDevice;
import com.energyict.genericprotocolimpl.webrtuz3.eventhandling.MbusControlLog;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * 
 * @author gna
 */

public class MbusEventProfile {
	
        private static final ObisCode EVENT_LOG_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");

	private MbusDevice mbusDevice;

	public MbusEventProfile(MbusDevice mbusDevice) {
		this.mbusDevice = mbusDevice;
	}

	public ProfileData getEvents() throws IOException{
		
        ProfileData profileData = new ProfileData();

        try {
            Date lastLogReading = getMeter().getLastLogbook();
            if (lastLogReading == null) {
                lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(getMeter());
            }
            Calendar fromCal = ProtocolUtils.getCleanCalendar(getTimeZone());
            fromCal.setTime(lastLogReading);
            mbusDevice.getLogger().log(Level.INFO, "Reading EVENTS from Mbus meter with serialnumber " + mbusDevice.getCustomerID() + ".");

            ProfileGeneric eventProfile = getCosemObjectFactory().getProfileGeneric(getCorrectedObisCode(EVENT_LOG_OBISCODE));
            DataContainer mbusLog = eventProfile.getBuffer(fromCal, mbusDevice.getWebRTU().getToCalendar());

            MbusControlLog mbusControlLog = new MbusControlLog(mbusLog);
            profileData.getMeterEvents().addAll(mbusControlLog.getMeterEvents());

//		mbusDevice.getWebRTU().getStoreObject().add(profileData, getMeter());

        } catch (IOException e) {
            e.printStackTrace();
            mbusDevice.getLogger().log(Level.INFO, "An error occured while reading the EVENTS from Mbus meter with serialnumber " + mbusDevice.getCustomerID() + ": " + e.getMessage());
        }

        return profileData;
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

    private ObisCode getCorrectedObisCode(ObisCode obisCode) {
        ObisCode oc = ProtocolTools.setObisCodeField(obisCode, 1, (byte) this.mbusDevice.getPhysicalAddress());
        System.out.println("Changed " + obisCode + " to " + oc);
        return oc;
    }
}
