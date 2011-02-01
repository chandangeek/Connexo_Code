package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;
import java.util.*;

import com.energyict.cbo.BaseUnit;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.*;
import com.energyict.protocolimpl.coronis.waveflowDLMS.a1800.ProfileDataReader;

public class A1800 extends AbstractDLMS {


	public static final ObisCode LOAD_PROFILE_PULSES = ObisCode.fromString("1.0.99.1.0.255");
	public static final ObisCode LOAD_PROFILE_ENG_CUMM = ObisCode.fromString("1.0.99.1.1.255");
	public static final ObisCode LOAD_PROFILE_ENG_ADV = ObisCode.fromString("1.0.99.1.2.255");
	
	public static final ObisCode LOG_PROFILE = ObisCode.fromString("1.1.99.98.0.255");
	public static final ObisCode OBJECT_LIST = ObisCode.fromString("0.0.40.0.0.255");
	static {
		objectEntries.put(ObisCode.fromString("1.0.96.1.0.255"),new ObjectEntry("Meter serial number",1));
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
		
		objectEntries.put(ObisCode.fromString("1.1.81.7.40.255"),new ObjectEntry("Phase angle 1 instantaneous current",3));
		objectEntries.put(ObisCode.fromString("1.1.81.7.50.255"),new ObjectEntry("Phase angle 2 instantaneous current",3));
		objectEntries.put(ObisCode.fromString("1.1.81.7.60.255"),new ObjectEntry("Phase angle 3 instantaneous current",3));
		
		objectEntries.put(ObisCode.fromString("1.1.130.7.0.255"),new ObjectEntry("Apparent power vectorial instantaneous current",3));
		objectEntries.put(ObisCode.fromString("1.1.142.7.0.255"),new ObjectEntry("Apparent power arithmetic instantaneous current",3));
		objectEntries.put(ObisCode.fromString("1.1.128.7.0.255"),new ObjectEntry("Import power instantaneous value",3));
		
		objectEntries.put(ObisCode.fromString("1.1.13.7.0.255"),new ObjectEntry("Instantaneous value of Aggregate power factor vectorial calculation",3));
		objectEntries.put(ObisCode.fromString("1.1.143.7.0.255"),new ObjectEntry("Instantaneous value of Aggregate power factor arithmetic calculation",3));
		
		objectEntries.put(ObisCode.fromString("1.1.0.4.2.255"),new ObjectEntry("CT ratio",1));
		objectEntries.put(ObisCode.fromString("1.1.0.4.3.255"),new ObjectEntry("VT ratio",1));
		
		objectEntries.put(ObisCode.fromString("0.0.97.97.0.255"),new ObjectEntry("Fatal alarm status (Diagnostic)",1));
		objectEntries.put(ObisCode.fromString("0.0.97.97.1.255"),new ObjectEntry("Non fatal alarm status 1 (Diagnostic)",1));
		objectEntries.put(ObisCode.fromString("0.0.97.97.2.255"),new ObjectEntry("Non fatal alarm status 2 (Diagnostic)",1));
		objectEntries.put(ObisCode.fromString("0.0.97.97.3.255"),new ObjectEntry("Non fatal alarm status 3 (Diagnostic)",1));
		objectEntries.put(ObisCode.fromString("0.0.97.97.4.255"),new ObjectEntry("Non fatal alarm status 4 (Diagnostic)",1));
		objectEntries.put(ObisCode.fromString("0.0.97.97.5.255"),new ObjectEntry("Non fatal alarm status 5 (Diagnostic)",1));
		
		objectEntries.put(ObisCode.fromString("1.1.1.6.0.255"),new ObjectEntry("Import active maximum demand (current billing period)",4));
		objectEntries.put(ObisCode.fromString("1.1.1.6.1.255"),new ObjectEntry("Import active maximum demand rate 1 (current billing period)",4));
		objectEntries.put(ObisCode.fromString("1.1.1.6.2.255"),new ObjectEntry("Import active maximum demand rate 2 (current billing period)",4));
		objectEntries.put(ObisCode.fromString("1.1.1.6.3.255"),new ObjectEntry("Import active maximum demand rate 3 (current billing period)",4));
		objectEntries.put(ObisCode.fromString("1.1.1.6.4.255"),new ObjectEntry("Import active maximum demand rate 4 (current billing period)",4));

		objectEntries.put(ObisCode.fromString("0.0.98.1.0.255"),new ObjectEntry("Historical registers self reads total",7));
		objectEntries.put(ObisCode.fromString("0.0.98.1.1.255"),new ObjectEntry("Historical registers self reads total rate 1",7));
		objectEntries.put(ObisCode.fromString("0.0.98.1.2.255"),new ObjectEntry("Historical registers self reads total rate 2",7));
		objectEntries.put(ObisCode.fromString("0.0.98.1.3.255"),new ObjectEntry("Historical registers self reads total rate 3",7));
		objectEntries.put(ObisCode.fromString("0.0.98.1.4.255"),new ObjectEntry("Historical registers self reads total rate 4",7));
		
		objectEntries.put(ObisCode.fromString("0.0.98.2.0.255"),new ObjectEntry("Historical registers previous billing total",7));
		objectEntries.put(ObisCode.fromString("0.0.98.2.1.255"),new ObjectEntry("Historical registers previous billing total rate 1",7));
		objectEntries.put(ObisCode.fromString("0.0.98.2.2.255"),new ObjectEntry("Historical registers previous billing total rate 2",7));
		objectEntries.put(ObisCode.fromString("0.0.98.2.3.255"),new ObjectEntry("Historical registers previous billing total rate 3",7));
		objectEntries.put(ObisCode.fromString("0.0.98.2.4.255"),new ObjectEntry("Historical registers previous billing total rate 4",7));
		
		objectEntries.put(ObisCode.fromString("0.1.10.0.1.255"),new ObjectEntry("Demand reset action",9));
		
		objectEntries.put(ObisCode.fromString("1.1.96.132.2.255"),new ObjectEntry("Scale factor",1));
		objectEntries.put(ObisCode.fromString("1.1.96.132.1.255"),new ObjectEntry("Multiplier",1));
		
		objectEntries.put(LOG_PROFILE,new ObjectEntry("Logbook",7));
		objectEntries.put(LOAD_PROFILE_PULSES,new ObjectEntry("Load profile puls values",7));
		objectEntries.put(LOAD_PROFILE_ENG_CUMM,new ObjectEntry("Load profile cumulative engineering values",7));
		objectEntries.put(LOAD_PROFILE_ENG_ADV,new ObjectEntry("Load profile advance engineering values",7));
		objectEntries.put(OBJECT_LIST,new ObjectEntry("Object list",15));
		
	};

	
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
		setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", LOAD_PROFILE_PULSES.toString())));		
	}
}
