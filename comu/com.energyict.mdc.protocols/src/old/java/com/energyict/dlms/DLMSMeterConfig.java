package com.energyict.dlms;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
/**
 *
 * @author  Koen
 */
public class DLMSMeterConfig {

    /** String indicator for the ActionResponse Fix */
    public final static String OLD = "OLD";
    /** String indicator for the encrypted HLS step 3 and 4 Fix */
    public final static String OLD2  = "OLD2";

    /** A regular Expression for the splitting of the {@link #manuf} string*/
	private static String splitter = "::";

    private UniversalObject[] IOL=null;

    /**
     * @deprecated  As of 12022009
     * Getter for property COL.
     * @return Value of property COL.
     */
    private UniversalObject[] COL=null;

    /** A Manufacturer identification String */
    private String manuf;

    /**
     * Extra info from the Manufacturer String.<br>
     * This value was added because originally there were errors in our DLMS implementation. By using the <i>extra</i> parameter, it is possible to still use the
     * incorrect DLMS implementation for older meters (specifically older NTA meters).
     * <br> <br>
     * Currently there are 2 values defined:
     * <li> <b>{@link #OLD}</b> 	: this is used for parsing the ActionResponse. Originally we skipped one byte
     * <li> <b>{@link #OLD2}</b> 	: this is used in the HLS association. Originally step 3 and 4 of the authentication were not encrypted if an encrypted session was proposed.
     */
    private String extra;

    private static DLMSConfig config = DLMSConfig.getInstance();

    public static DLMSMeterConfig getInstance() {
        return getInstance(null);
    }

    /**
     * @param manuf
     *            - a Manufacturer specific identification code
     * @return a static DLMSMeterConfig
     */
    public static DLMSMeterConfig getInstance(String manuf) {
        return new DLMSMeterConfig(manuf);
    }

    public void reportIOL() {
        for (int i=0;i<IOL.length;i++) {
            System.out.println(IOL[i].toString());
        }
    }

    /**
     * @return true if the Manufacturer is ActarisPLCC
     */
    public boolean isActarisPLCC() {
        if (manuf==null) {
			return false;
		} else {
			return "ActarisPLCC".compareTo(manuf)==0;
		}
    }

    /**
     * @return true if the Manufacturer is SLB
     */
    public boolean isSLB() {
        if (manuf == null) {
            return false;
        } else {
            return "SLB".compareTo(manuf) == 0;
        }
    }

    /**
     * @return true if the Manufacturer is SL7000
     */
    public boolean isSL7000() {
        if (manuf == null) {
            return false;
        } else {
            return (("SLB".compareTo(manuf) == 0) && ("SL7000".compareTo(extra) == 0));
        }
    }

    /**
     * @return true if the Manufacturer is ISK (or WKP)
     */
    public boolean isIskra() {
        if (manuf==null) {
			return false;
		} else {
			return ("ISK".compareTo(manuf)==0)||("WKP".compareTo(manuf)==0);
		}
    }

    /** Creates a new instance of DLMSMeterConfig */
    private DLMSMeterConfig(String manuf) {
    	if(manuf != null){
    		String[] manufSplit = manuf.split(splitter);
    		setManufacturer(manufSplit[0]);
    		if(manufSplit.length == 2){
    			setExtra(manufSplit[1]);
    		} else {
    			setExtra("");
    		}
    	} else {
    		setManufacturer(manuf);
    		setExtra("");
    	}
    }

    public UniversalObject findObject(ObisCode obisCode) throws ProtocolException {
        if (IOL == null) {
			throw new ProtocolException("DLMSMeterConfig, getSN, IOL empty!");
		}
        for (int i=0;i<IOL.length;i++) {
            if (IOL[i].equals(obisCode)) {
				return IOL[i];
			}
        }
       throw new NoSuchRegisterException("DLMSMeterConfig, findObject, "+obisCode.toString()+" not found in meter's instantiated object list!");
    }

	/**
	 * Find the {@link ObisCode} in the {@link UniversalObject} list of the
	 * meter, and return the BaseName (SN) of the DLMS object
	 *
	 * @param obisCode
	 * @return
	 * @throws ProtocolException
	 */
	public int getSN(ObisCode obisCode) throws ProtocolException {
		if (IOL == null) {
			throw new ProtocolException("DLMSMeterConfig, getSN, IOL empty!");
		}
		for (int i = 0; i < IOL.length; i++) {
			if (IOL[i].equals(obisCode)) {
				return IOL[i].getBaseName();
			}
		}
		throw new NoSuchRegisterException("DLMSMeterConfig, getSN, " + obisCode.toString() + " not found in meter's instantiated object list!");
	}

    public int getClassId(ObisCode obisCode) throws ProtocolException {
       if (IOL == null) {
		throw new ProtocolException("DLMSMeterConfig, getSN, IOL empty!");
	}
       for (int i=0;i<IOL.length;i++) {
           if (IOL[i].equals(obisCode)) {
               int classId = IOL[i].getClassID();
               return classId;
           }
       }
       throw new NoSuchRegisterException("DLMSMeterConfig, getClassId, "+obisCode.toString()+" not found in meter's instantiated object list!");
    }

    public int getProfileSN() throws ProtocolException {
       return config.getProfileSN(IOL);
    }

    public int getEventLogSN() throws ProtocolException {
       return config.getEventLogSN(IOL);
    }

    public int getHistoricValuesSN() throws ProtocolException {
       return config.getHistoricValuesSN(IOL);
    }

    public int getResetCounterSN() throws ProtocolException {
       return config.getResetCounterSN(IOL);
    }

    public int getClockSN() throws ProtocolException {
       return config.getClockSN(IOL);
    }

    public int getConfigSN() throws ProtocolException {
       return config.getConfigSN(IOL,manuf);
    }

    public int getVersionSN() throws ProtocolException {
       return config.getVersionSN(IOL,manuf);
    }
    public int getSerialNumberSN() throws ProtocolException {
       return config.getSerialNumberSN(IOL,manuf);
    }

	public int getIPv4SetupSN() throws ProtocolException {
		return config.getIPv4SetupSN(IOL);
	}

	public int getIPv6SetupSN() throws ProtocolException {
		return config.getIPv6SetupSN(IOL);
	}

	public int getP3ImageTransferSN() throws ProtocolException {
		return config.getP3ImageTransferSN(IOL);
	}

	public int getDisconnectorSN() throws ProtocolException{
		return config.getDisconnectorSN(IOL);
	}

	public int getDisconnectorScriptTableSN() throws ProtocolException{
		return config.getDisconnectorScriptTableSN(IOL);
	}

	public int getLimiterSN() throws ProtocolException{
		return config.getLimiterSN(IOL);
	}

	public int getPPPSetupSN() throws ProtocolException{
		return config.getPPPSetupSN(IOL);
	}

	public int getGPRSModemSetupSN() throws ProtocolException{
		return config.getGPRSModemSetupSN(IOL);
	}

    public UniversalObject getEventLogObject() throws ProtocolException {
       return config.getEventLogObject(IOL, manuf);
    }

    public UniversalObject getControlLogObject() throws ProtocolException {
        return config.getControlLog(IOL, manuf);
    }

    public UniversalObject getPowerFailureLogObject() throws ProtocolException {
        return config.getPowerFailureLog(IOL, manuf);
    }

    public UniversalObject getFraudDetectionLogObject() throws ProtocolException {
        return config.getFraudDetectionLog(IOL, manuf);
    }

    public UniversalObject getMbusEventLogObject() throws ProtocolException {
        return config.getMbusEventLog(IOL, manuf);
    }

    public UniversalObject getProfileObject() throws ProtocolException {
       return config.getProfileObject(IOL);
    }

    public UniversalObject getDailyProfileObject() throws ProtocolException {
    	return config.getDailyProfileObject(IOL,manuf);
    }

    public UniversalObject getMonthlyProfileObject() throws ProtocolException {
    	return config.getMonthlyProfileObject(IOL,manuf);
    }

    public UniversalObject getClockObject() throws ProtocolException {
       return config.getClockObject(IOL);
    }

    public UniversalObject getStatusObject() throws ProtocolException {
    	return config.getStatusObject(IOL,manuf);
    }

    public UniversalObject getConfigObject() throws ProtocolException {
       return config.getConfigObject(IOL,manuf);
    }

    public UniversalObject getVersionObject() throws ProtocolException {
       return config.getVersionObject(IOL,manuf);
    }

    public UniversalObject getSerialNumberObject() throws ProtocolException {
       return config.getSerialNumberObject(IOL,manuf);
    }

    public UniversalObject getIPv4SetupObject() throws ProtocolException {
    	return config.getIPv4SetupObject(IOL);
    }

    public UniversalObject getObject(DLMSObis dlmsObis) throws ProtocolException {
       if (IOL == null) {
		throw new ProtocolException("DLMSMeterConfig, objectlist (IOL) empty!");
	}
       for (int i=0;i<IOL.length;i++) {
           if (IOL[i].equals(dlmsObis)) {
			return IOL[i];
		}
       }
       throw new ProtocolException("DLMSMeterConfig, dlmsObis "+dlmsObis+" not found in objectlist (IOL)!");
    }

    public UniversalObject getMeterReadingObject(int id,String deviceId) throws ProtocolException {
       return config.getMeterReadingObject(IOL,id,deviceId);
    }

    /**
     * @deprecated  As of 12022009
     */
    public UniversalObject getChannelObject(int id) throws ProtocolException {
       int count=0;
       for (int i=0;i<COL.length;i++) {
           if (COL[i].isCapturedObjectNotAbstract()) {
               if (id==count) {
				return COL[i];
			}
               count++;
           }
       }
       throw new ProtocolException("DLMSMeterConfig, getMeterDemandObject("+id+"), not found in objectlist (IOL)!");
    }


    /**
     * @deprecated  As of 12022009
     */
    public UniversalObject getMeterDemandObject(int id) throws ProtocolException {
       int count=0;
       for (int i=0;i<COL.length;i++) {
           // Changed KV 23022007 to allow also gas and water captured objects
           //if (COL[i].isCapturedObjectElectricity()) {
           if (COL[i].isCapturedObjectNotAbstract()) {
               if (id == count) {
                   for (int t=0;t<IOL.length;t++) {
                      if (IOL[t].equals(COL[i])) {
						return IOL[t];
					}
                   }
               }
               count++;
           }
       }
       throw new ProtocolException("DLMSMeterConfig, getMeterDemandObject("+id+"), not found in objectlist (IOL)!");
    }

    /**
     * @deprecated  As of 12022009
     */
    public int getNumberOfChannels() throws ProtocolException {
       int count=0;
       for (int i=0;i<COL.length;i++) {
           if (COL[i].isCapturedObjectNotAbstract()) {
               count++;
           }
       }
       return count;
    }

    /**
     * Getter for property IOL.
     * @return Value of property IOL.
     */
    public UniversalObject[] getInstantiatedObjectList() {
        return this.IOL;
    }

    public void setInstantiatedObjectList(UniversalObject[] IOL) {
        this.IOL=IOL;
    }

    /**
     * @deprecated  As of 12022009
     * Getter for property COL.
     * @return Value of property COL.
     */
    public UniversalObject[] getCapturedObjectList() {
        return this.COL;
    }
    /**
     * @deprecated  As of 12022009, use COSEM class ProfileGeneric and com.energyict.dlms.cosem.CaptureObjectsHelper
     *
     */
    public void setCapturedObjectList(UniversalObject[] COL) {
        this.COL=COL;
    }

	public UniversalObject getMbusDisconnectControl(int physicalAddress) throws ProtocolException {
		return config.getMbusDisconnector(IOL, manuf, physicalAddress);
	}

	public UniversalObject getMbusControlLog(int physicalAddress) throws ProtocolException {
		return config.getMbusControlLog(IOL, manuf, physicalAddress);
	}

	public UniversalObject getMbusDisconnectControlState(int physicalAddress) throws ProtocolException {
		return config.getMbusDisconnectControlState(IOL, manuf, physicalAddress);
	}

	public UniversalObject getMbusSerialNumber(int physicalAddress) throws ProtocolException {
		return config.getMbusSerialNumber(IOL, manuf, physicalAddress);
	}

	public UniversalObject getMbusProfile(int physicalAddress) throws ProtocolException {
		return config.getMbusProfile(IOL, manuf, physicalAddress);
	}

	public UniversalObject getXMLConfig() throws ProtocolException {
		return config.getXMLConfig(IOL,manuf);
	}

	public UniversalObject getImageActivationSchedule() throws ProtocolException{
		return config.getImageActivationSchedule(IOL);
	}

	public UniversalObject getDisconnectControlSchedule() throws ProtocolException{
		return config.getDisconnectControlSchedule(IOL);
	}

	public UniversalObject getMbusStatusObject(int physicalAddress) throws ProtocolException{
		return config.getMbusStatusObject(IOL, manuf, physicalAddress);
	}

	public UniversalObject getP3ImageTransfer() throws ProtocolException{
		return config.getP3ImageTransfer(IOL);
	}

	public UniversalObject getConsumerMessageText() throws ProtocolException {
		return config.getConsumerMessageText(IOL);
	}

	public UniversalObject getConsumerMessageCode() throws ProtocolException {
		return config.getConsumerMessageCode(IOL);
	}

	public UniversalObject getDisconnector() throws ProtocolException{
		return config.getDisconnector(IOL);
	}

	public UniversalObject getDisconnectorScriptTable() throws ProtocolException{
		return config.getDisconnectorScriptTable(IOL);
	}

	public UniversalObject getTariffScriptTable() throws ProtocolException{
		return config.getTariffScriptTable(IOL);
	}

	public UniversalObject getActivityCalendar() throws ProtocolException{
		return config.getActivityCalendar(IOL);
	}

	public UniversalObject getMbusDisconnectorScriptTable(int physicalAddress) throws ProtocolException {
		return config.getMbusDisconnectorScriptTable(IOL, manuf, physicalAddress);
	}

	public UniversalObject getMbusDisconnectControlSchedule(int physicalAddress) throws ProtocolException {
		return config.getMbusDisconnectControlSchedule(IOL, manuf, physicalAddress);
	}

	public UniversalObject getLimiter() throws ProtocolException{
		return config.getLimiter(IOL);
	}

	public UniversalObject getSpecialDaysTable() throws ProtocolException{
		return config.getSpecialDaysTable(IOL);
	}

	public UniversalObject getMbusClient(int physicalAddress) throws ProtocolException{
		return config.getMbusClient(IOL, manuf, physicalAddress);
	}

    /**
     * Check if a given obisCode exists in the object list of the device
     * @param obisCode
     * @return
     */
    public boolean isObisCodeInObjectList(ObisCode obisCode) {
        UniversalObject[] objectList = getInstantiatedObjectList();
        if (objectList != null) {
            for (int i = 0; i < objectList.length; i++) {
                UniversalObject universalObject = objectList[i];
                if (universalObject != null && universalObject.getObisCode().equals(obisCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the DLMSClassId for a given obisCode from the objectLst.
     * Return DLMSClassId.UNKNOWN is not found.
     *
     * @param obisCode
     * @return
     */
    public DLMSClassId getDLMSClassId(ObisCode obisCode) {
        for (UniversalObject uo : getInstantiatedObjectList()) {
            if (uo.getObisCode().equals(obisCode)) {
                return uo.getDLMSClassId();
            }
        }
        return DLMSClassId.UNKNOWN;
    }

    /**
	 * @param manuf the manuf to set
	 */
	protected void setManufacturer(String manuf) {
		this.manuf = manuf;
	}

	/**
     * @return the {@link #extra}
	 */
	public String getExtra() {
		return extra;
	}

	/**
     * @param extra the extra to set
	 */
	protected void setExtra(String extra) {
		this.extra = extra;
	}

    public int getImageTransferSN() throws ProtocolException {
       return config.getImageTransferSN(IOL);
    }

    public int getSFSKPhyMacSetupSN() throws ProtocolException {
        return config.getSFSKPhyMacSetupSN(IOL);
}
}
