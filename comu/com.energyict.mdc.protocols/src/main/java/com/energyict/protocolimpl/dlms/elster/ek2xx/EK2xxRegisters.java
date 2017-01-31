/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.elster.ek2xx;

import com.energyict.mdc.common.ObisCode;

import java.util.HashMap;
import java.util.Map;

public class EK2xxRegisters {

	private static final String CLOCK_OBJECT 			= "CLOCK_OBJECT [8]";
	private static final String HDLC_SETUP_OBJECT 		= "HDLC_SETUP_OBJECT [23]";
	private static final String ASSOCIATION_OBJECT 		= "ASSOCIATION_OBJECT [15]";
	private static final String SAP_ASSIGNMENT_OBJECT 	= "SAP_ASSIGNMENT_OBJECT [17]";
	private static final String DATA_OBJECT 			= "DATA_OBJECT [1]";
	private static final String REGISTER_OBJECT 		= "REGISTER_OBJECT [3]";
	private static final String PROFILE_OBJECT 			= "PROFILE_OBJECT [7]";
	private static final String UNKNOWN_OBJECT			= "UNKNOWN_OBJECT";

	public static final ObisCode PROFILE_INTERVAL 		= ObisCode.fromString("7.0.0.8.1.255");
	public static final ObisCode PROFILE				= ObisCode.fromString("7.0.99.1.128.255");
	public static final ObisCode LOG					= ObisCode.fromString("7.0.99.99.0.255");
	public static final ObisCode CLOCK					= ObisCode.fromString("0.0.1.0.0.255");
	public static final ObisCode SOFTWARE_VERSION		= ObisCode.fromString("7.0.0.2.2.255");

	private Map objectInfo = new HashMap(0);
	private Map objectTypes = new HashMap(0);

	/*
	 * Constructors
	 */

	public EK2xxRegisters() {

		addName(CLOCK, " Clock object #1");
		addName("0.0.22.0.0.255", " IEC HDLC setup");
		addName("0.0.40.0.0.255", " Current association");
		addName("0.0.41.0.0.255", " SAP Assignment");
		addName("0.0.42.0.0.255", " COSEM logical device name");
		addName("0.0.96.1.0.255", " Device ID 1, Manufacturing number");
		addName("0.0.96.1.10.255", " Metering point ID");
		addName(SOFTWARE_VERSION, " Software version ID");
		addName("7.0.0.2.11.255", " Pressure sensor, serial no");
		addName("7.0.0.2.12.255", " Temperature sensor, serial no");
		addName(PROFILE_INTERVAL, " Recording interval 1 for profile");
		addName("7.0.13.0.0.255", " Ch. 0 Fwd abs conv volume Act value at meas conditions");
		addName("7.0.13.2.0.255", " Ch. 0 Fwd abs conv volume Value at base conditions");
		addName("7.0.43.0.0.255", " Ch. 0 Fwd logger conv volume Act value at meas conditions");
		addName("7.0.43.2.0.255", " Ch. 0 Fwd logger conv volume Value at base conditions");
		addName("7.0.41.0.0.255", " Current temperature, T");
		addName("7.0.42.0.0.255", " Current pressure, P");
		addName("7.0.53.0.0.255", " Compressibility, Z");
		addName("7.0.97.97.0.255", " Error object #1");
		addName(PROFILE, " Manufacturer specific");
		addName(LOG, " Certification data log #1");
		addName("7.1.0.0.1.255", "Gas ID #2");

		addType(CLOCK, CLOCK_OBJECT);
		addType("0.0.22.0.0.255", HDLC_SETUP_OBJECT);
		addType("0.0.40.0.0.255", ASSOCIATION_OBJECT);
		addType("0.0.41.0.0.255", SAP_ASSIGNMENT_OBJECT);
		addType("0.0.42.0.0.255", DATA_OBJECT);
		addType("0.0.96.1.0.255", DATA_OBJECT);
		addType("0.0.96.1.10.255", DATA_OBJECT);
		addType(SOFTWARE_VERSION, DATA_OBJECT);
		addType("7.0.0.2.11.255", DATA_OBJECT);
		addType("7.0.0.2.12.255", DATA_OBJECT);
		addType(PROFILE_INTERVAL, DATA_OBJECT);
		addType("7.0.13.0.0.255", REGISTER_OBJECT);
		addType("7.0.13.2.0.255", REGISTER_OBJECT);
		addType("7.0.43.0.0.255", REGISTER_OBJECT);
		addType("7.0.43.2.0.255", REGISTER_OBJECT);
		addType("7.0.41.0.0.255", REGISTER_OBJECT);
		addType("7.0.42.0.0.255", REGISTER_OBJECT);
		addType("7.0.53.0.0.255", REGISTER_OBJECT);
		addType("7.0.97.97.0.255", REGISTER_OBJECT);
		addType(PROFILE, PROFILE_OBJECT);
		addType(LOG, PROFILE_OBJECT);
		addType("7.1.0.0.1.255", DATA_OBJECT);

	}

	/*
	 * Private getters, setters and methods
	 */

	private void addName(String obisString, String description) {
		this.objectInfo.put(ObisCode.fromString(obisString), description);
	}

	private void addName(ObisCode obisCode, String description) {
		this.objectInfo.put(obisCode, description);
	}

	private void addType(String obisString, String type) {
		this.objectTypes.put(ObisCode.fromString(obisString), type);
	}

	private void addType(ObisCode obisCode, String type) {
		this.objectTypes.put(obisCode, type);
	}

	/*
	 * Public methods
	 */

	// TODO Auto-generated Public methods stub

	/*
	 * Public getters and setters
	 */

	public String getObjectName(ObisCode obisCode) {
		String infoMessage = (String) this.objectInfo.get(obisCode);
		if (infoMessage == null) {
			infoMessage = "No object description found.";
		}
		return infoMessage;
	}

	public String getObjectType(ObisCode obisCode) {
		String typeMessage = (String) this.objectTypes.get(obisCode);
		if (typeMessage == null) {
			typeMessage = UNKNOWN_OBJECT;
		}
		return typeMessage;
	}

	private boolean isThisObject(ObisCode obisCode, String objectTypeName) {
		return (objectTypeName.equalsIgnoreCase(getObjectType(obisCode)));
	}

	public boolean isRegisterObject(ObisCode obisCode) {return isThisObject(obisCode, REGISTER_OBJECT);}
	public boolean isClockObject(ObisCode obisCode) {return isThisObject(obisCode, CLOCK_OBJECT);}
	public boolean isHdlcSetupObject(ObisCode obisCode) {return isThisObject(obisCode, HDLC_SETUP_OBJECT);}
	public boolean isAssociationObject(ObisCode obisCode) {return isThisObject(obisCode, ASSOCIATION_OBJECT);}
	public boolean isSapAssignmentObject(ObisCode obisCode) {return isThisObject(obisCode, SAP_ASSIGNMENT_OBJECT);}
	public boolean isDataObject(ObisCode obisCode) {return isThisObject(obisCode, DATA_OBJECT);}
	public boolean isProfileObject(ObisCode obisCode) {return isThisObject(obisCode, PROFILE_OBJECT);}
	public boolean isUnknownObject(ObisCode obisCode) {return isThisObject(obisCode, UNKNOWN_OBJECT);}

}
