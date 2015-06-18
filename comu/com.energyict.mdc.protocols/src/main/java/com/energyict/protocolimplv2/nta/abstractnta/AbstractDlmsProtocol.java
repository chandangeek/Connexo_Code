package com.energyict.protocolimplv2.nta.abstractnta;

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
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.DlmsProperties;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.dsmr23.ComposedMeterInfo;
import com.energyict.protocolimplv2.nta.dsmr23.logbooks.Dsmr23LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr23.registers.Dsmr23RegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;
import com.energyict.protocolimplv2.security.DlmsSecuritySupport;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.io.IOException;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Common functionality that is shared between the smart V2 DLMS protocols
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/13
 * Time: 13:30
 * Author: khe
 */
public abstract class AbstractDlmsProtocol implements DeviceProtocol {

    public static final ObisCode dailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
    public static final ObisCode monthlyObisCode = ObisCode.fromString("0.0.98.1.0.255");

    protected DlmsProperties dlmsProperties;
    protected LoadProfileBuilder loadProfileBuilder;
    protected OfflineDevice offlineDevice;
    protected Dsmr23RegisterFactory registerFactory = null;

    private ComposedMeterInfo meterInfo;
    private DlmsSession dlmsSession;
    private DLMSCache dlmsCache;
    private MeterTopology meterTopology;
    private Dsmr23LogBookFactory logBookFactory;
    private Dsmr23Messaging dsmr23Messaging;
    private DlmsSecuritySupport dlmsSecuritySupport;

    private final Clock clock;
    private final PropertySpecService propertySpecService;
    private final SocketService socketService;
    private final SerialComponentService serialComponentService;
    private final IssueService issueService;
    private final TopologyService topologyService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private final LoadProfileFactory loadProfileFactory;

    protected AbstractDlmsProtocol(Clock clock, PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory) {
        this.clock = clock;
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
    }

    protected MeteringService getMeteringService() {
        return meteringService;
    }

    protected Clock getClock() {
        return clock;
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    protected MdcReadingTypeUtilService getReadingTypeUtilService() {
        return readingTypeUtilService;
    }

    protected CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    /**
     * Connect to the device, check the cached object lost and discover its Bus slaves.
     */
    @Override
    public void logOn() {
        getDlmsSession().connect();
        checkCacheObjects();
        getMeterTopology().searchForSlaveDevices();
    }

    @Override
    public Date getTime() {
        try {
            return getDlmsSession().getCosemObjectFactory().getClock().getDateTime();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(timeToSet, getTimeZone()));
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public String getSerialNumber() {
        return getMeterInfo().getSerialNr();
    }


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
     * @param deviceProtocolSecurityPropertySet
     *         the {@link DeviceProtocolSecurityPropertySet}to set
     */
    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getDlmsProperties().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getDlmsProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    private DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new DsmrSecuritySupport(this.propertySpecService);
        }
        return dlmsSecuritySupport;
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        return getSecuritySupport().getSecurityPropertySpecs();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getSecuritySupport().getSecurityRelationTypeName();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return getSecuritySupport().getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return getSecuritySupport().getEncryptionAccessLevels();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return getSecuritySupport().getSecurityPropertySpec(name);
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

    public MeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new MeterTopology(this, issueService, identificationService, collectedDataFactory);
        }
        return meterTopology;
    }

    protected Dsmr23RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr23RegisterFactory(this, this.issueService, this.readingTypeUtilService, collectedDataFactory, getDlmsProperties().isBulkRequest());
        }
        return registerFactory;
    }

    protected LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, this.issueService, readingTypeUtilService, collectedDataFactory, getDlmsProperties().isBulkRequest());
        }
        return loadProfileBuilder;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        try {
            this.dlmsCache = (DLMSCache) deviceProtocolCache;
        } catch (ClassCastException e) {
            this.dlmsCache = null;
        }
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return dlmsCache;
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
            throw IOExceptionHandler.handle(e, getDlmsSession());
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

                getLogger().fine("Checking the configuration parameters.");
                configNumber = getMeterInfo().getConfigurationChanges();

                if (this.dlmsCache.getConfProgChange() != configNumber) {
                    getLogger().fine("Meter configuration has changed, configuration is forced to be read.");
                    readObjectList();
                    changed = true;
                }

            } else { // cache does not exist
                this.dlmsCache = new DLMSCache();
                getLogger().fine("Cache does not exist, configuration is forced to be read.");
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

    protected void setDlmsSession(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    /**
     * 'Lazy' getter for the {@link #meterInfo}
     *
     * @return the {@link #meterInfo}
     */
    protected ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getDlmsSession(), getDlmsProperties().isBulkRequest());
        }
        return meterInfo;
    }

    protected DlmsProperties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new DlmsProperties(this.propertySpecService);
        }
        return dlmsProperties;
    }

    public TimeZone getTimeZone() {
        return getDlmsProperties().getTimeZone();
    }

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode     the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        int address;

        if (obisCode.equalsIgnoreBChannel(dailyObisCode) || obisCode.equalsIgnoreBChannel(monthlyObisCode)) {
            address = 0;
        } else {
            address = getPhysicalAddressFromSerialNumber(serialNumber);
        }

        if ((address == 0 && obisCode.getB() != -1 && obisCode.getB() != 128)) { // then don't correct the obisCode
            return obisCode;
        }

        if (address != -1) {
            return ProtocolTools.setObisCodeField(obisCode, 1, (byte) address);
        }
        return null;
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

    public Logger getLogger() { //TODO: usage of old logger should be prevented -> refactor/remove this
        return Logger.getLogger(this.getClass().getName());
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        getDlmsProperties().addProperties(properties);
    }

    @Override
    public PropertySpec getPropertySpec(String propertySpecName) {
        return getDlmsProperties().getPropertySpec(propertySpecName);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getDlmsProperties().getPropertySpecs();
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
}