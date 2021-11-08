/*
 * DLMSConfig.java
 *
 * Created on 4 april 2003, 14:57
 */

package com.energyict.dlms;

import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.exception.ConnectionCommunicationException;

/**
 *
 * @author  Koen
 *
 * DLMS configuration data, only 1 instance of DLMSConfig should exist!
 */

public final class DLMSConfig {

	//    SL7000 hardcoded object references in DLMSCOSEMGlobals
	//    final byte[] ASSOC_LN_OBJECT_LN={0,0,40,0,0,(byte)255};
	//    final byte[] SAP_OBJECT_LN={0,0,41,0,0,(byte)255};
	//    final byte[] CLOCK_OBJECT_LN={0,0,1,0,0,(byte)255};
	//    final byte[] HISTORIC_VALUES_OBJECT_LN={0,0,98,1,0,126};
	//    final byte[] LOAD_PROFILE_LN={0,0,99,1,0,(byte)255};


	private static final DLMSConfig CLOCK =  new DLMSConfig("",8,0,0,1,0,-1,255);
	private static final DLMSConfig PROFILE =  new DLMSConfig("",7,1,-1,99,1,-1,-1);

	// 18082004
	//    private static final DLMSConfig eventLog =  new DLMSConfig("",7,1,-1,99,98,-1,-1);
	private static final DLMSConfig HISTORICVALUES =  new DLMSConfig("",7,0,-1,98,1,-1,126);
	private static final DLMSConfig RESETCOUNTER =  new DLMSConfig("",3,1,0,0,1,0,255);
	private static final DLMSConfig IPV4SETUP = new DLMSConfig("",42,0,0,25,1,0,255);
	private static final DLMSConfig P3IMAGETRANSFER = new DLMSConfig("",18,0,0,44,0,0,255);
	private static final DLMSConfig IMAGEACTIVATIONSCHEDULE = new DLMSConfig("",22,0,0,15,0,2,255);
	private static final DLMSConfig DISCONNECTCONTROLSCHEDULE = new DLMSConfig("",22,0,0,15,0,1,255);
	private static final DLMSConfig CONSUMERMESSAGETEXT = new DLMSConfig("",1,0,0,96,13,0,255);
	private static final DLMSConfig CONSUMERMESSAGECODE = new DLMSConfig("",1,0,0,96,13,1,255);
	private static final DLMSConfig DISCONNECTOR = new DLMSConfig("",70,0,0,96,3,10,255);
	private static final DLMSConfig DISCONNECTORSCRIPTTABLE = new DLMSConfig("",9,0,0,10,0,106,255);
	private static final DLMSConfig LIMITER = new DLMSConfig("", 71,0,0,17,0,0,255);
	private static final DLMSConfig PPPSETUP = new DLMSConfig("", 44,0,0,25,3,0,255);
	private static final DLMSConfig GPRSMODEMSETUP = new DLMSConfig("", 45,0,0,25,4,0,255);
    private static final DLMSConfig LTEMODEMSETUP = new DLMSConfig("", 45,0,1,25,4,0,255);
	private static final DLMSConfig TARIFFSCRIPTTABLE = new DLMSConfig("",9,0,0,10,0,100,255);
	private static final DLMSConfig ACTIVITYCALENDAR = new DLMSConfig("",20,0,0,13,0,0,255);
	private static final DLMSConfig SPECIALDAYS = new DLMSConfig("",11,0,0,11,0,0,255);
    private static final DLMSConfig SFSKPhyMacSetupSN = new DLMSConfig("",50,0,0,26,0,0,255);
	private static final DLMSConfig USBSETUP = new DLMSConfig("",0,0,0,128,0,28,255);
	private static final DLMSConfig NBIOTMODEMSETUP = new DLMSConfig("", 45,0,1,25,4,0,255);


	private static final DLMSConfig[] configchange = {
		new DLMSConfig("GEC",1,0,0,96,2,0,255),
		new DLMSConfig("LGZ",3,0,0,96,2,0,255),
		new DLMSConfig("EIT",3,0,0,96,2,0,255),
		new DLMSConfig("ISK",1,0,0,96,2,0,255),
		new DLMSConfig("EMO",1,1,0,0,2,1,255),
		new DLMSConfig("SLB",4,0,0,96,2,0,255),
		new DLMSConfig("WKP",1,0,0,96,2,0,255)
	};

	private static final DLMSConfig[] eventLog = {
		new DLMSConfig("GEC",7,0,0,99,98,1,255),
		new DLMSConfig("LGZ",7,1,-1,99,98,-1,-1),
		new DLMSConfig("EIT",7,1,-1,99,98,-1,-1),
		new DLMSConfig("ISK",7,1,-1,99,98,-1,-1),
		new DLMSConfig("EMO",7,1,-1,99,98,-1,-1),
		new DLMSConfig("SLB",7,1,-1,99,98,-1,-1),
		new DLMSConfig("WKP",7,-1,-1,99,98,0,-1),   // First we check for E-field 0
		new DLMSConfig("WKP",7,-1,-1,99,98,-1,-1)   // If no match, try with wildcard for E-field
	};

	private static final DLMSConfig[] version = {
		new DLMSConfig("GEC",1,1,0,0,2,0,255),
		new DLMSConfig("LGZ",1,1,0,0,2,0,255),
		new DLMSConfig("EIT",1,1,0,0,2,0,255),
		new DLMSConfig("EMO",1,0,0,96,1,2,255),
		new DLMSConfig("SLB",1,0,0,142,1,1,255),
		new DLMSConfig("WKP",1,1,0,0,2,0,255)
	};

	private static final DLMSConfig[] status = {
		new DLMSConfig("WKP",1,0,0,96,10,1,255),
		new DLMSConfig("ISK",1,1,0,96,240,0,255)
	};

	/**
	 * For the NTA meters, there has been some changes regarding the A-field.
	 * Because the Mbus values are stored in the same Profile as the E-meter values, the A field should be '0'(meaning generic).
	 * But the other party decided to leave this to '1'...
	 *
	 * @return DLMSConfig[] the DLMSConfig for the DailyLoadProfile.
	 */
	private static final DLMSConfig[] dailyProfile = {
		new DLMSConfig("WKP",7,1,0,99,2,0,255)
	};

	private static final DLMSConfig[] monthlyProfile = {
		new DLMSConfig("WKP",7,0,0,98,1,0,255)
	};

	private static final DLMSConfig[] controlLog = {
		new DLMSConfig("WKP",7,0,0,99,98,2,255)
	};

	private static final DLMSConfig[] powerFailureLog = {
		new DLMSConfig("WKP",7,1,0,99,97,0,255)
	};

	private static final DLMSConfig[] communicationSessionLog = {
			new DLMSConfig("WKP",7,0,0,99,98,4,255)
	};

	private static final DLMSConfig[] voltageQualityLog = {
			new DLMSConfig("WKP",7,0,0,99,98,5,255)
	};

	private static final DLMSConfig[] fraudDetectionLog = {
		new DLMSConfig("WKP",7,0,0,99,98,1,255)
	};

	private static final DLMSConfig[] mbusEventLog = {
		new DLMSConfig("WKP",7,0,0,99,98,3,255)
	};

	private static final DLMSConfig[] mbusControlLog = {
		new DLMSConfig("WKP",7,0,1,24,5,0,255),
		new DLMSConfig("WKP",7,0,2,24,5,0,255),
		new DLMSConfig("WKP",7,0,3,24,5,0,255),
		new DLMSConfig("WKP",7,0,4,24,5,0,255)
	};

	private static final DLMSConfig[] mbusDisconnector = {
		new DLMSConfig("WKP",70,0,1,24,4,0,255),
		new DLMSConfig("WKP",70,0,2,24,4,0,255),
		new DLMSConfig("WKP",70,0,3,24,4,0,255),
		new DLMSConfig("WKP",70,0,4,24,4,0,255)
	};

	private static final DLMSConfig[] mbusDisconnectControlState = {
		//    	new DLMSConfig("ISK",7,0,1,128,30,31,255),
		//		new DLMSConfig("ISK",7,0,2,128,30,31,255),
		//		new DLMSConfig("ISK",7,0,3,128,30,31,255),
		//		new DLMSConfig("ISK",7,0,4,128,30,31,255)
	};

	private static final DLMSConfig[] mbusDisconnectControlSchedule = {
		new DLMSConfig("WKP",22,0,1,24,6,0,255),
		new DLMSConfig("WKP",22,0,2,24,6,0,255),
		new DLMSConfig("WKP",22,0,3,24,6,0,255),
		new DLMSConfig("WKP",22,0,4,24,6,0,255)
	};

	private static final DLMSConfig[] mbusDisconnectorScriptTable = {
		new DLMSConfig("WKP",9,0,1,24,7,0,255),
		new DLMSConfig("WKP",9,0,2,24,7,0,255),
		new DLMSConfig("WKP",9,0,3,24,7,0,255),
		new DLMSConfig("WKP",9,0,4,24,7,0,255)
	};

	private static final DLMSConfig[] serialNumber = {
		new DLMSConfig("GEC",1,1,0,0,0,0,255),
		new DLMSConfig("LGZ",1,1,0,0,0,0,255),
		new DLMSConfig("EMO",1,1,0,0,0,0,255),
		new DLMSConfig("SLB",1,0,0,96,1,0,255),
		new DLMSConfig("SLB",1,0,0,96,1,255,255),
		new DLMSConfig("ISK",1,0,0,96,1,0,255),
		new DLMSConfig("WKP",1,0,0,96,1,0,255)
	};

	private static final DLMSConfig[] mbusSerialNumber = {
		new DLMSConfig("WKP",1,0,1,96,1,0,255),
		new DLMSConfig("WKP",1,0,2,96,1,0,255),
		new DLMSConfig("WKP",1,0,3,96,1,0,255),
		new DLMSConfig("WKP",1,0,4,96,1,0,255)
	};

	private static final DLMSConfig[] mbusStatus = {
		new DLMSConfig("WKP",1,0,1,96,10,3,255),
		new DLMSConfig("WKP",1,0,2,96,10,3,255),
		new DLMSConfig("WKP",1,0,3,96,10,3,255),
		new DLMSConfig("WKP",1,0,4,96,10,3,255)
	};

	private static final DLMSConfig[] mbusProfile = {
		new DLMSConfig("WKP",7,0,1,24,3,0,255),
		new DLMSConfig("WKP",7,0,2,24,3,0,255),
		new DLMSConfig("WKP",7,0,3,24,3,0,255),
		new DLMSConfig("WKP",7,0,4,24,3,0,255)
	};

	private static final DLMSConfig[] mbusClient = {
		new DLMSConfig("WKP",72,0,1,24,1,0,255),
		new DLMSConfig("WKP",72,0,2,24,1,0,255),
		new DLMSConfig("WKP",72,0,3,24,1,0,255),
		new DLMSConfig("WKP",72,0,4,24,1,0,255),
	};

	private static final DLMSConfig[] xmlConfig = {
		new DLMSConfig("WKP",1,0,129,0,0,0,255)
	};

	private static final DLMSConfig[] meterReading = {
		new DLMSConfig("LGZ",3,1,1,1,8,0,255),
		new DLMSConfig("LGZ",3,1,1,5,8,0,255),
		new DLMSConfig("LGZ",3,1,1,8,8,0,255),
		new DLMSConfig("LGZ",3,1,1,2,8,0,255),
		new DLMSConfig("LGZ",3,1,1,7,8,0,255),
		new DLMSConfig("LGZ",3,1,1,6,8,0,255),
		new DLMSConfig("LGZ",3,1,1,10,8,0,255),

		new DLMSConfig("EIT",3,1,1,82,8,0,255),
		new DLMSConfig("EIT",3,1,2,82,8,0,255),
		new DLMSConfig("EIT",3,1,3,82,8,0,255),
		new DLMSConfig("EIT",3,1,4,82,8,0,255),
		new DLMSConfig("EIT",3,1,5,82,8,0,255),
		new DLMSConfig("EIT",3,1,6,82,8,0,255),
		new DLMSConfig("EIT",3,1,7,82,8,0,255),
		new DLMSConfig("EIT",3,1,8,82,8,0,255),
		new DLMSConfig("EIT",3,1,9,82,8,0,255),
		new DLMSConfig("EIT",3,1,10,82,8,0,255),
		new DLMSConfig("EIT",3,1,11,82,8,0,255),
		new DLMSConfig("EIT",3,1,12,82,8,0,255),
		new DLMSConfig("EIT",3,1,13,82,8,0,255),
		new DLMSConfig("EIT",3,1,14,82,8,0,255),
		new DLMSConfig("EIT",3,1,15,82,8,0,255),
		new DLMSConfig("EIT",3,1,16,82,8,0,255),
		new DLMSConfig("EIT",3,1,17,82,8,0,255),
		new DLMSConfig("EIT",3,1,18,82,8,0,255),
		new DLMSConfig("EIT",3,1,19,82,8,0,255),
		new DLMSConfig("EIT",3,1,20,82,8,0,255),
		new DLMSConfig("EIT",3,1,21,82,8,0,255),
		new DLMSConfig("EIT",3,1,22,82,8,0,255),
		new DLMSConfig("EIT",3,1,23,82,8,0,255),
		new DLMSConfig("EIT",3,1,24,82,8,0,255),
		new DLMSConfig("EIT",3,1,25,82,8,0,255),
		new DLMSConfig("EIT",3,1,26,82,8,0,255),
		new DLMSConfig("EIT",3,1,27,82,8,0,255),
		new DLMSConfig("EIT",3,1,28,82,8,0,255),
		new DLMSConfig("EIT",3,1,29,82,8,0,255),
		new DLMSConfig("EIT",3,1,30,82,8,0,255),
		new DLMSConfig("EIT",3,1,31,82,8,0,255),
		new DLMSConfig("EIT",3,1,32,82,8,0,255),

		new DLMSConfig("EMO",4,1,1,1,8,1,255),
		new DLMSConfig("EMO",4,1,1,1,8,2,255),
		new DLMSConfig("EMO",4,1,1,2,8,1,255),
		new DLMSConfig("EMO",4,1,1,2,8,2,255),
		new DLMSConfig("EMO",4,1,1,1,6,1,255),
		new DLMSConfig("EMO",4,1,1,2,6,1,255),
		new DLMSConfig("EMO",4,1,1,3,6,1,255),
		new DLMSConfig("EMO",4,1,1,4,6,1,255)
	};

	private int classid;
	private int a;
	private int b;
	private int c;
	private int d;
	private int e;
	private int f;
	private String manuf;

	static private DLMSConfig config=null;
	static public DLMSConfig getInstance() {
		if (config == null) {
			config = new DLMSConfig();
		}
		return config;
	}

	private DLMSConfig() {
	}
	/** Creates a new instance of DLMSConfig */
	private DLMSConfig(String manuf,int classid, int a, int b, int c, int d, int e, int f) {
		this.a=a;
		this.b=b;
		this.c=c;
		this.d=d;
		this.e=e;
		this.f=f;
		this.classid=classid;
		this.manuf = manuf;
	}

	protected String getManuf() {
		return this.manuf;
	}
	public int getClassID() {
		return this.classid;
	}
	public int getLNA() {
		return this.a;
	}
	public int getLNB() {
		return this.b;
	}
	public int getLNC() {
		return this.c;
	}
	public int getLND() {
		return this.d;
	}
	public int getLNE() {
		return this.e;
	}
	public int getLNF() {
		return this.f;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a configchange DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return DLMSConfig
	 */
	@SuppressWarnings("unused")
	protected DLMSConfig getConfig(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        return getMatchingDLMSConfigFromObjectList(objectList, manuf, configchange, "getConfig");
    }

    /*
     *  Find in objectList a matching DLMSConfig object with a version DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return DLMSConfig
     */
	protected DLMSConfig getVersion(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
		return getMatchingDLMSConfigFromObjectList(objectList, manuf, version, "getVersion");
    }

    /*
     *  Find in objectList a matching DLMSConfig object with a serialNumber DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return DLMSConfig
     */
	protected DLMSConfig getSerialNumber(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        return getMatchingDLMSConfigFromObjectList(objectList, manuf, serialNumber, "getSerialNumber");
    }

	protected DLMSConfig getClock() {
		return CLOCK;
	}

	protected DLMSConfig getProfile() {
		return PROFILE;
	}

	protected DLMSConfig getEventLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        return getMatchingDLMSConfigFromObjectList(objectList, manuf, eventLog, "getEventLog");
	}

	@SuppressWarnings("unused")
	protected DLMSConfig getHistoricValues() {
		return HISTORICVALUES;
	}

	protected DLMSConfig getResetCounter() {
		return RESETCOUNTER;
	}

	@SuppressWarnings("unused")
	protected DLMSConfig getIPv4Setup() {
		return IPV4SETUP;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a configchange DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getConfigSN(UniversalObject[] objectList, String manuf) {
		return getSNFromObjectList(objectList, manuf, configchange, "getConfigSN");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a version DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getVersionSN(UniversalObject[] objectList, String manuf) {
		return getSNFromObjectList(objectList, manuf, version, "getVersionSN");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a serialNumber DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getSerialNumberSN(UniversalObject[] objectList, String manuf) {
        return getSNFromObjectList(objectList, manuf, serialNumber, "getSerialNumberSN");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the clock DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getClockSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, CLOCK, "getClockSN");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the profile DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getProfileSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, PROFILE, "getProfileSN");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the eventLog DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getEventLogSN(UniversalObject[] objectList, String manuf) {
		return getSNFromObjectList(objectList, manuf, eventLog, "getEventLogSN");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the historicValues DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getHistoricValuesSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, HISTORICVALUES, "getHistoricValuesSN");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the resetCounter DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getResetCounterSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, RESETCOUNTER, "getResetCounterSN");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a configchange DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getConfigObject(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, configchange, "getConfigObject");
    }

	/*
	 *  Find in objectList a matching DLMSConfig object with a version DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getVersionObject(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, version, "getVersionObject");
    }

	/*
	 *  Find in objectList a matching DLMSConfig object with a status DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getStatusObject(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, status, "getStatusObject");
    }

	/*
	 *  Find in objectList a matching DLMSConfig object with a serialNumber DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getSerialNumberObject(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, serialNumber, "getSerialNumberObject");
    }

	/*
	 *  Find in objectList a matching DLMSConfig object with the clock DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getClockObject(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, CLOCK, "getClockObject");
    }

	/*
	 *  Find in objectList a matching DLMSConfig object with the profile DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getProfileObject(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, PROFILE, "getProfileObject");
    }

	protected UniversalObject getDailyProfileObject(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, dailyProfile, "getDailyProfileObject");
    }

	protected UniversalObject getMonthlyProfileObject(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, monthlyProfile, "getMonthlyProfileObject");
    }

	/*
	 *  Find in objectList a matching DLMSConfig object with the eventLog DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getEventLogObject(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, eventLog, "getEventLogObject");
    }

	protected UniversalObject getControlLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException{
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, controlLog, "getControlLogObject");
    }

	protected UniversalObject getPowerFailureLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException{
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, powerFailureLog, "getPowerFailureObject");
    }

    protected UniversalObject getCommunicationSessionLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, communicationSessionLog, "getCommunicationSessionLog");
	}

	public UniversalObject getVoltageQualityLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, voltageQualityLog, "getVoltageQualityLog");
	}

	protected UniversalObject getFraudDetectionLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException{
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, fraudDetectionLog, "getFraudDetectionLogObject");
    }

	protected UniversalObject getMbusEventLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException{
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusEventLog, "getMbusEventLog");
    }

	protected UniversalObject getMbusControlLog(UniversalObject[] objectList, String manuf, int zeroBasedChannel) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusControlLog, "getMbusControlLog", zeroBasedChannel);
    }

	protected UniversalObject getMbusDisconnector(UniversalObject[] objectList, String manuf, int zeroBasedChannel) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusDisconnector, "getMbusDisconnectControl", zeroBasedChannel);
    }

	protected UniversalObject getMbusDisconnectControlState(UniversalObject[] objectList, String manuf, int zeroBasedChannel) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusDisconnectControlState, "getMbusDisconnectControlState", zeroBasedChannel);
    }

	protected UniversalObject getMbusSerialNumber(UniversalObject[] objectList, String manuf, int zeroBasedChannel) throws NotInObjectListException{
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusSerialNumber, "getMbusSerialNumer", zeroBasedChannel);
    }

	protected UniversalObject getMbusStatusObject(UniversalObject[] objectList, String manuf, int zeroBasedChannel) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusStatus, "getMbusStatusObject", zeroBasedChannel);
    }

	public UniversalObject getMbusProfile(UniversalObject[] objectList, String manuf, int zeroBasedChannel) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusProfile, "getMbusProfile", zeroBasedChannel);
    }

	public UniversalObject getMbusDisconnectControlSchedule(UniversalObject[] objectList, String manuf, int zeroBasedChannel) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusDisconnectControlSchedule, "getMbusDisconnectControlSchedule", zeroBasedChannel);
    }

	public UniversalObject getMbusClient(UniversalObject[] objectList, String manuf, int zeroBasedChannel) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusClient, "getMbusClient", zeroBasedChannel);
    }

	public UniversalObject getMbusDisconnectorScriptTable(UniversalObject[] objectList, String manuf, int zeroBasedChannel) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, manuf, mbusDisconnectorScriptTable, "getMbusDisconnectorScriptTable", zeroBasedChannel);
    }

	public UniversalObject getXMLConfig(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        return getMatchingUniversalObjectFromObjectList(objectList, manuf, xmlConfig, "getXMLConfig");
    }

	/*
	 *  Find in objectList a matching DLMSConfig object with the historicValues DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	@SuppressWarnings("unused")
	protected UniversalObject getHistoricValuesObject(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, HISTORICVALUES, "getHistoricValuesObject");
    }

	/*
	 *  Find in objectList a matching DLMSConfig object with the resetCounter DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	@SuppressWarnings("unused")
	protected UniversalObject getResetcounterObject(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, RESETCOUNTER, "getResetcounterObject");
    }

	/*
	 *  Find in meterReading DLMSConfig objects all the objects assigned to a specific deviceID
	 *  @param String deviceId
	 *  @return int nr of meterreading DLMSConfig objects
	 */
	private int getNrOfMeterReadingObjects(String deviceId) {
		int count=0;
		for (DLMSConfig dlmsConfig : meterReading) {
			if (dlmsConfig.getManuf().compareTo(deviceId) == 0) {
				count++;
			}
		}
		return count;
	}

	/*
	 *  Get the meterReading DLMSConfig object for a specific id
	 *  @param String deviceId
	 *  @param int id
	 *  @return DLMSConfig
	 */
	private DLMSConfig getMeterReadingDLMSConfigObject(int id,String deviceId) throws NotInObjectListException {
		int count=0;
		for (DLMSConfig dlmsConfig : meterReading) {
			if (dlmsConfig.getManuf().compareTo(deviceId) == 0) {
				if (id == count) {
					return dlmsConfig;
				}
				count++;
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMeterReadingDLMSConfigObject("+id+","+deviceId+"), not found in objectlist (IOL)!");
    }

	/*
	 *  Find in objectList a matching DLMSConfig object with a meterreading DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @param String deviceId
	 *  @param int id
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getMeterReadingObject(UniversalObject[] objectList,int id, String deviceId) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getMeterReadingObject, objectlist empty!");
        if (id >=getNrOfMeterReadingObjects(deviceId)) {
            throw new NotInObjectListException("DLMSConfig, getMeterReadingObject, meterreading id error!");
        }
		//for (int t=0;t<version.length;t++) { // KV 17062003 removed
		for (UniversalObject universalObject : objectList) {
			DLMSConfig dlmsConfig = getMeterReadingDLMSConfigObject(id, deviceId);
			//System.out.println(dlmsConfig.toString()+" == "+objectList[i].toString()+" ?");
			if (universalObject.equals(dlmsConfig)) {
				return universalObject;
			}
		}
		//}
        throw new NotInObjectListException("DLMSConfig, getMeterReadingObject("+id+","+deviceId+"), not found in objectlist (IOL)!");
    }

	public String toString() {
		return this.getLNA()+"."+
		this.getLNB()+"."+
		this.getLNC()+"."+
		this.getLND()+"."+
		this.getLNE()+"."+
		this.getLNF()+"."+
		this.getClassID();
	}

	public UniversalObject getIPv4SetupObject(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, IPV4SETUP, "ipv4SetupObject");
    }

	public UniversalObject getImageActivationSchedule(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, IMAGEACTIVATIONSCHEDULE, "imageActivationSchedule");
    }

	public int getIPv4SetupSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, IPV4SETUP, "ipv4Setup");
	}

	public UniversalObject getP3ImageTransfer(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, P3IMAGETRANSFER, "P3ImageTransfer");
    }

	public int getP3ImageTransferSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, P3IMAGETRANSFER, "P3ImageTransfer");
	}

	public UniversalObject getConsumerMessageText(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, CONSUMERMESSAGETEXT, "ConsumerMessageText");
    }

	@SuppressWarnings("unused")
	public int getConsumerMessageTextSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, CONSUMERMESSAGETEXT, "ConsumerMessageText");
	}

	public UniversalObject getConsumerMessageCode(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, CONSUMERMESSAGECODE, "ConsumerMessageCode");
    }

	@SuppressWarnings("unused")
	public int getConsumerMessageCodeSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, CONSUMERMESSAGECODE, "ConsumerMessageCode");
	}

	public UniversalObject getDisconnector(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, DISCONNECTOR, "Disconnector");
    }

	public int getDisconnectorSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, DISCONNECTOR, "DisconnectorSN");
	}

	public UniversalObject getDisconnectControlSchedule(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, DISCONNECTCONTROLSCHEDULE, "DisconnectSchedule");
    }

	public UniversalObject getDisconnectorScriptTable(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, DISCONNECTORSCRIPTTABLE, "DisconnectorScriptTable");
    }

	public UniversalObject getTariffScriptTable(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, TARIFFSCRIPTTABLE, "getTariffScriptTable");
    }

	public UniversalObject getActivityCalendar(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList, ACTIVITYCALENDAR, "getActivityCalendar");
    }

	public UniversalObject getSpecialDaysTable(UniversalObject[] objectList) throws NotInObjectListException{
		return getMatchingUniversalObjectFromObjectList(objectList, SPECIALDAYS, "getSpecialDaysTable");
    }

	public int getDisconnectorScriptTableSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, DISCONNECTORSCRIPTTABLE, "DisconnectorScriptTableSN");
	}

	public UniversalObject getLimiter(UniversalObject[] objectList) throws NotInObjectListException {
		return getMatchingUniversalObjectFromObjectList(objectList,LIMITER, "Limiter");
    }

	public int getLimiterSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, LIMITER, "Limiter");
	}

	public int getPPPSetupSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, PPPSETUP, "PPPSetup");
	}

	public int getGPRSModemSetupSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, GPRSMODEMSETUP, "GPRSModemSetup");
	}


	public int getNBIOTModemSetupSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, NBIOTMODEMSETUP, "NBIOTModemSetup");
	}

    public int getLTEModemSetupSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, LTEMODEMSETUP, "LTEModemSetup");
    }

	public int getUSBSetupSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, USBSETUP, "USBSetup");
	}

    public int getImageTransferSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, P3IMAGETRANSFER, "ImageTransfer");
    }

    public int getSFSKPhyMacSetupSN(UniversalObject[] objectList) {
		return getSNFromObjectList(objectList, SFSKPhyMacSetupSN, "SFSKPhyMacSetupSN");
    }

	private int getSNFromObjectList(UniversalObject[] objectList, String manuf, DLMSConfig[] theObjects, String methodName) {
		checkEmptyObjectList(objectList, "DLMSConfig, "+methodName+", objectlist empty!");
		for (DLMSConfig dlmsConfig : theObjects) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (dlmsConfig.getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (UniversalObject universalObject : objectList) {
				if (universalObject.equals(dlmsConfig)) {
					return universalObject.getBaseName();
				}
			}
		}
		return 0;
	}

	private int getSNFromObjectList(UniversalObject[] objectList, DLMSConfig theConfig, String objectName) {
		checkEmptyObjectList(objectList, "DLMSConfig, "+objectName+", objectlist empty!");
		for (UniversalObject universalObject : objectList) {
			if (universalObject.equals(theConfig)) {
				return universalObject.getBaseName();
			}
		}
		return 0;
	}

	private UniversalObject getMatchingUniversalObjectFromObjectList(UniversalObject[] objectList, String manuf, DLMSConfig[] theConfigs, String methodName) throws NotInObjectListException {
		checkEmptyObjectList(objectList, "DLMSConfig, " + methodName+ ", objectlist empty!");
		for (DLMSConfig dlmsConfig : theConfigs) {
			if ((manuf != null) && (dlmsConfig.getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (UniversalObject universalObject : objectList) {
				if (universalObject.equals(dlmsConfig)) {
					return universalObject;
				}
			}
		}
		throw new NotInObjectListException("DLMSConfig, "+methodName+", not found in objectlist (IOL)");
	}

	private DLMSConfig getMatchingDLMSConfigFromObjectList(UniversalObject[] objectList, String manuf, DLMSConfig[] theConfigs, String methodName) throws NotInObjectListException {
		checkEmptyObjectList(objectList, "DLMSConfig, " + methodName+ ", objectlist empty!");
		for (DLMSConfig dlmsConfig : theConfigs) {
			if ((manuf != null) && (dlmsConfig.getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (UniversalObject universalObject : objectList) {
				if (universalObject.equals(dlmsConfig)) {
					return dlmsConfig;
				}
			}
		}
		throw new NotInObjectListException("DLMSConfig, "+methodName+", not found in objectlist (IOL)");
	}

	private UniversalObject getMatchingUniversalObjectFromObjectList(UniversalObject[] objectList, String manuf, DLMSConfig[] theConfigs, String methodName, int zeroBasedChannel) throws NotInObjectListException {
		int count = 0;
		checkEmptyObjectList(objectList, "DLMSConfig, "+methodName+", objectlist empty!");
		for (DLMSConfig dlmsConfig : theConfigs) {
			if ((manuf != null) && (dlmsConfig.getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if (count++ == zeroBasedChannel) {
				for (UniversalObject universalObject : objectList) {
					if (universalObject.equals(dlmsConfig)) {
						return universalObject;
					}
				}
			}
		}
		throw new NotInObjectListException("DLMSConfig, "+methodName+", not found in objectlist (IOL)");
	}

	private UniversalObject getMatchingUniversalObjectFromObjectList(UniversalObject[] objectList, DLMSConfig theConfig, String objectName) throws NotInObjectListException {
		checkEmptyObjectList(objectList, "DLMSConfig, "+objectName+", objectlist empty!");
		for (UniversalObject universalObject : objectList) {
			if (universalObject.equals(theConfig)) {
				return universalObject;
			}
		}
		throw new NotInObjectListException("DLMSConfig, "+objectName+", not found in objectlist (IOL)!");
	}

	private void checkEmptyObjectList(UniversalObject[] objectList, String msg) {
		if (objectList == null) {
			ProtocolException protocolException = new ProtocolException(msg);
			throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
		}
	}
}
