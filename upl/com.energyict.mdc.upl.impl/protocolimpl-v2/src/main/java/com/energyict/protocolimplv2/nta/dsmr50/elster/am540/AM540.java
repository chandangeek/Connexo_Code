package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.migration.MigrateFromV1Protocol;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataEncryptionException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.idis.AM540ObjectList;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr50.Dsmr50ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.logbooks.Dsmr50LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.AM540MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles.AM540LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.registers.Dsmr50RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * V2 version of the AM540 protocol.
 * This version adds breaker & relais support and other IDIS features.
 * <p>
 * Note that this is a hybrid between DSMR5.0 and IDISP2.
 * <p>
 * The frame counter is not read out (no register in the meter has it), it is stored in the device cache so it can be re-used in the next communication session
 *
 * @author khe
 * @since 17/12/2014 - 14:30
 */
public class AM540 extends AbstractDlmsProtocol implements MigrateFromV1Protocol, SerialNumberSupport {

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final LoadProfileExtractor loadProfileExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final TariffCalendarExtractor calendarExtractor;
    private final NumberLookupExtractor numberLookupExtractor;
    private Dsmr50LogBookFactory dsmr50LogBookFactory;
    private AM540Messaging am540Messaging;
    private long initialFrameCounter = -1;
    private IDISMeterTopology meterTopology;
    private LoadProfileBuilder loadProfileBuilder;
    private Dsmr50RegisterFactory registerFactory;
    private AM540Cache am540Cache;

    public AM540(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.loadProfileExtractor = loadProfileExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.calendarExtractor = calendarExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        getLogger().info("AM540 protocol init");
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        getDeviceCache().setConnectionToBeaconMirror(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());

        HHUSignOnV2 hhuSignOn = null;
        if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties(), hhuSignOn, "P07210"));
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    /**
     * Connect to the device and check the cached object list.
     */
    @Override
    public void logOn() {
        connectWithRetries();
        checkCacheObjects();
    }

    @Override
    public AM540Cache getDeviceCache() {
        if (this.am540Cache == null) {
            am540Cache = new AM540Cache(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());
        }
        if (getDlmsSession() != null) { // this is called in init(), where we don't have a DLMS Session yet
            try {
                this.am540Cache.setTXFrameCounter(getDlmsSessionProperties().getClientMacAddress(),
                        getDlmsSession().getAso().getSecurityContext().getFrameCounter() + 1);     //Save this for the next session
            } catch (Exception ex) {
                getLogger().severe(ex.getCause() + ex.getMessage());
            }
        }
        return this.am540Cache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        getLogger().info("AM540 - setDeviceCace");
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof AM540Cache)) {
            am540Cache = (AM540Cache) deviceProtocolCache;
            this.initialFrameCounter = this.am540Cache.getTXFrameCounter(1);
            getLogger().info(" - saving frameCounter: " + this.initialFrameCounter);
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {

        //Refresh the object list if it doesn't exist or if the property is enabled
        if ((getDeviceCache().getObjectList() == null) || (getDlmsSessionProperties().isReadCache())) {

            //For beacon mirror logical device, always read the actual object list. Same for when the property is enabled.
            if (getDlmsSessionProperties().isReadCache() || getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
                getLogger().info("ForcedToReadCache property is true, reading cache!");
                readObjectList();
                getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            } else {
                //In case of actual meter, use a hard coded object list to avoid heavy load on the PLC network
                getLogger().info("Cache does not exist, using hardcoded copy of object list");
                UniversalObject[] objectList = new AM540ObjectList().getObjectList();
                getDeviceCache().saveObjectList(objectList);
            }
        } else {
            getLogger().info("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        super.setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(initialFrameCounter == -1 ? 1 : initialFrameCounter);    //Set the frameCounter from last session (which has been loaded from cache)
    }

    public Dsmr50Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new Dsmr50Properties();
        }
        return (Dsmr50Properties) dlmsProperties;
    }


    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     */
    private void connectWithRetries() {
        int tries = 0;
        while (true) {
            ProtocolRuntimeException exception;
            try {
                getDlmsSession().getDLMSConnection().setRetries(0);   //Temporarily disable retries in the connection layer, AARQ retries are handled here
                if (getDlmsSession().getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    getDlmsSession().getDlmsV2Connection().connectMAC();
                    getDlmsSession().createAssociation((int) getDlmsSessionProperties().getAARQTimeout());
                }
                return;
            } catch (ProtocolRuntimeException e) {
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;        //Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (e instanceof ConnectionCommunicationException) {
                    throw e;
                } else if (e instanceof DataEncryptionException) {
                    throw e;
                }
                exception = e;
            } finally {
                getDlmsSession().getDLMSConnection().setRetries(getDlmsSessionProperties().getRetries());
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > getDlmsSessionProperties().getAARQRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries.");
                throw CommunicationException.protocolConnectFailed(exception);
            } else {
                getLogger().info("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    getDlmsSession().getAso().releaseAssociation();
                } catch (ProtocolRuntimeException e) {
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
                getDlmsSession().getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
            }
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new SioOpticalConnectionType(this.propertySpecService));
        result.add(new RxTxOpticalConnectionType(this.propertySpecService));
        return result;
    }

    /**
     * A collection of general DSMR50 properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new Dsmr50ConfigurationSupport(this.propertySpecService);
        }
        return dlmsConfigurationSupport;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM540 DLMS (NTA DSMR5.0) V2";
    }


    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfileReaders);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfileReaders);
    }

    protected LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new AM540LoadProfileBuilder(this, this.getCollectedDataFactory(), this.getIssueFactory());
            ((Dsmr40LoadProfileBuilder) loadProfileBuilder).setCumulativeCaptureTimeChannel(getDlmsSessionProperties().isCumulativeCaptureTimeChannel());
        }
        return loadProfileBuilder;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        return getDsmr50LogBookFactory().getLogBookData(logBookReaders);
    }

    private Dsmr50LogBookFactory getDsmr50LogBookFactory() {
        if (dsmr50LogBookFactory == null) {
            dsmr50LogBookFactory = new Dsmr50LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return dsmr50LogBookFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getAM540Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> list) {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            IOException cause = new IOException("When connected to the mirror logical device, execution of device commands is not allowed.");
            throw DeviceConfigurationException.notAllowedToExecuteCommand("send of device messages", cause);
        } else {
            return getAM540Messaging().executePendingMessages(list);
        }
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> list) {
        return getAM540Messaging().updateSentMessages(list);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return getAM540Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new SerialDeviceProtocolDialect(this.propertySpecService), new TcpDeviceProtocolDialect(this.propertySpecService));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    private Dsmr50RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr50RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    public AM540Messaging getAM540Messaging() {
        if (this.am540Messaging == null) {
            this.am540Messaging =
                    new AM540Messaging(
                            new AM540MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory()),
                            this.propertySpecService,
                            this.nlsService,
                            this.converter,
                            this.messageFileExtractor,
                            this.calendarExtractor,
                            this.numberLookupExtractor,
                            this.loadProfileExtractor);
        }
        return this.am540Messaging;
    }

    /**
     * Read out the serial number, this can either be of the module (equipment identifier) or of the connected e-meter.
     * Note that reading out this register from the mirror logical device in the Beacon, the obiscode must always be 0.0.96.1.0.255
     */
    @Override
    public String getSerialNumber() {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect() || !getDlmsSessionProperties().useEquipmentIdentifierAsSerialNumber()) {
            return getMeterInfo().getSerialNr();
        } else {
            return getMeterInfo().getEquipmentIdentifier();
        }
    }

    @Override
    public Date getTime() {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            return new Date();  //Don't read out the clock of the mirror logical device, it does not know the actual meter time.
        } else {
            return super.getTime();
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            IOException cause = new IOException("When connected to the mirror logical device, writing of the clock is not allowed.");
            throw DeviceConfigurationException.notAllowedToExecuteCommand("date/time change", cause);
        } else {
            super.setTime(timeToSet);
        }
    }

    @Override
    public IDISMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new IDISMeterTopology(this, this.getCollectedDataFactory());
            meterTopology.searchForSlaveDevices();
        }
        return meterTopology;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-10-13 18:07:16 +0300 (Thu, 13 Oct 2016)$";
    }

    @Override
    public TypedProperties formatLegacyProperties(com.energyict.mdc.upl.properties.TypedProperties legacyProperties) {
        TypedProperties result = TypedProperties.empty();

        // Map 'ServerMacAddress' to 'ServerUpperMacAddress' and 'ServerLowerMacAddress'
        Object serverMacAddress = legacyProperties.getProperty(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        if (serverMacAddress != null) {
            String[] macAddress = ((String) serverMacAddress).split(":");
            if (macAddress.length >= 1) {
                String upperMacAddress = macAddress[0];
                if ("x".equals(upperMacAddress.toLowerCase())) {
                    result.setProperty(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, new BigDecimal(-1));
                } else {
                    result.setProperty(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, mapToBigDecimal(upperMacAddress));
                }
            }

            if (macAddress.length >= 2) {
                String lowerMacAddress = macAddress[1];
                if ("x".equals(lowerMacAddress.toLowerCase())) {
                    result.setProperty(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, new BigDecimal(-1));
                } else {
                    result.setProperty(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, mapToBigDecimal(lowerMacAddress));
                }
            }
        }

        // Map 'ForcedToReadCache' to 'ReadCache'
        Object readCache = legacyProperties.getProperty(Dsmr40Properties.PROPERTY_FORCED_TO_READ_CACHE);
        if (readCache != null) {
            result.setProperty(Dsmr50Properties.READCACHE_PROPERTY, ProtocolTools.getBooleanFromString((String) readCache));
        }

        return result;
    }

    private BigDecimal mapToBigDecimal(String text) {
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

}