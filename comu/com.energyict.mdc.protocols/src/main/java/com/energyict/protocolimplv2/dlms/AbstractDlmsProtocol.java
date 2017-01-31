/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceRegisterSupport;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimplv2.common.MyOwnPrivateRegister;
import com.energyict.protocolimplv2.nta.dsmr23.ComposedMeterInfo;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.logbooks.Dsmr23LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr23.registers.Dsmr23RegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;
import com.energyict.protocolimplv2.security.DlmsSecuritySupport;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Provider;
import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

public abstract class AbstractDlmsProtocol implements DeviceProtocol {

    public static final ObisCode dailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
    public static final ObisCode monthlyObisCode = ObisCode.fromString("0.0.98.1.0.255");
    protected final Thesaurus thesaurus;
    protected final PropertySpecService propertySpecService;
    private final Clock clock;
    private final SocketService socketService;
    private final SerialComponentService serialComponentService;
    private final IssueService issueService;
    private final TopologyService topologyService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private final LoadProfileFactory loadProfileFactory;
    private final Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider;
    protected DlmsProperties dlmsProperties;
    protected LoadProfileBuilder loadProfileBuilder;
    protected OfflineDevice offlineDevice;
    protected ConfigurationSupport dlmsConfigurationSupport;
    protected Dsmr23RegisterFactory registerFactory = null;
    private ComposedMeterInfo meterInfo;
    private DlmsSession dlmsSession;
    private DLMSCache dlmsCache;
    protected AbstractMeterTopology meterTopology;
    private Dsmr23LogBookFactory logBookFactory;
    private Dsmr23Messaging dsmr23Messaging;
    protected DlmsSecuritySupport dlmsSecuritySupport;
    /**
     * Indicating if the meter has a breaker.
     * This implies whether or not we can control the breaker and read the control logbook.
     * This will be set to false in the cryptoserver protocols, because these meters don't have a breaker anymore.
     */
    private boolean hasBreaker = true;
    private Logger logger;

    protected AbstractDlmsProtocol(
            Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService,
            SerialComponentService serialComponentService, IssueService issueService,
            TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService,
            IdentificationService identificationService, CollectedDataFactory collectedDataFactory,
            MeteringService meteringService, LoadProfileFactory loadProfileFactory,
            Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.socketService = socketService;
        this.serialComponentService = serialComponentService;
        this.issueService = issueService;
        this.topologyService = topologyService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
        this.loadProfileFactory = loadProfileFactory;
        this.dsmrSecuritySupportProvider = dsmrSecuritySupportProvider;
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }

    public Clock getClock() {
        return clock;
    }

    public IssueService getIssueService() {
        return issueService;
    }

    public MdcReadingTypeUtilService getReadingTypeUtilService() {
        return readingTypeUtilService;
    }

    public CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    /**
     * Connect to the device, check the cached object lost and discover its MBus slaves.
     */
    @Override
    public void logOn() {
        getDlmsSession().connect();
        checkCacheObjects();
    }

    @Override
    public Date getTime() {
        return getMeterInfo().getClock();
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(timeToSet, getTimeZone()));
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsProperties().getRetries() + 1);
        }
    }

    @Override
    public String getSerialNumber() {
        return getMeterInfo().getSerialNr();
    }

//    /**
//     * General device properties, add them to the DLMS session properties
//     *
//     * @param properties properties to add
//     */
//    @Override
//    public void addProperties(TypedProperties properties) {
//        this.getDlmsSessionProperties().addProperties(properties);
//    }

    /**
     * Dialect properties, add them to the DLMS session properties
     *
     * @param dialectProperties the DeviceProtocolDialectProperties to add to the DeviceProtocol
     */
    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getDlmsProperties().addProperties(dialectProperties);
    }

    /**
     * Security related properties, add them to the DLMS session properties
     *
     * @param deviceProtocolSecurityPropertySet the {@link DeviceProtocolSecurityPropertySet}to set
     */
    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getDlmsProperties().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getDlmsProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = dsmrSecuritySupportProvider.get();
        }
        return dlmsSecuritySupport;
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return this.getSecuritySupport().getCustomPropertySet();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return getSecuritySupport().getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return getSecuritySupport().getEncryptionAccessLevels();
    }

    public Dsmr23LogBookFactory getDeviceLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new Dsmr23LogBookFactory(this, issueService, collectedDataFactory, meteringService);
        }
        return logBookFactory;
    }

    protected Dsmr23Messaging getDsmr23Messaging() {
        if (dsmr23Messaging == null) {
            dsmr23Messaging = new Dsmr23Messaging(new Dsmr23MessageExecutor(this, clock, topologyService, this.issueService, this.readingTypeUtilService, this.collectedDataFactory, this.loadProfileFactory), topologyService);
        }
        return dsmr23Messaging;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getMeterTopology().getDeviceTopology();
    }

    public AbstractMeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new MeterTopology(this, issueService, identificationService, collectedDataFactory);
            meterTopology.searchForSlaveDevices();
        }
        return meterTopology;
    }

    protected DeviceRegisterSupport getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr23RegisterFactory(this, this.issueService, this.readingTypeUtilService, collectedDataFactory, getDlmsProperties().isBulkRequest());
        }
        return registerFactory;
    }

    protected LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, this.issueService, readingTypeUtilService, collectedDataFactory, getDlmsProperties().isBulkRequest(), getDlmsProperties().isIgnoreDSTStatusCode());
        }
        return loadProfileBuilder;
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return dlmsCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        try {
            this.dlmsCache = (DLMSCache) deviceProtocolCache;
        } catch (ClassCastException e) {
            this.dlmsCache = null;
        }
    }

    /**
     * Request Association buffer list out of the meter.
     */
    protected void readObjectList() {
        try {
            if (getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationLN().getBuffer());
            } else if (getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationSN().getBuffer());
            } else {
                throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
            }
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsProperties().getRetries() + 1);
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        int configNumber = -1;
        boolean changed = false;
        try {
            if (this.dlmsCache != null && this.dlmsCache.getObjectList() != null) { // the dlmsCache exists
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());

                getLogger().info("Checking the configuration parameters.");
                configNumber = getMeterInfo().getConfigurationChanges();

                if (this.dlmsCache.getConfProgChange() != configNumber) {
                    getLogger().info("Meter configuration has changed, configuration is forced to be read.");
                    readObjectList();
                    changed = true;
                }

            } else { // cache does not exist
                this.dlmsCache = new DLMSCache();
                getLogger().info("Cache does not exist, configuration is forced to be read.");
                readObjectList();
                configNumber = getMeterInfo().getConfigurationChanges();
                changed = true;
            }
        } finally {
            if (changed) {
                this.dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
                this.dlmsCache.setConfProgChange(configNumber);
            }
        }
    }

    @Override
    public void logOff() {
        if (getDlmsSession() != null) {
            getDlmsSession().disconnect();
        }
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    protected void setDlmsSession(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;
    }

    /**
     * 'Lazy' getter for the {@link #meterInfo}
     *
     * @return the {@link #meterInfo}
     */
    protected ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getDlmsSession(),
                    getDlmsProperties().isBulkRequest(),
                    getDlmsProperties().getRoundTripCorrection(),
                    getDlmsProperties().getRetries()
            );
        }
        return meterInfo;
    }

    public DlmsSessionProperties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new DlmsProperties(this.propertySpecService, this.thesaurus);
        }
        return dlmsProperties;
    }

    public TimeZone getTimeZone() {
        return getDlmsProperties().getTimeZone();
    }

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        if (obisCode.anyChannel()) {
            return getMeterTopology().getPhysicalAddressCorrectedObisCode(obisCode, serialNumber);
        } else {
            return obisCode;
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
    public int getPhysicalAddressFromSerialNumber(String serialNumber) {
        return getMeterTopology().getPhysicalAddress(serialNumber);
    }

    @Override
    public void terminate() {
        //Nothing to do here...
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        getDlmsProperties().addProperties(properties);
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> allProperties = new ArrayList<>(getDlmsConfigurationSupport().getOptionalProperties());
        allProperties.addAll(getDlmsConfigurationSupport().getRequiredProperties());
        return allProperties;
    }

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
     * A collection of general DLMS properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new DlmsConfigurationSupport(propertySpecService);
        }
        return dlmsConfigurationSupport;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.GATEWAY;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return new ManufacturerInformation();
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public SocketService getSocketService() {
        return socketService;
    }

    public SerialComponentService getSerialComponentService() {
        return serialComponentService;
    }

    public IdentificationService getIdentificationService() {
        return identificationService;
    }

    public TopologyService getTopologyService() {
        return topologyService;
    }

    public LoadProfileFactory getLoadProfileFactory() {
        return loadProfileFactory;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = getCollectedDataFactory().createFirmwareVersionsCollectedData(this.offlineDevice.getDeviceIdentifier());
        firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getMeterInfo().getFirmwareVersion());
        return firmwareVersionsCollectedData;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return collectedDataFactory.createBreakerStatusCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        CollectedCalendar collectedCalendar = this.getCollectedDataFactory().createCalendarCollectedData(this.offlineDevice.getDeviceIdentifier());
        try {
            ActivityCalendar activityCalendar = this.getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
            this.updateCollectedCalendar(activityCalendar, collectedCalendar);
        } catch (ProtocolException e) {
            this.getIssueService().newProblem(
                    this.getCalendarRegister(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE),
                    com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_READ_CALENDAR_INFO,
                    DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE, e);
        }
        return collectedCalendar;
    }

    private void updateCollectedCalendar(ActivityCalendar activityCalendar, CollectedCalendar collectedCalendar) {
        try {
            collectedCalendar.setActiveCalendar(activityCalendar.readCalendarNameActive().stringValue());
        } catch (IOException e) {
            this.getIssueService().newProblem(
                    this.getCalendarRegister(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE),
                    com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_READ_CALENDAR_INFO,
                    DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE, e);
        }
        try {
            collectedCalendar.setPassiveCalendar(activityCalendar.readCalendarNamePassive().stringValue());
        } catch (IOException e) {
            this.getIssueService().newProblem(
                    this.getCalendarRegister(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE),
                    com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_READ_CALENDAR_INFO,
                    DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE, e);
        }
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return this.getDlmsSession().getCosemObjectFactory();
    }

    private OfflineRegister getCalendarRegister(ObisCode obisCode) {
        return new MyOwnPrivateRegister(this.getOfflineDevice(), obisCode);
    }

    public boolean useDsmr4SelectiveAccessFormat() {
        return true;
    }
}