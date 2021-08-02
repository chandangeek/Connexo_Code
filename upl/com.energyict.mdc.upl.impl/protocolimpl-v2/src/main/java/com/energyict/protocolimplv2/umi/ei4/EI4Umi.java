package com.energyict.protocolimplv2.umi.ei4;

import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;

import com.energyict.cim.EndDeviceType;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimplv2.umi.AbstractUmiProtocol;
import com.energyict.protocolimplv2.umi.ei4.events.EI4UmiLogBookFactory;
import com.energyict.protocolimplv2.umi.ei4.messages.EI4UmiMessaging;
import com.energyict.protocolimplv2.umi.ei4.profile.EI4UmiProfileDataReader;
import com.energyict.protocolimplv2.umi.ei4.registers.EI4UmiRegisterFactory;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiActiveSoftwareVersion;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiwanMetrologyClock;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiwanMetrologyClockControl;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjRspPayload;
import com.energyict.protocolimplv2.umi.properties.UmiPropertiesBuilder;
import com.energyict.protocolimplv2.umi.properties.UmiSessionProperties;
import com.energyict.protocolimplv2.umi.properties.UmiSessionPropertiesS2;
import com.energyict.protocolimplv2.umi.security.scheme2.UmiCVCCertificate;
import com.energyict.protocolimplv2.umi.session.IUmiSession;
import com.energyict.protocolimplv2.umi.session.UmiSession;
import com.energyict.protocolimplv2.umi.session.UmiSessionS2;
import com.energyict.protocolimplv2.umi.types.ResultCode;
import com.energyict.protocolimplv2.umi.types.UmiId;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class EI4Umi extends AbstractUmiProtocol {

    private static final EndDeviceType typeMeter = EndDeviceType.GASMETER;

    private OfflineDevice offlineDevice;
    private final NlsService nlsService;
    private final Converter converter;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private ComChannel comChannel;
    private Logger logger;
    private EI4UmiLogBookFactory logBookFactory = null;
    private EI4UmiRegisterFactory registerFactory = null;
    private EI4UmiMessaging messaging = null;
    private IUmiSession umiSession;
    private EI4UmiProfileDataReader profileDataReader;

    private static PrivateKey ownPrivateKey = Keys.getOwnPrivateKey();
    private static UmiCVCCertificate ownCertificate = Keys.getOwnCert();
    private static UmiCVCCertificate comModuleCertificate = Keys.getRemoteCMCert();

    static private UmiId comModuleUmiId = new UmiId("144115188075855873");

    public EI4Umi(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory,
                  IssueFactory issueFactory, NlsService nlsService, Converter converter,
                  DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.converter = converter;
        this.messageFileExtractor = messageFileExtractor;
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(new InboundIpConnectionType());
    }

    @Override
    public String getProtocolDescription() {
        return "EI4 UMI Protocol";
    }

    @Override
    public String getVersion() {
        return "2021-03-05";
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.comChannel = comChannel;
        setupSecuredSession();
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.METER;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return new ManufacturerInformation();
    }

    @Override
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(new TcpDeviceProtocolDialect(getPropertySpecService(), nlsService));
    }

    @Override
    public String getSerialNumber() {
        return getOfflineDevice().getSerialNumber();
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return offlineDevice.getDeviceIdentifier();
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getProfileDataReader().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        return getProfileDataReader().getLoadProfileData(loadProfileReaders);
    }

    protected EI4UmiProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new EI4UmiProfileDataReader(this, getCollectedDataFactory(), getIssueFactory(),
                    getOfflineDevice());
        }
        return profileDataReader;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookFactory().getLogBookData(logBooks);
    }

    private DeviceLogBookSupport getLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = createLogBookFactory();
        }
        return logBookFactory;
    }

    protected EI4UmiLogBookFactory createLogBookFactory() {
        return new EI4UmiLogBookFactory(this, getIssueFactory(), getCollectedDataFactory());
    }

    protected EI4UmiMessaging getProtocolMessaging() {
        if (messaging == null) {
            messaging = createMessaging();
        }
        return messaging;
    }

    protected EI4UmiMessaging createMessaging() {
        return new EI4UmiMessaging(this, getCollectedDataFactory(), getIssueFactory(), getPropertySpecService(), nlsService, converter);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getProtocolMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getProtocolMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getProtocolMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    private EI4UmiMessaging getMessaging() {
        if (messaging == null) {
            messaging =
                    new EI4UmiMessaging(
                            this,
                            getCollectedDataFactory(),
                            getIssueFactory(),
                            getPropertySpecService(),
                            nlsService,
                            converter);
        }
        return messaging;
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    private DeviceRegisterSupport getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = createRegisterFactory();
        }
        return registerFactory;
    }

    protected EI4UmiRegisterFactory createRegisterFactory() {
        return new EI4UmiRegisterFactory(this, getIssueFactory(), getCollectedDataFactory());
    }

    public void setupSecuredSession() {
        UmiSessionProperties properties = new UmiPropertiesBuilder().sourceUmiId(UmiSession.thisUmiId).build();
        UmiId deviceId = null;
        try {
            umiSession = new UmiSession(comChannel, properties);
            umiSession.startSession();
            Pair<ResultCode, ReadObjRspPayload> pair = umiSession.readObject(UmiSession.UMI_ID_CODE);
            if (pair.getFirst() == ResultCode.OK) {
                deviceId = new UmiId(pair.getLast().getValue(), true);
                TypedProperties typedProperties = com.energyict.mdc.upl.TypedProperties.empty();
                typedProperties.setProperty("UmiId", deviceId.toString());
                this.setUPLProperties(typedProperties);
            }
        } catch (GeneralSecurityException generalSecurityException) {
            getLogger().severe(generalSecurityException.getLocalizedMessage());
            throw CommunicationException.protocolConnectFailed(generalSecurityException);
        } catch (IOException ioException) {
            getLogger().severe(ioException.getLocalizedMessage());
            throw CommunicationException.protocolConnectFailed(ioException);
        } catch (Exception e) {
            getLogger().warning(e.getMessage());
            throw CommunicationException.protocolConnectFailed(e);
        }
        // --------------------------------------------------------------------------

        UmiPropertiesBuilder builder = new UmiPropertiesBuilder()
                .sourceUmiId(UmiSession.thisUmiId)
                .destinationUmiId(deviceId == null ? UmiSession.destinationUmiId : deviceId)
                .ownCertificate(ownCertificate)
                .ownPrivateKey(ownPrivateKey);
        umiSession = new UmiSessionS2(comChannel, new UmiSessionPropertiesS2(builder, comModuleCertificate));
        try {
            ResultCode code = umiSession.startSession();
            if (code != ResultCode.OK) {
                getLogger().warning("UMI session is not established, result code=" + code.getDescription());
                throw new ProtocolException("UMI session is not established, result code=" + code.getDescription());
            } else {
                getLogger().info("Secured UMI session has been established");
            }
        } catch (Exception e) {
            getLogger().warning(e.getMessage());
            throw CommunicationException.protocolConnectFailed(e);
        }
    }

    public IUmiSession getUmiSession() {
        return umiSession;
    }


    public EndDeviceType getTypeMeter() {
        return typeMeter;
    }

    @Override
    public Date getTime() {
        Date date = new Date();
        try {
            Pair<ResultCode, ReadObjRspPayload> pair = umiSession.readObject(UmiwanMetrologyClock.METROLOGY_CLOCK_UMI_CODE);
            if (pair.getFirst() != ResultCode.OK) {
                throw new ProtocolException("Read clock operation failed with the code: " + pair.getFirst());
            }

            UmiwanMetrologyClock clock = new UmiwanMetrologyClock(pair.getLast().getValue());
            date = clock.getCurrentDateTime();
        } catch (Exception ex) {
            throw CommunicationException.protocolConnectFailed(ex);
        }
        return date;
    }

    public void setTime(Date timeToSet) {
        UmiwanMetrologyClockControl clockControl = new UmiwanMetrologyClockControl(timeToSet, 0, 0);
        try {
            ResultCode resultCode = umiSession.writeObject(UmiwanMetrologyClockControl.UMIWAN_METROLOGY_CLOCK_CONTROL, clockControl.getRaw());
            if (resultCode != ResultCode.OK) {
                throw new ProtocolException("Set clock operation failed with the code: " + resultCode);
            }
        } catch (Exception ex) {
            throw CommunicationException.protocolConnectFailed(ex);
        }

    }

    /**
     * Return empty breaker status.
     */
    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return getCollectedDataFactory().createBreakerStatusCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return getCollectedDataFactory().createCalendarCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions(String serialNumber) {
        if (offlineDevice.getSerialNumber().equals(serialNumber)) {
            CollectedFirmwareVersion firmwareVersionsCollectedData = getCollectedDataFactory().createFirmwareVersionsCollectedData(new DeviceIdentifierById(this.offlineDevice.getId()));
            try {
                Pair<ResultCode, ReadObjRspPayload> pair = umiSession.readObject(UmiActiveSoftwareVersion.ACTIVE_SOFTWARE_VERSION_UMI_CODE);
                if (pair.getFirst() != ResultCode.OK) {
                    throw new ProtocolException("Read active firmware version failed with the code: " + pair.getFirst());
                }
                UmiActiveSoftwareVersion softwareVersion = new UmiActiveSoftwareVersion(pair.getLast().getValue());
                firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(UmiActiveSoftwareVersion.ACTIVE_HOST_SOFTWARE_VERSION);
                firmwareVersionsCollectedData.setActiveCommunicationFirmwareVersion(softwareVersion.toString());
                return firmwareVersionsCollectedData;
            } catch (Exception ex) {
                throw CommunicationException.protocolConnectFailed(ex);
            }
        }
        return super.getFirmwareVersions(serialNumber);
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }


}
