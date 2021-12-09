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
import com.energyict.protocol.exception.ConnectionCommunicationException;

/**
 * @author Koen
 */
public class DLMSMeterConfig {

    /**
     * String indicator for the ActionResponse Fix
     */
    @SuppressWarnings("unused")
    public final static String OLD = "OLD";
    /**
     * String indicator for the encrypted HLS step 3 and 4 Fix
     */
    public final static String OLD2 = "OLD2";

    /**
     * A regular Expression for the splitting of the {@link #manuf} string
     */
    private static final String splitter = "::";
    private static final DLMSConfig config = DLMSConfig.getInstance();
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
            setManufacturer(null);
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

    @SuppressWarnings("unused")
    public void reportIOL() {
        for (UniversalObject universalObject : IOL) {
            System.out.println(universalObject.toString());
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
     * @return true if the Manufacturer is ELS::AS3000
     */
    public boolean isAS3000() {
        if (manuf == null) {
            return false;
        } else {
            return (("ELS".compareTo(manuf) == 0) && ("AS3000".compareTo(extra) == 0));
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
        for (UniversalObject universalObject : IOL) {
            if (universalObject.equals(obisCode)) {
                return universalObject;
            }
        }
        throw new NotInObjectListException("DLMSMeterConfig, findObject, " + obisCode.toString() + " not found in meter's instantiated object list!");
    }

    /**
     * Find the {@link ObisCode} in the {@link UniversalObject} list of the
     * meter, and return the BaseName (SN) of the DLMS object
     *
     * @param obisCode the ObisCode to check
     * @return int the SN for the given ObisCode
     * @throws NotInObjectListException when the ObisCode is not listed in the IOL
     */
    public int getSN(ObisCode obisCode) throws NotInObjectListException {
        checkEmptyObjectList("DLMSMeterConfig, getSN, IOL empty!");
        for (UniversalObject universalObject : IOL) {
            if (universalObject.equals(obisCode)) {
                return universalObject.getBaseName();
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
        for (UniversalObject universalObject : IOL) {
            if (universalObject.equals(obisCode)) {
                return universalObject.getClassID();
            }
        }
        throw new NotInObjectListException("DLMSMeterConfig, getClassId, " + obisCode.toString() + " not found in meter's instantiated object list!");
    }

    public int getProfileSN() {
        return config.getProfileSN(IOL);
    }

    @SuppressWarnings("unused")
    public int getEventLogSN() {
        return config.getEventLogSN(IOL, manuf);
    }

    public int getHistoricValuesSN() {
        return config.getHistoricValuesSN(IOL);
    }

    public int getResetCounterSN() {
        return config.getResetCounterSN(IOL);
    }

    public int getClockSN() {
        return config.getClockSN(IOL);
    }

    @SuppressWarnings("unused")
    public int getConfigSN() {
        return config.getConfigSN(IOL, manuf);
    }

    @SuppressWarnings("unused")
    public int getVersionSN() {
        return config.getVersionSN(IOL, manuf);
    }

    @SuppressWarnings("unused")
    public int getSerialNumberSN() {
        return config.getSerialNumberSN(IOL, manuf);
    }

    public int getIPv4SetupSN() {
        return config.getIPv4SetupSN(IOL);
    }

    public int getP3ImageTransferSN() {
        return config.getP3ImageTransferSN(IOL);
    }

    public int getDisconnectorSN() {
        return config.getDisconnectorSN(IOL);
    }

    @SuppressWarnings("unused")
    public int getDisconnectorScriptTableSN() {
        return config.getDisconnectorScriptTableSN(IOL);
    }

    public int getLimiterSN() {
        return config.getLimiterSN(IOL);
    }

    public int getPPPSetupSN() {
        return config.getPPPSetupSN(IOL);
    }

    public int getGPRSModemSetupSN() {
        return config.getGPRSModemSetupSN(IOL);
    }

    public int getNBIOTModemSetupSN() {
        return config.getNBIOTModemSetupSN(IOL);
    }

    public int getLTEModemSetupSN() {
        return config.getLTEModemSetupSN(IOL);
    }

    @SuppressWarnings("unused")
    public int getUSBSetupSN() {
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

    @SuppressWarnings("unused")
    public UniversalObject getCommunicationSessionLogObject() throws NotInObjectListException {
        return config.getCommunicationSessionLog(IOL, manuf);
    }

    @SuppressWarnings("unused")
    public UniversalObject getVoltageQualityLogObject() throws NotInObjectListException {
        return config.getVoltageQualityLog(IOL, manuf);
    }

    public UniversalObject getFraudDetectionLogObject() throws NotInObjectListException {
        return config.getFraudDetectionLog(IOL, manuf);
    }

    public UniversalObject getMbusEventLogObject() throws NotInObjectListException {
        return config.getMbusEventLog(IOL, manuf);
    }

    @SuppressWarnings("unused")
    public UniversalObject getProfileObject() throws NotInObjectListException {
        return config.getProfileObject(IOL);
    }

    @SuppressWarnings("unused")
    public UniversalObject getDailyProfileObject() throws NotInObjectListException {
        return config.getDailyProfileObject(IOL, manuf);
    }

    @SuppressWarnings("unused")
    public UniversalObject getMonthlyProfileObject() throws NotInObjectListException {
        return config.getMonthlyProfileObject(IOL, manuf);
    }

    public UniversalObject getClockObject() throws NotInObjectListException {
        return config.getClockObject(IOL);
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public UniversalObject getIPv4SetupObject() throws NotInObjectListException {
        return config.getIPv4SetupObject(IOL);
    }

    public UniversalObject getObject(DLMSObis dlmsObis) throws NotInObjectListException {
        checkEmptyObjectList("DLMSMeterConfig, objectlist (IOL) empty!");
        for (UniversalObject universalObject : IOL) {
            if (universalObject.equals(dlmsObis)) {
                return universalObject;
            }
        }
        throw new NotInObjectListException("DLMSMeterConfig, dlmsObis " + dlmsObis + " not found in objectlist (IOL)!");
    }

    @SuppressWarnings("unused")
    public UniversalObject getMeterReadingObject(int id, String deviceId) throws NotInObjectListException {
        return config.getMeterReadingObject(IOL, id, deviceId);
    }

    /**
     * @deprecated As of 12022009
     */
    public UniversalObject getChannelObject(int id) throws NotInObjectListException {
        int count = 0;
        for (UniversalObject universalObject : COL) {
            if (universalObject.isCapturedObjectNotAbstract()) {
                if (id == count) {
                    return universalObject;
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
        for (UniversalObject object : COL) {
            // Changed KV 23022007 to allow also gas and water captured objects
            //if (COL[i].isCapturedObjectElectricity()) {
            if (object.isCapturedObjectNotAbstract()) {
                if (id == count) {
                    for (UniversalObject universalObject : IOL) {
                        if (universalObject.equals(object)) {
                            return universalObject;
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
        for (UniversalObject universalObject : COL) {
            if (universalObject.isCapturedObjectNotAbstract()) {
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public UniversalObject getMbusStatusObject(int physicalAddress) throws NotInObjectListException {
        return config.getMbusStatusObject(IOL, manuf, physicalAddress);
    }

    @SuppressWarnings("unused")
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
    public UniversalObject getMbusClient(int zeroBasedPhysicalAddress) throws NotInObjectListException {
        return config.getMbusClient(IOL, manuf, zeroBasedPhysicalAddress);
    }

    /**
     * Check if a given obisCode exists in the object list of the device
     *
     * @param obisCode the ObisCode to check
     * @return boolean true when the ObisCode exists in the IOL
     */
    public boolean isObisCodeInObjectList(ObisCode obisCode) {
        UniversalObject[] objectList = getInstantiatedObjectList();
        if (objectList != null) {
            for (UniversalObject universalObject : objectList) {
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
     * @param obisCode then ObisCode to check
     * @return boolean true when then ObisCodes exists in the IOL (ignoring the B channel)
     */
    public boolean isObisCodeInObjectListIgnoreChannelB(ObisCode obisCode) {
        UniversalObject[] objectList = getInstantiatedObjectList();
        if (objectList != null) {
            for (UniversalObject universalObject : objectList) {
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
     * @param obisCode the ObisCode to search
     * @return DLMSClassId the corresponding DLMSClassId or DLMSClassId.UNKNOWN when not found
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

    public int getImageTransferSN() {
        return config.getImageTransferSN(IOL);
    }

    public int getSFSKPhyMacSetupSN() {
        return config.getSFSKPhyMacSetupSN(IOL);
    }
}
