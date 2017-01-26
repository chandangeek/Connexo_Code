package com.energyict.dlms;

import com.energyict.mdc.protocol.api.NotInObjectListException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

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
	private static final DLMSConfig TARIFFSCRIPTTABLE = new DLMSConfig("",9,0,0,10,0,100,255);
	private static final DLMSConfig ACTIVITYCALENDAR = new DLMSConfig("",20,0,0,13,0,0,255);
	private static final DLMSConfig SPECIALDAYS = new DLMSConfig("",11,0,0,11,0,0,255);
    private static final DLMSConfig SFSKPhyMacSetupSN = new DLMSConfig("",50,0,0,26,0,0,255);
	private static final DLMSConfig USBSETUP = new DLMSConfig("",0,0,0,128,0,28,255);


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
	 * @return the DLMSConfig for the DailyLoadProfile.
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
	protected DLMSConfig getConfig(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getConfig, objectlist empty!");
		for (int t=0;t<configchange.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (configchange[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].equals(configchange[t])) {
					return configchange[t];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getConfig, not found in objectlist (IOL)!");
    }

    private void checkEmptyObjectList(UniversalObject[] objectList, String msg) {
        if (objectList == null) {
            ProtocolException protocolException = new ProtocolException(msg);
			throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, protocolException);
        }
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a version DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return DLMSConfig
	 */
	protected DLMSConfig getVersion(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getVersion, objectlist empty!");
		for (int t=0;t<version.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (version[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].equals(version[t])) {
					return version[t];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getVersion, not found in objectlist (IOL)!");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a serialNumber DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return DLMSConfig
	 */
	protected DLMSConfig getSerialNumber(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getSerialNumber, objectlist empty!");
		for (int t=0;t<serialNumber.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (serialNumber[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].equals(serialNumber[t])) {
					return serialNumber[t];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getSerialNumber, not found in objectlist (IOL)!");
	}

	protected DLMSConfig getClock() {
		return CLOCK;
	}

	protected DLMSConfig getProfile() {
		return PROFILE;
	}

	protected DLMSConfig getEventLog(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getEventLog, objectlist empty!");
		for (int t=0;t<eventLog.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (eventLog[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].equals(eventLog[t])) {
					return eventLog[t];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getEventLog, not found in objectlist (IOL)!");
		//        return eventLog;
	}

	protected DLMSConfig getHistoricValues() {
		return HISTORICVALUES;
	}

	protected DLMSConfig getResetCounter() {
		return RESETCOUNTER;
	}

	protected DLMSConfig getIPv4Setup() {
		return IPV4SETUP;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a configchange DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getConfigSN(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getConfigSN, objectlist empty!");
		for (int t=0;t<configchange.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (configchange[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].equals(configchange[t])) {
					return objectList[i].getBaseName();
				}
			}
		}
		return 0;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a version DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getVersionSN(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getVersionSN, objectlist empty!");
		for (int t=0;t<version.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (version[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].equals(version[t])) {
					return objectList[i].getBaseName();
				}
			}
		}
		return 0;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a serialNumber DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getSerialNumberSN(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getSerialNumberSN, objectlist empty!");
		for (int t=0;t<serialNumber.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (serialNumber[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].equals(serialNumber[t])) {
					return objectList[i].getBaseName();
				}
			}
		}
		return 0;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the clock DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getClockSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getClockSN, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(CLOCK)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the profile DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getProfileSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getProfileSN, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(PROFILE)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the eventLog DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getEventLogSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getEventLogSN, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(eventLog)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the historicValues DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getHistoricValuesSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getHistoricValuesSN, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(HISTORICVALUES)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the resetCounter DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return int short name reference
	 */
	protected int getResetCounterSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getResetCounterSN, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(RESETCOUNTER)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a configchange DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getConfigObject(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getConfigObject, objectlist empty!");
		for (int t=0;t<configchange.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (configchange[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {

				//System.out.println("KV_DEBUG> "+objectList[i].toString()+" == "+ configchange[t].toString()+" ?");
				if (objectList[i].equals(configchange[t])) {
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getConfigObject, not found in objectlist (IOL)!");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a version DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getVersionObject(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getVersionObject, objectlist empty!");
		for (int t=0;t<version.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (version[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].equals(version[t])) {
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getVersionObject, not found in objectlist (IOL)!");

	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a status DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getStatusObject(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getStatusObject, objectlist empty!");
		for(int t = 0; t < status.length; t++){
			if((manuf != null) && (status[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for(int i = 0; i < objectList.length; i++){
				if(objectList[i].equals(status[t])){
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getStatusObject, not found in objectlist (IOL)");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with a serialNumber DLMSConfig object
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getSerialNumberObject(UniversalObject[] objectList,String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getSerialNumberObject, objectlist empty!");
		for (int t=0;t<serialNumber.length;t++) {
			// if manuf != null, use it in the search for DLMSConfig object!
			if ((manuf != null) && (serialNumber[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for (int i=0;i<objectList.length;i++) {
				if (objectList[i].equals(serialNumber[t])) {
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getSerialNumberObject, not found in objectlist (IOL)!");

	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the clock DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getClockObject(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getClockObject, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(CLOCK)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, getClockObject, not found in objectlist (IOL)!");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the profile DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getProfileObject(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getProfileObject, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(PROFILE)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, getProfileObject, not found in objectlist (IOL)!");
	}


	protected UniversalObject getDailyProfileObject(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getDailyProfileObject, objectlist empty!");
		for(int t = 0; t < dailyProfile.length; t++){
			if((manuf != null) && (dailyProfile[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for(int i = 0; i < objectList.length; i++){
				if(objectList[i].equals(dailyProfile[t])){
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getDailyProfileObject, not found in objectlist (IOL)");
	}

	protected UniversalObject getMonthlyProfileObject(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getMonthlyProfileObject, objectlist empty!");
		for(int t = 0; t < monthlyProfile.length; t++){
			if((manuf != null) && (monthlyProfile[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for(int i = 0; i < objectList.length; i++){
				if(objectList[i].equals(monthlyProfile[t])){
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMonthlyObject, not found in objectlist (IOL)");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the eventLog DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getEventLogObject(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getEventLogObject, objectlist empty!");
		for(int t = 0; t < eventLog.length; t++){
			if((manuf != null) && (eventLog[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for(int i = 0; i < objectList.length; i++){
				if(objectList[i].equals(eventLog[t])){
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getEventLogObject, not found in objectlist (IOL)");
	}

	protected UniversalObject getControlLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException{
        checkEmptyObjectList(objectList, "DLMSConfig, getControlLogObject, objectlist empty!");
		for(int t = 0; t < controlLog.length; t++){
			if((manuf != null) && (controlLog[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for(int i = 0; i < objectList.length; i++){
				if(objectList[i].equals(controlLog[t])){
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getControlLogObject, not found in objectlist (IOL)");
	}

	protected UniversalObject getPowerFailureLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException{
        checkEmptyObjectList(objectList, "DLMSConfig, getPowerFailureObject, objectlist empty!");
		for(int t = 0; t < powerFailureLog.length; t++){
			if((manuf != null) && (powerFailureLog[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for(int i = 0; i < objectList.length; i++){
				if(objectList[i].equals(powerFailureLog[t])){
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getPowerFailureObject, not found in objectlist (IOL");
	}

	protected UniversalObject getFraudDetectionLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException{
        checkEmptyObjectList(objectList, "DLMSConfig, getFraudDetectionLogObject, objectlist empty!");
		for(int t = 0; t < fraudDetectionLog.length; t++){
			if((manuf != null) && (fraudDetectionLog[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for(int i = 0; i < objectList.length; i++){
				if(objectList[i].equals(fraudDetectionLog[t])){
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getFraudDetectionLogObject, not found in objectlist (IOL)");
	}

	protected UniversalObject getMbusEventLog(UniversalObject[] objectList, String manuf) throws NotInObjectListException{
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusEventLog, objectlist empty!");
		for(int t = 0; t < mbusEventLog.length; t++){
			if((manuf != null) && (mbusEventLog[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for(int i = 0; i < objectList.length; i++){
				if(objectList[i].equals(mbusEventLog[t])){
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusEventLog, not found in objectlist (IOL)");
	}

	protected UniversalObject getMbusControlLog(UniversalObject[] objectList, String manuf, int channel) throws NotInObjectListException{
		int count = 0;
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusControlLog, objectlist empty!");
		for(int t = 0; t < mbusControlLog.length; t++){
			if((manuf != null) && (mbusControlLog[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if(count++ == channel){
				for(int i = 0; i < objectList.length; i++){
					if(objectList[i].equals(mbusControlLog[t])){
						return objectList[i];
					}
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusControlLog, not found in objectlist (IOL)");
	}

	protected UniversalObject getMbusDisconnector(UniversalObject[] objectList, String manuf, int channel) throws NotInObjectListException{
		int count = 0;
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusDisconnectControl, objectlist empty!");
		for(int t = 0; t < mbusDisconnector.length; t++){
			if((manuf != null) && (mbusDisconnector[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if(count++ == channel){
				for(int i = 0; i < objectList.length; i++){
					if(objectList[i].equals(mbusDisconnector[t])){
						return objectList[i];
					}
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusDisconnectControl, not found in objectlist (IOL)");
	}

	protected UniversalObject getMbusDisconnectControlState(UniversalObject[] objectList, String manuf, int channel) throws NotInObjectListException{
		int count = 0;
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusDisconnectControlState, objectlist empty!");
		for(int t = 0; t < mbusDisconnectControlState.length; t++){
			if((manuf != null) && (mbusDisconnectControlState[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if(count++ == channel){
				for(int i = 0; i < objectList.length; i++){
					if(objectList[i].equals(mbusDisconnectControlState[t])){
						return objectList[i];
					}
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusDisconnectControlState, not found in objectlist (IOL)");
	}

	protected UniversalObject getMbusSerialNumber(UniversalObject[] objectList, String manuf, int channel) throws NotInObjectListException{
		int count = 0;
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusSerialNumber, objectlist empty!");
		for(int t = 0; t < mbusSerialNumber.length; t++){
			if((manuf != null) && (mbusSerialNumber[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if(count++ == channel){
				for(int i = 0; i < objectList.length; i++){
					if(objectList[i].equals(mbusSerialNumber[t])){
						return objectList[i];
					}
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusSerialNumber, not found in objectlist (IOL)");
	}

	protected UniversalObject getMbusStatusObject(UniversalObject[] objectList, String manuf, int channel) throws NotInObjectListException{
		int count = 0;
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusStatusObject, objectlist empty!");
		for(int t = 0; t < mbusStatus.length; t++){
			if((manuf != null) && (mbusStatus[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if(count++ == channel){
				for(int i = 0; i < objectList.length; i++){
					if(objectList[i].equals(mbusStatus[t])){
						return objectList[i];
					}
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusStatusObject, not found in objectlist (IOL)");
	}

	public UniversalObject getMbusProfile(UniversalObject[] objectList, String manuf, int channel) throws NotInObjectListException {
		int count = 0;
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusProfile, objectlist empty!");
		for(int t = 0; t < mbusProfile.length; t++){
			if((manuf != null) && (mbusProfile[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if(count++ == channel){
				for(int i = 0; i < objectList.length; i++){
					if(objectList[i].equals(mbusProfile[t])){
						return objectList[i];
					}
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusProfile, not found in objectlist (IOL)");
	}

	public UniversalObject getMbusDisconnectControlSchedule(UniversalObject[] objectList, String manuf, int channel) throws NotInObjectListException {
		int count = 0;
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusDisconnectControlSchedule, objectlist empty!");
		for(int t = 0; t < mbusDisconnectControlSchedule.length; t++){
			if((manuf != null) && (mbusDisconnectControlSchedule[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if(count++ == channel){
				for(int i = 0; i < objectList.length; i++){
					if(objectList[i].equals(mbusDisconnectControlSchedule[t])){
						return objectList[i];
					}
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusDisconnectControlSchedule, not found in objectlist (IOL)");
	}

	public UniversalObject getMbusClient(UniversalObject[] objectList, String manuf, int channel) throws NotInObjectListException {
		int count = 0;
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusClient, objectlist empty!");
		for(int t = 0; t < mbusClient.length; t++){
			if((manuf != null) && (mbusClient[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if(count++ == channel){
				for(int i = 0; i < objectList.length; i++){
					if(objectList[i].equals(mbusClient[t])){
						return objectList[i];
					}
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusClient, not found in objectlist (IOL)");
	}

	public UniversalObject getMbusDisconnectorScriptTable(UniversalObject[] objectList, String manuf, int channel) throws NotInObjectListException {
		int count = 0;
        checkEmptyObjectList(objectList, "DLMSConfig, getMbusDisconnectorScriptTable, objectlist empty!");
		for(int t = 0; t < mbusDisconnectorScriptTable.length; t++){
			if((manuf != null) && (mbusDisconnectorScriptTable[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			if(count++ == channel){
				for(int i = 0; i < objectList.length; i++){
					if(objectList[i].equals(mbusDisconnectorScriptTable[t])){
						return objectList[i];
					}
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getMbusDisconnectorScriptTable, not found in objectlist (IOL)");
	}

	public UniversalObject getXMLConfig(UniversalObject[] objectList, String manuf) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getXMLConfig, objectlist empty!");
		for(int t = 0; t < xmlConfig.length; t++){
			if((manuf != null) && (xmlConfig[t].getManuf().compareTo(manuf) != 0)) {
				continue;
			}
			for(int i = 0; i < objectList.length; i++){
				if(objectList[i].equals(xmlConfig[t])){
					return objectList[i];
				}
			}
		}
        throw new NotInObjectListException("DLMSConfig, getXMLConfig, not found in objectlist (IOL)");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the historicValues DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getHistoricValuesObject(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getHistoricValuesObject, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(HISTORICVALUES)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, getHistoricValuesObject, not found in objectlist (IOL)!");
	}

	/*
	 *  Find in objectList a matching DLMSConfig object with the resetCounter DLMSConfig objects
	 *  @param UniversalObject[] objectList
	 *  @return UniversalObject the matching objectList
	 */
	protected UniversalObject getResetcounterObject(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getResetcounterObject, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(RESETCOUNTER)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, getResetcounterObject, not found in objectlist (IOL)!");
	}

	/*
	 *  Find in meterReading DLMSConfig objects all the objects assigned to a specific deviceID
	 *  @param String deviceId
	 *  @return int nr of meterreading DLMSConfig objects
	 */
	private int getNrOfMeterReadingObjects(String deviceId) {
		int count=0;
		for (int i=0;i<meterReading.length;i++) {
			if (meterReading[i].getManuf().compareTo(deviceId) == 0) {
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
		for (int i=0;i<meterReading.length;i++) {
			if (meterReading[i].getManuf().compareTo(deviceId) == 0) {
				if (id == count) {
					return meterReading[i];
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
		for (int i=0;i<objectList.length;i++) {
			DLMSConfig dlmsConfig = getMeterReadingDLMSConfigObject(id,deviceId);
			//System.out.println(dlmsConfig.toString()+" == "+objectList[i].toString()+" ?");
			if (objectList[i].equals(dlmsConfig)) {
				return objectList[i];
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
        checkEmptyObjectList(objectList, "DLMSConfig, ipv4SetupObject, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(IPV4SETUP)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, ipv4SetupObject, not found in objectlist (IOL)!");
	}

	public UniversalObject getImageActivationSchedule(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, imageActivationSchedule, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(IMAGEACTIVATIONSCHEDULE)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, imageActivationSchedule, not found in objectlist (IOL)!");
	}

	public int getIPv4SetupSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, ipv4Setup, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(IPV4SETUP)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	public UniversalObject getP3ImageTransfer(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, P3ImageTransfer, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(P3IMAGETRANSFER)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, P3ImageTransfer, not found in objectlist (IOL)!");
	}


	public int getP3ImageTransferSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, P3ImageTransfer, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(P3IMAGETRANSFER)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	public UniversalObject getConsumerMessageText(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, ConsumerMessageText, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(CONSUMERMESSAGETEXT)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, ConsumerMessageText, not found in objectlist (IOL)!");
	}

	public int getConsumerMessageTextSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, ConsumerMessageText, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(CONSUMERMESSAGETEXT)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	public UniversalObject getConsumerMessageCode(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, ConsumerMessageCode, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(CONSUMERMESSAGECODE)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, ConsumerMessageCode, not found in objectlist (IOL)!");
	}

	public int getConsumerMessageCodeSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, ConsumerMessageCode, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(CONSUMERMESSAGECODE)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	public UniversalObject getDisconnector(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, Disconnector, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(DISCONNECTOR)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, Disconnector, not found in objectlist (IOL)!");
	}

	public int getDisconnectorSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, DisconnectorSN, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(DISCONNECTOR)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	public UniversalObject getDisconnectControlSchedule(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, DisconnectSchedule, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(DISCONNECTCONTROLSCHEDULE)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, DisconnectSchedule, not found in objectlist (IOL)!");
	}

	public UniversalObject getDisconnectorScriptTable(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, DisconnectorScriptTable, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(DISCONNECTORSCRIPTTABLE)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, DisconnectorScriptTable, not found in objectlist (IOL)!");
	}

	public UniversalObject getTariffScriptTable(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getTariffScriptTable, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(TARIFFSCRIPTTABLE)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, getTariffScriptTable, not found in objectlist (IOL)!");
	}

	public UniversalObject getActivityCalendar(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, getActivityCalendar, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(ACTIVITYCALENDAR)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, getActivityCalendar, not found in objectlist (IOL)!");
	}

	public UniversalObject getSpecialDaysTable(UniversalObject[] objectList) throws NotInObjectListException{
        checkEmptyObjectList(objectList, "DLMSConfig, getSpecialDaysTable, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(SPECIALDAYS)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, getSpecialDaysTable, not found in objectlist (IOL)!");
	}

	public int getDisconnectorScriptTableSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, DisconnectorScriptTableSN, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(DISCONNECTORSCRIPTTABLE)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	public UniversalObject getLimiter(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, Limiter, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(LIMITER)) {
				return objectList[i];
			}
		}
        throw new NotInObjectListException("DLMSConfig, Limiter, not found in objectlist (IOL)!");
	}

	public int getLimiterSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, Limiter, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(LIMITER)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	public int getPPPSetupSN(UniversalObject[] objectList) throws NotInObjectListException{
        checkEmptyObjectList(objectList, "DLMSConfig, PPPSetup, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(PPPSETUP)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

	public int getGPRSModemSetupSN(UniversalObject[] objectList) throws NotInObjectListException{
        checkEmptyObjectList(objectList, "DLMSConfig, GPRSModemSetup, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(GPRSMODEMSETUP)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}


	public int getUSBSetupSN(UniversalObject[] objectList) throws NotInObjectListException{
		checkEmptyObjectList(objectList, "DLMSConfig, USBSetup, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(USBSETUP)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
	}

    public int getImageTransferSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, ImageTransfer, objectlist empty!");
		for (int i=0;i<objectList.length;i++) {
			if (objectList[i].equals(P3IMAGETRANSFER)) {
				return objectList[i].getBaseName();
			}
		}
		return 0;
    }

    public int getSFSKPhyMacSetupSN(UniversalObject[] objectList) throws NotInObjectListException {
        checkEmptyObjectList(objectList, "DLMSConfig, SFSKPhyMacSetupSN, objectlist empty!");
        for (int i = 0; i < objectList.length; i++) {
            if (objectList[i].equals(SFSKPhyMacSetupSN)) {
                return objectList[i].getBaseName();
            }
        }
        return 0;
    }
}
