package com.energyict.protocolimplv2.nta.abstractnta;


import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.common.composedobjects.ComposedMeterInfo;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.Dsmr23LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr23.registers.Dsmr23RegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.MBusConfigurationObject;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:20:34
 * Modified: 22.11.2018
 * Copied original class from 8.11 project and adapted to v2 versions of protocols.
 * New base class is AbstractDlmsProtocol.
 */
public abstract class AbstractSmartNtaProtocol extends AbstractDlmsProtocol {

    public static final int ObisCodeBFieldIndex = 1;

    public static final ObisCode dailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
    public static final ObisCode monthlyObisCode = ObisCode.fromString("0.0.98.1.0.255");

    public static final ObisCode FIRMWARE_VERSION_METER_CORE = ObisCode.fromString("1.1.0.2.0.255");
    public static final ObisCode FIRMWARE_VERSION_COMMS_MODULE = ObisCode.fromString("1.2.0.2.0.255");

    public static final ObisCode MBUS_DEVICE_CONFIGURATION = ObisCode.fromString("0.x.24.2.2.255");

    public AbstractSmartNtaProtocol(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(propertySpecService, collectedDataFactory, issueFactory);
    }

    /**
     * Get the AXDRDateTimeDeviationType for this DeviceType
     *
     * @return the requested type
     */
    public abstract AXDRDateTimeDeviationType getDateTimeDeviationType();

    /**
     * The <code>Properties</code> used for this protocol
     */
    protected DlmsProperties properties;

    /**
     * Indicating if the meter has a breaker.
     * This implies whether or not we can control the breaker and read the control logbook.
     * This will be set to false in the cryptoserver protocols, because these meters don't have a breaker anymore.
     */
    private boolean hasBreaker = true;

    /**
     * The used {@link ComposedMeterInfo}
     */
    private ComposedMeterInfo meterInfo;

    /**
     * The used {@link Dsmr23RegisterFactory}
     */
    protected Dsmr23RegisterFactory registerFactory;

    protected Dsmr23LogBookFactory logBookFactory;

    /**
     * The used {@link LoadProfileBuilder}
     */
    protected LoadProfileBuilder loadProfileBuilder;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology}
     */
    protected MeterTopology meterTopology;

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    public DlmsProperties getProperties() {
        if (this.properties == null) {
            this.properties = new DlmsProperties();
        }
        return this.properties;
    }

    /**
     * Tests if the Rtu wants to use the bulkRequests
     *
     * @return true if the Rtu wants to use BulkRequests, false otherwise
     */
    public boolean supportsBulkRequests() {
        return getProperties().isBulkRequest();
    }

    /**
     * 'Lazy' getter for the {@link #meterInfo}
     *
     * @return the {@link #meterInfo}
     */
    public ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) { //TODO hardcoded roundTripCorrection and retries, remove hardcode or get info from properties
            meterInfo = new ComposedMeterInfo(getDlmsSession(), supportsBulkRequests(), 0, 0);
        }
        return meterInfo;
    }

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     */
    public String getFirmwareVersion() {
        return getMeterInfo().getFirmwareVersion();
    }

    /**
     * Get the SerialNumber of the device
     *
     * @return the serialNumber of the device
     */
    public String getMeterSerialNumber() {
        return getMeterInfo().getSerialNr();
    }


    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return this.getRegisterFactory().readRegisters(registers);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getDeviceLogBookFactory().getLogBookData(logBooks);
    }

    private Dsmr23LogBookFactory getDeviceLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new Dsmr23LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return logBookFactory;
    }
    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date: 2017-02-28 22:03:14 +0200 (Tue, 28 Feb 2017) $";
    }

    protected DeviceRegisterSupport getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr23RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList
     *
     * @return the physical Address of the Meter.
     */
    public int getPhysicalAddress() {
        return 0; // the 'Master' has physicalAddress 0
    }

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    public ObisCode getPhysicalAddressCorrectedObisCode(final ObisCode obisCode, final String serialNumber) {
        int address;

        if (obisCode.equalsIgnoreBChannel(dailyObisCode) || obisCode.equals(monthlyObisCode)) {
            address = 0;
        } else {
            address = getPhysicalAddressFromSerialNumber(serialNumber);
        }

        //correct the mW values for 1.128.x.8.x.255
        if ((address == 0) && isElectricityMilliWatts(obisCode)) {
            return ProtocolTools.setObisCodeField(obisCode, ObisCodeBFieldIndex, (byte) 0);
        }

        if ((address == 0 && obisCode.getB() != -1) || obisCode.getB() == 128) { // then don't correct the obisCode
            return obisCode;
        }
        if (address != -1) {
            return ProtocolTools.setObisCodeField(obisCode, ObisCodeBFieldIndex, (byte) address);
        }
        return null;
    }

    /**
     * Some meter have custom obis-codes mapped to mW instead of kW
     *
     * @param obisCode the obis code to check
     * @return boolean true or false
     */
    public boolean isElectricityMilliWatts(ObisCode obisCode) {
        return Dsmr23RegisterFactory.isElectricityMilliWatts(obisCode);
    }


    @Override
    public String getSerialNumber() {
        if (getDlmsSessionProperties().useEquipmentIdentifierAsSerialNumber()){
            journal("Using Equipment-Identifier to identify the device");
            return getMeterInfo().getEquipmentIdentifier();
        } else {
            journal("Using Serial-Number to identify the device");
            return getMeterInfo().getSerialNr();
        }
    }

    /**
     * Return the serialNumber of the meter which corresponds with the B-Field of the given ObisCode
     *
     * @param obisCode the ObisCode
     * @return the serialNumber
     */
    public String getSerialNumberFromCorrectObisCode(ObisCode obisCode) {
        return getMeterTopology().getSerialNumber(obisCode);
    }

    /**
     * Search for the physicalAddress of the meter with the given serialNumber
     *
     * @param serialNumber the serialNumber of the meter
     * @return the requested physical address or -1 when it could not be found
     */
    public int getPhysicalAddressFromSerialNumber(final String serialNumber) {
        return getMeterTopology().getPhysicalAddress(serialNumber);
    }

    protected LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return loadProfileBuilder;
    }

    /**
     * Search for local slave devices so a general topology can be build up
     */
    public void searchForSlaveDevices() {
        getMeterTopology().searchForSlaveDevices();
    }

    public MeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new MeterTopology(this, this.getCollectedDataFactory());
            meterTopology.searchForSlaveDevices();
        }
        return meterTopology;
    }
    //TODO check if this is needed in v2
//    public List<String> getTopology(){
//        List<String> slaveDevices = new ArrayList<String>();
//
//        for(DeviceMapping device : getMeterTopology().getMbusMeterMap()){
//            slaveDevices.add(device.getSerialNumber());
//        }
//
//        return slaveDevices;
//    }

    public void readTopology() {
        searchForSlaveDevices();
    }
    //TODO check if WakeUp call is still needed for the protocol and how to fit it into Connexo if so
//    /**
//     * Executes the WakeUp call. The implementer should use and/or update the <code>Link</code> if a WakeUp succeeded. The communicationSchedulerId
//     * can be used to find the task which triggered this wakeUp or which Rtu is being waked up.
//     *
//     * @param communicationSchedulerId the ID of the <code>CommunicationScheduler</code> which started this task
//     * @param link                     Link created by the comserver, can be null if a NullDialer is configured
//     * @param logger                   Logger object - when using a level of warning or higher message will be stored in the communication session's database log,
//     *                                 messages with a level lower than warning will only be logged in the file log if active.
//     * @throws com.energyict.cbo.BusinessException if a business exception occurred
//     * @throws java.io.IOException                 if an io exception occurred
//     */
//    public boolean executeWakeUp(int communicationSchedulerId, Link link, Logger logger) throws BusinessException, IOException {
//        if (getProperties().isWakeUp()) {
//            String ipAddress = "";
//            logger.info("In Wakeup");
//            CommunicationScheduler cs = ProtocolTools.mw().getCommunicationSchedulerFactory().find(communicationSchedulerId);
//            if (cs != null) {
//                SmsWakeup smsWakeup = new SmsWakeup(cs.getRtu(), logger);
//                try {
//                    smsWakeup.doWakeUp();
//                } catch (SQLException e) {
//                    logger.severe("WakeUp failed - " + e.getMessage());
//                    Environment.getDefault().closeConnection();
//                    throw new BusinessException("Failed during the WakeUp", e);
//                }
//
//                ipAddress = ProtocolTools.checkIPAddressForPortNumber(smsWakeup.getIpAddress(), String.valueOf(getProperties().getIpPortNumber()));
//
//                link.setStreamConnection(new SocketStreamConnection(ipAddress));
//                link.getStreamConnection().open();
//                logger.log(Level.INFO, "Connected to " + ipAddress);
//            } else {
//                throw new BusinessException("Could not find the proper CommunicationScheduler during the WakeUp.");
//            }
//        }
//        return true;
//    }

    public boolean hasBreaker() {
        return hasBreaker;
    }

    /**
     * Setter is only called from the cryptoserver protocols to remove the breaker functionality
     */
    public void setHasBreaker(boolean hasBreaker) {
        this.hasBreaker = hasBreaker;
    }


    /**
     * Getter for the default obis code of the communication module
     *
     * @return ObisCode the ObisCode for Firmware Version Meter Core
     */
    public ObisCode getFirmwareVersionMeterCoreObisCode() {
        return FIRMWARE_VERSION_METER_CORE;
    }

    /**
     * Getter for the default obis code for the communication module
     * Individual protocols can ovveride (i.e. EMSR)
     *
     * @return ObisCode the Firmware Version communications module ObisCode
     */
    public ObisCode getFirmwareVersionCommsModuleObisCode() {
        return FIRMWARE_VERSION_COMMS_MODULE;
    }

    /**
     * Getter for the MBus device configuration object, which stores firmware information;
     *
     * @return ObisCode the ObisCode for the corresponding serial Number
     */
    private ObisCode getMbusDeviceConfigurationObisCode(String serialNumber) {
        return getPhysicalAddressCorrectedObisCode(MBUS_DEVICE_CONFIGURATION, serialNumber);
    }


    /**
     * Getter for the default obis code for the auxiliary module (only ESMR supports this)
     *
     * @return ObisCode, the Obis code for Auxiliary Firmware Version
     */
    private ObisCode getFirmwareVersionAuxiliaryModuleObisCode() {
        return ESMR50RegisterFactory.AUXILIARY_FIRMWARE_VERSION;
    }

    public void collectFirmwareVersionMeterCore(CollectedFirmwareVersion result) {
        ObisCode coreFirmwareVersion = getFirmwareVersionMeterCoreObisCode();
        try {
            journal("Collecting active meter core firmware version from " + coreFirmwareVersion);
            AbstractDataType valueAttr = getDlmsSession().getCosemObjectFactory().getData(coreFirmwareVersion).getValueAttr();
            String fwVersion = valueAttr.isOctetString() ? valueAttr.getOctetString().stringValue() : valueAttr.toBigDecimal().toString();
            result.setActiveMeterFirmwareVersion(fwVersion);
            journal("Active meter core firmware version is " + fwVersion);
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue problem = this.getIssueFactory().createProblem(coreFirmwareVersion, "issue.protocol.readingOfFirmwareFailed", e.toString());
                result.setFailureInformation(ResultType.InCompatible, problem);
            }
        }
    }


    public void collectFirmwareVersionCommunicationModule(CollectedFirmwareVersion result) {
        ObisCode communicationModuleFirmwareVersion = getFirmwareVersionCommsModuleObisCode();
        try {
            journal("Collecting active communication module firmware version from " + communicationModuleFirmwareVersion);
            AbstractDataType valueAttr = getDlmsSession().getCosemObjectFactory().getData(communicationModuleFirmwareVersion).getValueAttr();
            String fwVersion = valueAttr.isOctetString() ? valueAttr.getOctetString().stringValue() : valueAttr.toBigDecimal().toString();
            result.setActiveCommunicationFirmwareVersion(fwVersion);
            journal("Active communication module firmware version is " + fwVersion);
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue problem = this.getIssueFactory().createProblem(communicationModuleFirmwareVersion, "issue.protocol.readingOfFirmwareFailed", e.toString());
                result.setFailureInformation(ResultType.InCompatible, problem);
            }
        }
    }

    public void collectFirmwareVersionAuxiliary(CollectedFirmwareVersion result) {
        ObisCode auxiliaryModuleFirmwareVersion = getFirmwareVersionAuxiliaryModuleObisCode();
        try {
            journal("Collecting auxiliary module firmware version from " + auxiliaryModuleFirmwareVersion);
            AbstractDataType valueAttr = getDlmsSession().getCosemObjectFactory().getData(auxiliaryModuleFirmwareVersion).getValueAttr();
            String fwVersion = valueAttr.isOctetString() ? valueAttr.getOctetString().stringValue() : valueAttr.toBigDecimal().toString();
            result.setActiveAuxiliaryFirmwareVersion(fwVersion);
            journal("Auxiliary module firmware version is " + fwVersion);
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue problem = this.getIssueFactory().createProblem(auxiliaryModuleFirmwareVersion, "issue.protocol.readingOfFirmwareFailed", e.toString());
                result.setFailureInformation(ResultType.InCompatible, problem);
            }
        }
    }


    protected CollectedFirmwareVersion collectSlaveFirmwareVersions(String serialNumber) {
        DeviceIdentifierBySerialNumber slaveDevice = new DeviceIdentifierBySerialNumber(serialNumber);
        CollectedFirmwareVersion result = this.getCollectedDataFactory().createFirmwareVersionsCollectedData(slaveDevice);

        ObisCode mbusDeviceConfigurationObisCode = getMbusDeviceConfigurationObisCode(serialNumber);
        try {
            journal("Collecting M-Bus firmware information from " + mbusDeviceConfigurationObisCode);
            AbstractDataType valueAttr = getDlmsSession().getCosemObjectFactory().getExtendedRegister(mbusDeviceConfigurationObisCode).getValueAttr();

            MBusConfigurationObject configurationObject = new MBusConfigurationObject(valueAttr);
            if (configurationObject.isDecoded()) {
                journal("Collected firmware configuration: " + configurationObject.getContent());

                result.setActiveMeterFirmwareVersion(configurationObject.getOperationalFirmware());
                journal("Setting meter operational fw: " + configurationObject.getOperationalFirmware());

                final Optional<? extends OfflineDevice> slave = offlineDevice.getAllSlaveDevices().stream()
                        .filter(d -> d.getSerialNumber().equals(serialNumber)).findFirst();

                if (slave.isPresent()) {
                    final OfflineDevice offlineDevice = slave.get();
                    if (offlineDevice.supportsCommunicationFirmwareVersion()) {
                        result.setActiveCommunicationFirmwareVersion(configurationObject.getAdditionalFirmware());
                        journal("Setting communication fw: " + configurationObject.getAdditionalFirmware());
                    }

                    if (offlineDevice.supportsAuxiliaryFirmwareVersion()) {
                        journal("Setting auxiliary fw: " + configurationObject.getAdditionalFirmware());
                        result.setActiveAuxiliaryFirmwareVersion(configurationObject.getAdditionalFirmware());
                    }
                }

            } else {
                journal(Level.WARNING, configurationObject.getErrorMessage());
            }

        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue problem = this.getIssueFactory().createProblem(mbusDeviceConfigurationObisCode, "issue.protocol.readingOfFirmwareFailed", e.toString());
                result.setFailureInformation(ResultType.InCompatible, problem);
            }
        }

        return result;
    }


    @Override
    public CollectedFirmwareVersion getFirmwareVersions(String serialNumber) {

        //check for slaves first
        if (serialNumber != null) {
            if (!getSerialNumber().equals(serialNumber)) {
                return collectSlaveFirmwareVersions(serialNumber);
            }
        }

        // return the master
        CollectedFirmwareVersion result = this.getCollectedDataFactory().createFirmwareVersionsCollectedData(
                new DeviceIdentifierById(this.offlineDevice.getId()));

        collectFirmwareVersionMeterCore(result);

        if (supportsCommunicationFirmwareVersion()) {
            collectFirmwareVersionCommunicationModule(result);
        }

        if (supportsAuxiliaryFirmwareVersion()) {
            collectFirmwareVersionAuxiliary(result);
        }

        return result;
    }


    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }

    @Override
    public boolean supportsAuxiliaryFirmwareVersion() {
        // only ESMR5 meters support this
        return false;
    }
}
