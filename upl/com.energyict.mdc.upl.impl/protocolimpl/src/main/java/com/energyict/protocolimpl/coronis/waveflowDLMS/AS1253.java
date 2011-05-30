package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.coronis.waveflowDLMS.as1253.ProfileDataReader;

import java.io.IOException;
import java.util.*;

public class AS1253 extends AbstractDLMS {

	/**
	 * Predefined obiscodes for the A1800 meter
	 */	
	public static final ObisCode LOAD_PROFILE_PULSE_VALUES = ObisCode.fromString("1.1.99.1.0.255");
	public static final ObisCode LOG_PROFILE = ObisCode.fromString("1.1.99.98.0.255");
	public static final ObisCode OBJECT_LIST = ObisCode.fromString("0.0.40.0.0.255");
	public static final ObisCode METER_SERIAL_NUMBER = ObisCode.fromString("1.1.96.1.0.255");
	
	private Map<ObisCode,ObjectEntry> objectEntries = null;
	
    /**
     * Override this method to request the load profile from the meter starting at lastreading until now.
     * @param lastReading request from
     * @param includeEvents enable or disable tht reading of meterevents
     * @throws java.io.IOException When something goes wrong
     * @return All load profile data in the meter from lastReading
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
    	ProfileDataReader profileDataReader = new ProfileDataReader(this);
    	return profileDataReader.getProfileData(lastReading, includeEvents);
    }	


    
	@Override
	void doTheValidateProperties(Properties properties) {
		setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", LOAD_PROFILE_PULSE_VALUES.toString())));		
	}



	@Override
	ObisCode getSerialNumberObisCodeForPairingRequest() {
		return METER_SERIAL_NUMBER;
	}



	@Override
	PairingMeterId getPairingMeterId() {
		return PairingMeterId.AS1253;
	}



	@Override
	Map<ObisCode, ObjectEntry> getObjectEntries() {
	
		/**
		 * Lazy initialize the map with objectEntries 
		 */
		if (objectEntries == null) {
			
			objectEntries = new HashMap();
		
			objectEntries.put(METER_SERIAL_NUMBER,new ObjectEntry("Meter serial number",1));
			objectEntries.put(ObisCode.fromString("1.1.1.8.0.255"),new ObjectEntry("Import active energy tarif 0",3));
			objectEntries.put(ObisCode.fromString("1.1.2.8.0.255"),new ObjectEntry("Export active energy tarif 0",3));
			objectEntries.put(ObisCode.fromString("1.1.3.8.0.255"),new ObjectEntry("Import reactive energy tarif 0",3));
			objectEntries.put(ObisCode.fromString("1.1.9.6.1.255"),new ObjectEntry("Apparent maximum 1",4));
			objectEntries.put(ObisCode.fromString("1.1.32.7.0.255"),new ObjectEntry("Phase 1 instantaneous voltage",3));
			objectEntries.put(ObisCode.fromString("1.1.52.7.0.255"),new ObjectEntry("Phase 2 instantaneous voltage",3));
			objectEntries.put(ObisCode.fromString("1.1.72.7.0.255"),new ObjectEntry("Phase 3 instantaneous voltage",3));
			objectEntries.put(ObisCode.fromString("1.1.31.7.0.255"),new ObjectEntry("Phase 1 instantaneous current",3));
			objectEntries.put(ObisCode.fromString("1.1.51.7.0.255"),new ObjectEntry("Phase 2 instantaneous current",3));
			objectEntries.put(ObisCode.fromString("1.1.71.7.0.255"),new ObjectEntry("Phase 3 instantaneous current",3));
			objectEntries.put(ObisCode.fromString("1.1.33.7.0.255"),new ObjectEntry("Power factor phase 1 instantaneous",3));
			objectEntries.put(ObisCode.fromString("1.1.53.7.0.255"),new ObjectEntry("Power factor phase 2 instantaneous",3));
			objectEntries.put(ObisCode.fromString("1.1.73.7.0.255"),new ObjectEntry("Power factor phase 3 instantaneous",3));
			objectEntries.put(ObisCode.fromString("1.1.34.7.0.255"),new ObjectEntry("Frequency phase 1 instantaneous",3));
			objectEntries.put(ObisCode.fromString("1.1.54.7.0.255"),new ObjectEntry("Frequency phase 2 instantaneous",3));
			objectEntries.put(ObisCode.fromString("1.1.74.7.0.255"),new ObjectEntry("Frequency phase 3 instantaneous",3));
			objectEntries.put(ObisCode.fromString("1.1.1.7.0.255"),new ObjectEntry("Import power instantaneous value",3));
			objectEntries.put(ObisCode.fromString("1.1.2.7.0.255"),new ObjectEntry("Export power instantaneous value",3));
			objectEntries.put(ObisCode.fromString("1.1.97.97.255.255"),new ObjectEntry("Fatal alarm status (Diagnostic)",1));
			objectEntries.put(ObisCode.fromString("1.1.97.97.1.255"),new ObjectEntry("Non fatal alarm status 1 (Diagnostic)",1));
			objectEntries.put(ObisCode.fromString("1.1.97.97.2.255"),new ObjectEntry("Non fatal alarm status 2 (Diagnostic)",1));
			objectEntries.put(ObisCode.fromString("1.1.97.97.3.255"),new ObjectEntry("Non fatal alarm status 3 (Diagnostic)",1));
			objectEntries.put(ObisCode.fromString("1.1.96.3.0.255"),new ObjectEntry("Contactor control (on-OFF)- for direct connected meters",1));
			
			objectEntries.put(ObisCode.fromString("1.1.96.56.255.255"),new ObjectEntry("total time of all pwr fails/ battery use time counter",1));
			objectEntries.put(ObisCode.fromString("1.1.0.9.2.255"),new ObjectEntry("Meter date",1));
			objectEntries.put(ObisCode.fromString("1.1.0.9.1.255"),new ObjectEntry("Meter time",1));
			objectEntries.put(ObisCode.fromString("1.1.0.0.0.255"),new ObjectEntry("Utility Id",1));
			
			objectEntries.put(LOG_PROFILE,new ObjectEntry("Logbook",7));
			objectEntries.put(LOAD_PROFILE_PULSE_VALUES,new ObjectEntry("Load profile non cumulative engineering values",7));
			objectEntries.put(OBJECT_LIST,new ObjectEntry("Object list",15));

		}
		return objectEntries;
	}	

    

	
}

