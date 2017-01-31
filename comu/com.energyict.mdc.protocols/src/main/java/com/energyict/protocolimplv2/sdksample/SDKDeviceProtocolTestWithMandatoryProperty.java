/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.UnableToCreateConnectionType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.protocols.impl.channels.ConnectionTypeRule;

import com.energyict.protocolimplv2.security.DlmsSecuritySupport;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation of a DeviceProtocol which 1 mandatory and 1 optional property
 */
public class SDKDeviceProtocolTestWithMandatoryProperty extends SDKDeviceProtocol {

    private Logger logger = Logger.getLogger(SDKDeviceProtocol.class.getSimpleName());

    /**
     * The {@link com.energyict.mdc.protocol.api.device.offline.OfflineDevice} that holds all <i>necessary</i> information to perform the relevant ComTasks for this <i>session</i>
     */
    private OfflineDevice offlineDevice;
    /**
     * The ComChannel that will be used to read/write.
     * Actual reading/writing needs to be performed on this object as logging/communicationStatistics are calculated based on
     * the calls you make on this.
     */
    private ComChannel comChannel;
    /**
     * Will hold the cache object of the Device related to this protocol
     */
    private DeviceProtocolCache deviceProtocolCache;
    /**
     * Keeps track of all the protocol properties <b>AND</b> the current deviceProtocolDialectProperties
     */
    private TypedProperties typedProperties = TypedProperties.empty();
    private final PropertySpecService propertySpecService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public SDKDeviceProtocolTestWithMandatoryProperty(Thesaurus thesaurus, PropertySpecService propertySpecService, IdentificationService identificationService,
                                                      CollectedDataFactory collectedDataFactory, ProtocolPluggableService protocolPluggableService,
                                                      DlmsSecuritySupport dlmsSecuritySupport) {
        super(protocolPluggableService, thesaurus, propertySpecService, identificationService, collectedDataFactory, dlmsSecuritySupport);
        this.propertySpecService = propertySpecService;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.protocolPluggableService = protocolPluggableService;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.comChannel = comChannel;
        this.logger.log(Level.INFO, "Initializing DeviceProtocol for Device with serialNumber " + this.offlineDevice.getSerialNumber());
    }

    @Override
    public void terminate() {
        /*
        The order of disconnecting a session is:
        - logOff or daisyChainedLogOff (depending on the following ComTasks)
        - terminate
        - physically closing the connection
         */
        this.logger.log(Level.INFO, "Terminating DeviceProtocol SESSION");
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.values());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(
                this.propertySpecService
                        .stringSpec()
                        .named(SDKTranslationKeys.SDKSTRINGPROPERTY)
                        .fromThesaurus(getThesaurus())
                        .markRequired()
                        .finish());
        propertySpecs.add(
                this.propertySpecService
                        .obisCodeSpec()
                        .named(SDKTranslationKeys.SDKOBISCODEPROPERTY)
                        .fromThesaurus(getThesaurus())
                        .addValues(
                                ObisCode.fromString("1.0.1.8.0.255"),
                                ObisCode.fromString("1.0.1.8.1.255"),
                                ObisCode.fromString("1.0.1.8.2.255"),
                                ObisCode.fromString("1.0.2.8.0.255"),
                                ObisCode.fromString("1.0.2.8.1.255"),
                                ObisCode.fromString("1.0.2.8.2.255"))
                        .markExhaustive()
                        .finish());
        return propertySpecs;
    }

    @Override
    public void logOn() {
        String logOn = "DeviceProtocol logOn!";
        this.logger.log(Level.INFO, logOn);
        this.comChannel.write(logOn.getBytes());
    }

    @Override
    public void daisyChainedLogOn() {
        String logOn = "DeviceProtocol daisyChained logOn!";
        this.logger.log(Level.INFO, logOn);
        this.comChannel.write(logOn.getBytes());
    }

    @Override
    public void logOff() {
        String logOff = "DeviceProtocol logOff!";
        this.logger.log(Level.INFO, logOff);
        this.comChannel.write(logOff.getBytes());
    }

    @Override
    public void daisyChainedLogOff() {
        String logOff = "DeviceProtocol daisyChained logOff!";
        this.logger.log(Level.INFO, logOff);
        this.comChannel.write(logOff.getBytes());
    }

    @Override
    public String getSerialNumber() {
        this.logger.log(Level.INFO, "Getting the serialNumber of the device, actually just returning the one we got from the OfflineDevice");
        return this.offlineDevice.getSerialNumber();
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceProtocolCache = deviceProtocolCache;
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return this.deviceProtocolCache;
    }

    @Override
    public void setTime(Date timeToSet) {
        if(getTimeDeviationPropertyForWrite().getSeconds() == 0){
            this.logger.log(Level.INFO, "Setting the time of the device to " + timeToSet);
            this.comChannel.write(timeToSet.toString().getBytes());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeToSet);
            calendar.add(Calendar.SECOND, getTimeDeviationPropertyForWrite().getSeconds());
            this.logger.log(Level.INFO, "Setting the time of the device to " + calendar.getTime() + ". " +
                    "This is the time added with the deviation property value of " + getTimeDeviationPropertyForWrite().getSeconds() + " seconds");
            this.comChannel.write(calendar.getTime().toString().getBytes());
        }
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>();
        // by default all loadProfileReaders are supported, only if the corresponding ObisCodeProperty matches, we mark it as not supported
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            this.logger.log(Level.INFO, "Fetching loadProfile configuration for loadProfile with ObisCode " + loadProfileReader.getProfileObisCode());
            CollectedLoadProfileConfiguration loadProfileConfiguration = collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getDeviceIdentifier());
            if (!loadProfileReader.getProfileObisCode().equals(getIgnoredObisCode())) {
                loadProfileConfiguration.setChannelInfos(loadProfileReader.getChannelInfos());
            } else {
                this.logger.log(Level.INFO, "Marking loadProfile as not supported due to the value of the " + SDKLoadProfileDialectProperties.ActualFields.NOT_SUPPORTED_LOAD_PROFILE.propertySpecName() + " property");
                loadProfileConfiguration.setSupportedByMeter(false);
            }
        }
        return loadProfileConfigurations;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        //TODO
        return Collections.emptyList();
    }

    @Override
    public Date getTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, getTimeDeviationPropertyForRead().getSeconds());
        Date timeToReturn = cal.getTime();
        this.logger.info("Returning the based on the deviationProperty " + timeToReturn);
        return timeToReturn;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        //TODO
        return Collections.emptyList();
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return EnumSet.of(
                DeviceMessageId.ACTIVITY_CALENDER_SEND,
                DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME,
                DeviceMessageId.CONTACTOR_ARM,
                DeviceMessageId.CONTACTOR_CLOSE,
                DeviceMessageId.CONTACTOR_OPEN,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER,
                DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE,
                DeviceMessageId.FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        //TODO
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        //TODO
        return null;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(
                new SDKLoadProfileProtocolDialect(this.getThesaurus(), this.propertySpecService),
                new SDKStandardProtocolDialect(this.getThesaurus(), this.propertySpecService),
                new SDKTimeProtocolDialect(this.getThesaurus(), this.propertySpecService),
                new SDKTopologyTaskProtocolDialect(this.getThesaurus(), this.propertySpecService));
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        this.logger.log(Level.INFO, "Adding the deviceProtocolDialect properties to the DeviceProtocol instance.");
        this.typedProperties.setAllProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.logger.log(Level.INFO, "Adding the deviceProtocolSecurity properties to the DeviceProtocol instance.");
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        final CollectedTopology collectedTopology = this.collectedDataFactory.createCollectedTopology(this.offlineDevice.getDeviceIdentifier());
        if (!is(getSlaveOneSerialNumber()).empty()) {
            collectedTopology.addSlaveDevice(this.identificationService.createDeviceIdentifierBySerialNumber(getSlaveOneSerialNumber()));
        }
        if (!is(getSlaveTwoSerialNumber()).empty()) {
            collectedTopology.addSlaveDevice(this.identificationService.createDeviceIdentifierBySerialNumber(getSlaveTwoSerialNumber()));
        }
        return collectedTopology;
    }

    @Override
    public String getProtocolDescription() {
        return "EICT SDK DeviceProtocol with mandatory properties";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.logger.log(Level.INFO, "Adding the properties to the DeviceProtocol instance.");
        this.typedProperties.setAllProperties(properties);
    }

    private ObisCode getIgnoredObisCode() {
        return (ObisCode) this.typedProperties.getProperty(SDKLoadProfileDialectProperties.ActualFields.NOT_SUPPORTED_LOAD_PROFILE.propertySpecName(), ObisCode.fromString("0.0.0.0.0.0"));
    }

    private TimeDuration getTimeDeviationPropertyForRead() {
        return (TimeDuration) this.typedProperties.getProperty(SDKTimeDialectProperties.ActualFields.CLOCK_OFFSET_WHEN_WRITING.propertySpecName(), new TimeDuration(0));
    }

    private TimeDuration getTimeDeviationPropertyForWrite() {
        return (TimeDuration) this.typedProperties.getProperty(SDKTimeDialectProperties.ActualFields.CLOCK_OFFSET_WHEN_READING.propertySpecName(), new TimeDuration(0));
    }

    private String getSlaveOneSerialNumber(){
        return (String) this.typedProperties.getProperty(SDKTopologyTaskProtocolDialect.slaveOneSerialNumberPropertyName, "");
    }

    private String getSlaveTwoSerialNumber(){
        return (String) this.typedProperties.getProperty(SDKTopologyTaskProtocolDialect.slaveTwoSerialNumberPropertyName, "");
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> connectionTypes = new ArrayList<>();
        for (ConnectionTypeRule connectionTypeRule : ConnectionTypeRule.values()) {
            try {
                connectionTypes.add(this.protocolPluggableService.createConnectionType(connectionTypeRule.getProtocolTypeClass().getName()));
            }
            catch (UnableToCreateConnectionType e) {
                e.printStackTrace(System.err);
            }
        }
        return connectionTypes;
    }

}