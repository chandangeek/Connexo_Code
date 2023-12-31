package com.energyict.protocolimplv2.dlms.a2;

import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.ApplicationServiceObjectV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataEncryptionException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a2.events.A2LogBookFactory;
import com.energyict.protocolimplv2.dlms.a2.messages.A2Messaging;
import com.energyict.protocolimplv2.dlms.a2.profile.A2ProfileDataReader;
import com.energyict.protocolimplv2.dlms.a2.properties.A2ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.a2.properties.A2Properties;
import com.energyict.protocolimplv2.dlms.a2.registers.A2RegisterFactory;
import com.energyict.protocolimplv2.dlms.a2.registers.EIStoredValues;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 * @author H236365
 * @since 12/11/2018
 */
public class A2 extends AbstractDlmsProtocol {
    private static final ObisCode FIRMWARE_VERSION_APPLICATION_OBIS_CODE = ObisCode.fromString("7.1.0.2.1.255");
    private static final ObisCode BOOTLOADER_AUXILIARY_FIRMWARE_VERSION = ObisCode.fromString("7.2.0.2.1.255");

    public final static int PUBLIC_CLIENT = 16;
    public final static int INSTALLER_MAINTAINER_CLIENT = 3;
    public final static int MANAGEMENT_CLIENT = 1;

    private final static int ADDRESSING_MODE = 1;
    protected final static ObisCode FRAME_COUNTER_MANAGEMENT_ONLINE = ObisCode.fromString("0.0.43.1.1.255");
    private final static ObisCode FRAME_COUNTER_MANAGEMENT_OFFLINE = ObisCode.fromString("0.1.43.1.1.255");
    public final static ObisCode COSEM_LOGICAL_DEVICE_NAME = ObisCode.fromString("0.0.42.0.0.255");

    protected A2ProfileDataReader profileDataReader = null;
    protected A2RegisterFactory registerFactory = null;
    private A2LogBookFactory logBookFactory = null;
    private A2Messaging messaging = null;
    private A2HHUSignOn hhuSignOnV2 = null;

    private final Converter converter;
    private final NlsService nlsService;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    protected EIStoredValues storedValues = null;

    public A2(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
              NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor,
              KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.converter = converter;
        this.messageFileExtractor = messageFileExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());

        journal("Start protocol for " + offlineDevice.getSerialNumber());
        journal("-version: " + getVersion());
        if (ComChannelType.SerialComChannel.equals(comChannel.getComChannelType()) || ComChannelType.OpticalComChannel.equals(comChannel.getComChannelType())) {
            getDlmsSessionProperties().getProperties().setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
            getHHUSignOn((SerialPortComChannel) comChannel);
            setupSession(comChannel, FRAME_COUNTER_MANAGEMENT_OFFLINE);
        } else {
            setupSession(comChannel, FRAME_COUNTER_MANAGEMENT_ONLINE);
        }
        journal("Protocol initialization phase ended, executing tasks ...");
    }


    @Override
    public void logOn() {
        getDlmsSession().assumeConnected(getDlmsSessionProperties().getMaxRecPDUSize(), getDlmsSessionProperties().getConformanceBlock());
        getDlmsSession().getDlmsV2Connection().connectMAC();
        getDlmsSession().getDLMSConnection().setInvokeIdAndPriorityHandler(
                new IncrementalInvokeIdAndPriorityHandler(new InvokeIdAndPriority((byte) 0x41)));
        checkCacheObjects();
    }

    @Override
    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = getDeviceCache();
        if (dlmsCache.getObjectList() == null) {
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            UniversalObject[] objectList = dlmsCache.getObjectList();
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(objectList);
        }
    }

    protected void setupSession(ComChannel comChannel, ObisCode frameCounterObisCode) {
        long frameCounter;
        String logicalDeviceName = this.offlineDevice.getSerialNumber();
        DlmsSession publicDlmsSession = getPublicDlmsSession(comChannel, getDlmsProperties());
        try {
            frameCounter = getFrameCounter(publicDlmsSession, frameCounterObisCode);
            if (hhuSignOnV2 != null) {
                logicalDeviceName = getLogicalDeviceName(publicDlmsSession);
                checkDeviceName(logicalDeviceName);
            }
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        } finally {
            journal("Disconnecting public client");
            publicDlmsSession.disconnect();
        }
        getDlmsSessionProperties().setSerialNumber(logicalDeviceName);
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
        getDlmsSessionProperties().getProperties().setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(MANAGEMENT_CLIENT));
        if (hhuSignOnV2 != null) {
            hhuSignOnV2.setClientMacAddress(MANAGEMENT_CLIENT);
        }
        setDlmsSession(createDlmsSession(comChannel, getDlmsSessionProperties()));
    }

    protected long getFrameCounter(DlmsSession publicDlmsSession, ObisCode frameCounterObiscode) throws IOException {
        journal("Public client connected, reading frame counter " + frameCounterObiscode.toString() + ", corresponding to client " + getDlmsSessionProperties().getClientMacAddress());
        long frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObiscode).getValueAttr().longValue();
        journal("Frame counter received: " + frameCounter);
        return frameCounter;
    }

    protected String getLogicalDeviceName(DlmsSession publicDlmsSession) throws IOException {
        journal("Reading COSEM logical device name " + COSEM_LOGICAL_DEVICE_NAME.toString() + ", corresponding to client " + getDlmsSessionProperties().getClientMacAddress());
        String logicalDeviceName = getLogDevName(publicDlmsSession);
        journal("COSEM logical device name received: " + logicalDeviceName);
        return logicalDeviceName;
    }

    private String getLogDevName(DlmsSession publicDlmsSession) throws IOException {
        return publicDlmsSession.getCosemObjectFactory().getData(COSEM_LOGICAL_DEVICE_NAME).getValueAttr().getOctetString().stringValue();
    }

    protected DlmsSession getPublicDlmsSession(ComChannel comChannel, DlmsProperties publicClientProperties) {
        DlmsSession publicDlmsSession = createDlmsSession(comChannel, publicClientProperties);
        journal("Connecting to public client:" + PUBLIC_CLIENT);
        connectWithRetries(publicDlmsSession);
        return publicDlmsSession;
    }

    public A2DlmsSession createDlmsSession(ComChannel comChannel, DlmsProperties dlmsSessionProperties) {
        return new A2DlmsSession(comChannel, dlmsSessionProperties, hhuSignOnV2, offlineDevice.getSerialNumber());
    }

    protected DlmsProperties getDlmsProperties() {
        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
        clone.setProperty(DlmsProtocolProperties.ADDRESSING_MODE, BigDecimal.valueOf(ADDRESSING_MODE));
        DlmsProperties publicClientProperties = new DlmsProperties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(
                new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT), 0, 0, 0, 0, 0, clone)
        );  // SecurityLevel 0:0
        return publicClientProperties;
    }

    protected void checkDeviceName(String logicalDeviceName) {
        if (!logicalDeviceName.equals(getDlmsSessionProperties().getDeviceId())) {
            CommunicationException exception = CommunicationException.unexpectedPropertyValue(DlmsProtocolProperties.DEVICE_ID, logicalDeviceName, getDlmsSessionProperties().getDeviceId());
            journal(Level.SEVERE, exception.getMessage());
            throw exception;
        }
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     *
     * @param dlmsSession DlmsSession to use
     */
    @Override
    protected void connectWithRetries(DlmsSession dlmsSession) {
        int tries = 0;
        while (true) {
            ProtocolRuntimeException exception;
            try {
                ApplicationServiceObjectV2 aso = dlmsSession.getAso();
                if (aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.getDlmsV2Connection().connectMAC();
                    dlmsSession.createAssociation();
                }
                return;
            } catch (ProtocolRuntimeException e) {
                journal(Level.WARNING, e.getMessage(), e);
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;    // Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (e instanceof ConnectionCommunicationException) {
                    throw e;
                } else if (e instanceof DataEncryptionException) {
                    throw e;
                }
                exception = e;
            } finally {
                dlmsSession.getDLMSConnection().setRetries(getDlmsSessionProperties().getRetries());
            }

            // Release and retry the AARQ in case of ACSE exception
            if (++tries > dlmsSession.getProperties().getRetries()) {
                journal(Level.SEVERE, "Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries.");
                throw CommunicationException.protocolConnectFailed(exception);
            } else {
                journal("Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (ProtocolRuntimeException e) {
                    dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
            }
        }
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        // "The communication is HDLC "ab initio" and is not using the startup with IEC 1107 protocol."
        hhuSignOnV2 = new A2HHUSignOn(serialPortComChannel, getDlmsProperties());
        hhuSignOnV2.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOnV2.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOnV2.enableDataReadout(false);
        return hhuSignOnV2;
    }

    @Override
    protected void readObjectList() {
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(new A2ObjectList().getObjectList());
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(new InboundIpConnectionType(), new SioOpticalConnectionType(getPropertySpecService()), new RxTxOpticalConnectionType(getPropertySpecService()));
    }

    @Override
    public void setTime(Date timeToSet, ClockChangeMode changeMode) {
        try {
            if (ClockChangeMode.SYNC.equals(changeMode)) {
                if (isTimeIntervalOverClockSync() && isTimeSyncAroundInterval(timeToSet)) {
                    journal("Time sync not performed due to hourly interval change on the meter");
                    return;
                }
            }
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSessionProperties().getRetries() + 1);
        }
    }

    protected boolean isTimeIntervalOverClockSync() throws IOException {
        return getDlmsSessionProperties().isTimeIntervalOverClockSync();
    }

    protected boolean isTimeSyncAroundInterval(Date timeToSet) throws IOException {
        Calendar calToSet = Calendar.getInstance();
        Calendar calToGet = Calendar.getInstance();
        calToSet.setTime(timeToSet);
        calToGet.setTime(getDlmsSession().getCosemObjectFactory().getClock().getDateTime());
        if (calToSet.get(Calendar.HOUR_OF_DAY) == calToGet.get(Calendar.HOUR_OF_DAY)) {
            return false;
        }
        return true;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getProfileDataReader().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        return getProfileDataReader().getLoadProfileData(loadProfileReaders);
    }

    public A2ProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new A2ProfileDataReader(this, getCollectedDataFactory(), getIssueFactory(), getOfflineDevice(), getDlmsSessionProperties().getLimitMaxNrOfDays());
        }
        return profileDataReader;
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
    public String getSerialNumber() {
        // The configured serial number in EiServer should match the Logical device name of the device not the manufacturing number
        try {
            return getLogDevName(getDlmsSession());
        } catch (IOException e) {
            journal(Level.SEVERE, e.getLocalizedMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
        }
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

    protected A2LogBookFactory createLogBookFactory() {
        return new A2LogBookFactory(this, getCollectedDataFactory(), getIssueFactory());
    }

    public Converter getConverter() {
        return converter;
    }

    public KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {
        return keyAccessorTypeExtractor;
    }

    public NlsService getNlsService() {
        return nlsService;
    }

    public A2HHUSignOn getHhuSignOnV2() {
        return hhuSignOnV2;
    }

    public DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
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
        return getProtocolMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return getProtocolMessaging().prepareMessageContext(device, offlineDevice, deviceMessage);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(new SerialDeviceProtocolDialect(getPropertySpecService(), nlsService), new TcpDeviceProtocolDialect(getPropertySpecService(), nlsService));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    public A2RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new A2RegisterFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public String getProtocolDescription() {
        return "EI6 pre-2021 ThemisUno DLMS Protocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-11-24$";
    }

    protected A2Messaging getProtocolMessaging() {
        if (messaging == null) {
            messaging = createMessaging();
        }
        return messaging;
    }

    protected A2Messaging createMessaging() {
        return new A2Messaging(this, getPropertySpecService(), getNlsService(), getConverter(), getMessageFileExtractor(), getKeyAccessorTypeExtractor());
    }

    @Override
    public A2Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new A2Properties();
        }
        return (A2Properties) dlmsProperties;
    }

    /**
     * A collection of general DLMS properties.
     * These properties are not related to the security or the protocol dialects.
     */
    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new A2ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    @Override
    public boolean useDsmr4SelectiveAccessFormat() {
        return true;
    }

    public StoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new EIStoredValues(this);
        }
        return storedValues;
    }

    @Override
    public boolean supportsAuxiliaryFirmwareVersion() {
        return true;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions(String serialNumber) {
        if (offlineDevice.getSerialNumber().equals(serialNumber)) {
            CollectedFirmwareVersion firmwareVersionsCollectedData = getCollectedDataFactory().createFirmwareVersionsCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getActiveMeterFirmwareVersion());
            firmwareVersionsCollectedData.setActiveAuxiliaryFirmwareVersion(getBootloaderAuxiliaryMeterFirmwareVersion());
            return firmwareVersionsCollectedData;
        }
        return super.getFirmwareVersions(serialNumber);
    }

    public String getActiveMeterFirmwareVersion() {
        return getFirmwareVersion(FIRMWARE_VERSION_APPLICATION_OBIS_CODE);
    }

    public String getBootloaderAuxiliaryMeterFirmwareVersion() {
        return getFirmwareVersion(BOOTLOADER_AUXILIARY_FIRMWARE_VERSION);
    }

    public String getFirmwareVersion(ObisCode firmwareObiscode) {
        try {
            journal("Collecting firmware version from " + firmwareObiscode);
            Data firmwareData = getDlmsSession().getCosemObjectFactory().getData(firmwareObiscode);
            AbstractDataType valueAttr = firmwareData.getValueAttr();
            if (valueAttr.isOctetString()) {
                return (getRegisterFactory().decodeFirmwareVersion(valueAttr.getOctetString()));
            }
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
        return "";
    }
}