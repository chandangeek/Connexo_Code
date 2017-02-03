/*
 * DLMSMeterConfig.java
 *
 * Created on 4 april 2003, 15:52
 * Changes:
 * KV 14072004 DLMSMeterConfig made multithreaded! singleton pattern implementation removed!
 */

package com.energyict.dlms;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

/**
 * @author Koen
 */
public class DLMSMeterConfig {

    /**
     * String indicator for the ActionResponse Fix
     */
    public final static String OLD = "OLD";
    /**
     * String indicator for the encrypted HLS step 3 and 4 Fix
     */
    public final static String OLD2 = "OLD2";

    /**
     * A regular Expression for the splitting of the {@link #manuf} string
     */
    private static String splitter = "::";
    private static DLMSConfig config = DLMSConfig.getInstance();
    private UniversalObject[] IOL = null;
    /**
     * @return Value of property COL.
     * @deprecated As of 12022009
     * Getter for property COL.
     */
    private UniversalObject[] COL = null;
    /**
     * A Manufacturer identification String
     */
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

    /**
     * Creates a new instance of DLMSMeterConfig
     */
    private DLMSMeterConfig(String manuf) {
        if (manuf != null) {
            String[] manufSplit = manuf.split(splitter);
            setManufacturer(manufSplit[0]);
            if (manufSplit.length == 2) {
                setExtra(manufSplit[1]);
            } else {
                setExtra("");
            }
        } else {
            setManufacturer(manuf);
            setExtra("");
        }
    }

    public static DLMSMeterConfig getInstance() {
        return getInstance(null);
    }

    /**
     * @param manuf - a Manufacturer specific identification code
     * @return a static DLMSMeterConfig
     */
    public static DLMSMeterConfig getInstance(String manuf) {
        return new DLMSMeterConfig(manuf);
    }

    public void reportIOL() {
        for (int i = 0; i < IOL.length; i++) {
            System.out.println(IOL[i].toString());
        }
    }

    /**
     * @return true if the Manufacturer is ActarisPLCC
     */
    public boolean isActarisPLCC() {
        if (manuf == null) {
            return false;
        } else {
            return "ActarisPLCC".compareTo(manuf) == 0;
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
        if (manuf == null) {
            return false;
        } else {
            return ("ISK".compareTo(manuf) == 0) || ("WKP".compareTo(manuf) == 0);
        }
    }

    public UniversalObject findObject(ObisCode obisCode) throws NotInObjectListException {
        checkEmptyObjectList("DLMSMeterConfig, getSN, IOL empty!");
        for (int i = 0; i < IOL.length; i++) {
            if (IOL[i].equals(obisCode)) {
                return IOL[i];
            }
        }
        throw new NotInObjectListException("DLMSMeterConfig, findObject, " + obisCode.toString() + " not found in meter's instantiated object list!");
    }

    /**
     * Find the {@link ObisCode} in the {@link UniversalObject} list of the
     * meter, and return the BaseName (SN) of the DLMS object
     *
     * @param obisCode
     * @return
     * @throws NotInObjectListException
     */
    public int getSN(ObisCode obisCode) throws NotInObjectListException {
        checkEmptyObjectList("DLMSMeterConfig, getSN, IOL empty!");
        for (int i = 0; i < IOL.length; i++) {
            if (IOL[i].equals(obisCode)) {
                return IOL[i].getBaseName();
            }
        }
        throw new NotInObjectListException("DLMSMeterConfig, getSN, " + obisCode.toString() + " not found in meter's instantiated object list!");
    }


    private void checkEmptyObjectList(String msg) {
        if (IOL == null) {
            ProtocolException protocolException = new ProtocolException(msg);
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        }
    }

    public int getClassId(ObisCode obisCode) throws NotInObjectListException {
        checkEmptyObjectList("DLMSMeterConfig, getSN, IOL empty!");
        for (int i = 0; i < IOL.length; i++) {
            if (IOL[i].equals(obisCode)) {
                int classId = IOL[i].getClassID();
                return classId;
            }
        }
        throw new NotInObjectListException("DLMSMeterConfig, getClassId, " + obisCode.toString() + " not found in meter's instantiated object list!");
    }

    public int getProfileSN() throws NotInObjectListException {
        return config.getProfileSN(IOL);
    }

    public int getEventLogSN() throws NotInObjectListException {
        return config.getEventLogSN(IOL);
    }

    public int getHistoricValuesSN() throws NotInObjectListException {
        return config.getHistoricValuesSN(IOL);
    }

    public int getResetCounterSN() throws NotInObjectListException {
        return config.getResetCounterSN(IOL);
    }

    public int getClockSN() throws NotInObjectListException {
        return config.getClockSN(IOL);
    }

    public int getConfigSN() throws NotInObjectListException {
        return config.getConfigSN(IOL, manuf);
    }

    public int getVersionSN() throws NotInObjectListException {
        return config.getVersionSN(IOL, manuf);
    }

    public int getSerialNumberSN() throws NotInObjectListException {
        return config.getSerialNumberSN(IOL, manuf);
    }

    public int getIPv4SetupSN() throws NotInObjectListException {
        return config.getIPv4SetupSN(IOL);
    }

    public int getP3ImageTransferSN() throws NotInObjectListException {
        return config.getP3ImageTransferSN(IOL);
    }

    public int getDisconnectorSN() throws NotInObjectListException {
        return config.getDisconnectorSN(IOL);
    }

    public int getDisconnectorScriptTableSN() throws NotInObjectListException {
        return config.getDisconnectorScriptTableSN(IOL);
    }

    public int getLimiterSN() throws NotInObjectListException {
        return config.getLimiterSN(IOL);
    }

    public int getPPPSetupSN() throws NotInObjectListException {
        return config.getPPPSetupSN(IOL);
    }

    public int getGPRSModemSetupSN() throws NotInObjectListException {
        return config.getGPRSModemSetupSN(IOL);
    }

    public int getUSBSetupSN() throws NotInObjectListException {
        return config.getUSBSetupSN(IOL);
    }

    public UniversalObject getEventLogObject() throws NotInObjectListException {
        return config.getEventLogObject(IOL, manuf);
    }

    public UniversalObject getControlLogObject() throws NotInObjectListException {
        return config.getControlLog(IOL, manuf);
    }

    public UniversalObject getPowerFailureLogObject() throws NotInObjectListException {
        return config.getPowerFailureLog(IOL, manuf);
    }

    public UniversalObject getFraudDetectionLogObject() throws NotInObjectListException {
        return config.getFraudDetectionLog(IOL, manuf);
    }

    public UniversalObject getMbusEventLogObject() throws NotInObjectListException {
        return config.getMbusEventLog(IOL, manuf);
    }

    public UniversalObject getProfileObject() throws NotInObjectListException {
        return config.getProfileObject(IOL);
    }

    public UniversalObject getDailyProfileObject() throws NotInObjectListException {
        return config.getDailyProfileObject(IOL, manuf);
    }

    public UniversalObject getMonthlyProfileObject() throws NotInObjectListException {
        return config.getMonthlyProfileObject(IOL, manuf);
    }

    public UniversalObject getClockObject() throws NotInObjectListException {
        return config.getClockObject(IOL);
    }

    public UniversalObject getStatusObject() throws NotInObjectListException {
        return config.getStatusObject(IOL, manuf);
    }

    public UniversalObject getConfigObject() throws NotInObjectListException {
        return config.getConfigObject(IOL, manuf);
    }

    public UniversalObject getVersionObject() throws NotInObjectListException {
        return config.getVersionObject(IOL, manuf);
    }

    public UniversalObject getSerialNumberObject() throws NotInObjectListException {
        return config.getSerialNumberObject(IOL, manuf);
    }

    public UniversalObject getIPv4SetupObject() throws NotInObjectListException {
        return config.getIPv4SetupObject(IOL);
    }

    public UniversalObject getObject(DLMSObis dlmsObis) throws NotInObjectListException {
        checkEmptyObjectList("DLMSMeterConfig, objectlist (IOL) empty!");
        for (int i = 0; i < IOL.length; i++) {
            if (IOL[i].equals(dlmsObis)) {
                return IOL[i];
            }
        }
        throw new NotInObjectListException("DLMSMeterConfig, dlmsObis " + dlmsObis + " not found in objectlist (IOL)!");
    }

    public UniversalObject getMeterReadingObject(int id, String deviceId) throws NotInObjectListException {
        return config.getMeterReadingObject(IOL, id, deviceId);
    }

    /**
     * @deprecated As of 12022009
     */
    public UniversalObject getChannelObject(int id) throws NotInObjectListException {
        int count = 0;
        for (int i = 0; i < COL.length; i++) {
            if (COL[i].isCapturedObjectNotAbstract()) {
                if (id == count) {
                    return COL[i];
                }
                count++;
            }
        }
        throw new NotInObjectListException("DLMSMeterConfig, getMeterDemandObject(" + id + "), not found in objectlist (IOL)!");
    }


    /**
     * @deprecated As of 12022009
     */
    public UniversalObject getMeterDemandObject(int id) throws NotInObjectListException {
        int count = 0;
        for (int i = 0; i < COL.length; i++) {
            // Changed KV 23022007 to allow also gas and water captured objects
            //if (COL[i].isCapturedObjectElectricity()) {
            if (COL[i].isCapturedObjectNotAbstract()) {
                if (id == count) {
                    for (int t = 0; t < IOL.length; t++) {
                        if (IOL[t].equals(COL[i])) {
                            return IOL[t];
                        }
                    }
                }
                count++;
            }
        }
        throw new NotInObjectListException("DLMSMeterConfig, getMeterDemandObject(" + id + "), not found in objectlist (IOL)!");
    }

    /**
     * @deprecated As of 12022009
     */
    public int getNumberOfChannels() throws NotInObjectListException {
        int count = 0;
        for (int i = 0; i < COL.length; i++) {
            if (COL[i].isCapturedObjectNotAbstract()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Getter for property IOL.
     *
     * @return Value of property IOL.
     */
    public UniversalObject[] getInstantiatedObjectList() {
        return this.IOL;
    }

    public void setInstantiatedObjectList(UniversalObject[] IOL) {
        this.IOL = IOL;
    }

    /**
     * @return Value of property COL.
     * @deprecated As of 12022009
     * Getter for property COL.
     */
    public UniversalObject[] getCapturedObjectList() {
        return this.COL;
    }

    /**
     * @deprecated As of 12022009, use COSEM class com.energyict.dlms.cosem.ProfileGeneric and com.energyict.dlms.cosem.CaptureObjectsHelper
     */
    public void setCapturedObjectList(UniversalObject[] COL) {
        this.COL = COL;
    }

    public UniversalObject getMbusDisconnectControl(int physicalAddress) throws NotInObjectListException {
        return config.getMbusDisconnector(IOL, manuf, physicalAddress);
    }

    public UniversalObject getMbusControlLog(int physicalAddress) throws NotInObjectListException {
        return config.getMbusControlLog(IOL, manuf, physicalAddress);
    }

    public UniversalObject getMbusDisconnectControlState(int physicalAddress) throws NotInObjectListException {
        return config.getMbusDisconnectControlState(IOL, manuf, physicalAddress);
    }

    public UniversalObject getMbusSerialNumber(int physicalAddress) throws NotInObjectListException {
        return config.getMbusSerialNumber(IOL, manuf, physicalAddress);
    }

    public UniversalObject getMbusProfile(int physicalAddress) throws NotInObjectListException {
        return config.getMbusProfile(IOL, manuf, physicalAddress);
    }

    public UniversalObject getXMLConfig() throws NotInObjectListException {
        return config.getXMLConfig(IOL, manuf);
    }

    public UniversalObject getImageActivationSchedule() throws NotInObjectListException {
        return config.getImageActivationSchedule(IOL);
    }

    public UniversalObject getDisconnectControlSchedule() throws NotInObjectListException {
        return config.getDisconnectControlSchedule(IOL);
    }

    public UniversalObject getMbusStatusObject(int physicalAddress) throws NotInObjectListException {
        return config.getMbusStatusObject(IOL, manuf, physicalAddress);
    }

    public UniversalObject getP3ImageTransfer() throws NotInObjectListException {
        return config.getP3ImageTransfer(IOL);
    }

    public UniversalObject getConsumerMessageText() throws NotInObjectListException {
        return config.getConsumerMessageText(IOL);
    }

    public UniversalObject getConsumerMessageCode() throws NotInObjectListException {
        return config.getConsumerMessageCode(IOL);
    }

    public UniversalObject getDisconnector() throws NotInObjectListException {
        return config.getDisconnector(IOL);
    }

    public UniversalObject getDisconnectorScriptTable() throws NotInObjectListException {
        return config.getDisconnectorScriptTable(IOL);
    }

    public UniversalObject getTariffScriptTable() throws NotInObjectListException {
        return config.getTariffScriptTable(IOL);
    }

    public UniversalObject getActivityCalendar() throws NotInObjectListException {
        return config.getActivityCalendar(IOL);
    }

    public UniversalObject getMbusDisconnectorScriptTable(int physicalAddress) throws NotInObjectListException {
        return config.getMbusDisconnectorScriptTable(IOL, manuf, physicalAddress);
    }

    public UniversalObject getMbusDisconnectControlSchedule(int physicalAddress) throws NotInObjectListException {
        return config.getMbusDisconnectControlSchedule(IOL, manuf, physicalAddress);
    }

    public UniversalObject getLimiter() throws NotInObjectListException {
        return config.getLimiter(IOL);
    }

    public UniversalObject getSpecialDaysTable() throws NotInObjectListException {
        return config.getSpecialDaysTable(IOL);
    }

    /**
     * Returns the MBus client for a provided 0-based physicalAddress
     *
     **/
    public UniversalObject getMbusClient(int physicalAddress) throws NotInObjectListException {
        return config.getMbusClient(IOL, manuf, physicalAddress);
    }

    /**
     * Check if a given obisCode exists in the object list of the device
     *
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
     * Check if a given obisCode exists in the object list of the device ignoring the B Channel
     *
     * @param obisCode
     * @return
     */
    public boolean isObisCodeInObjectListIgnoreChannelB(ObisCode obisCode) {
        UniversalObject[] objectList = getInstantiatedObjectList();
        if (objectList != null) {
            for (int i = 0; i < objectList.length; i++) {
                UniversalObject universalObject = objectList[i];
                if (universalObject != null && universalObject.getObisCode().equalsIgnoreBChannel(obisCode)) {
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

    public int getImageTransferSN() throws NotInObjectListException {
        return config.getImageTransferSN(IOL);
    }

    public int getSFSKPhyMacSetupSN() throws NotInObjectListException {
        return config.getSFSKPhyMacSetupSN(IOL);
    }
}
