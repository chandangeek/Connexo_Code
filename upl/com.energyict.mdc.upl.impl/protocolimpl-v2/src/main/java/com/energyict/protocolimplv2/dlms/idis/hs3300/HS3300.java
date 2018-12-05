package com.energyict.protocolimplv2.dlms.idis.hs3300;

import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.MirrorTcpDeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
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
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.ResponseSecurityLevel;
import com.energyict.mdc.upl.security.SecuritySuite;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataEncryptionException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.hs3300.messages.HS3300Messaging;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300Properties;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.security.DlmsSecuritySuite1And2Support;
import com.energyict.protocolimplv2.security.SecurityPropertySpecTranslationKeys;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HS3300 extends AbstractDlmsProtocol implements SerialNumberSupport, AdvancedDeviceProtocolSecurityCapabilities {

    private static final int MANAGEMENT_CLIENT = 1;
    private static final int PUBLIC_CLIENT     = 16;

    private static final ObisCode FC_MANAGEMENT    = ObisCode.fromString("0.0.43.1.1.255");
    private static final ObisCode FC_DATA_READOUT  = ObisCode.fromString("0.0.43.1.2.255");
    private static final ObisCode FC_INSTALLATION  = ObisCode.fromString("0.0.43.1.5.255");
    private static final ObisCode FC_MAINTENANCE   = ObisCode.fromString("0.0.43.1.6.255");
    private static final ObisCode FC_CERTIFICATION = ObisCode.fromString("0.0.43.1.7.255");

    private final TariffCalendarExtractor calendarExtractor;
    private final NlsService nlsService;
    private final Converter converter;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private CertificateWrapperExtractor certificateWrapperExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    private HS3300Messaging deviceMessaging;
    private HS3300Cache deviceCache;

    public HS3300(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                  TariffCalendarExtractor calendarExtractor, NlsService nlsService, Converter converter,
                  DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                  KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.calendarExtractor           = calendarExtractor;
        this.nlsService                  = nlsService;
        this.converter                   = converter;
        this.messageFileExtractor        = messageFileExtractor;
        this.certificateWrapperExtractor = certificateWrapperExtractor;
        this.keyAccessorTypeExtractor    = keyAccessorTypeExtractor;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        getDeviceCache().setConnectionToBeaconMirror(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());
        getLogger().info("Start protocol for " + offlineDevice.getSerialNumber());
        getLogger().info("Version: " + getVersion());
        handleFC(comChannel);
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger()));
        getLogger().info("Protocol init successful");
    }

    @Override
    public String getVersion() {
        return "$Date: 2018-09-26 16:00:00 +0300 (Wed, 26 Sep 2018)$";
    }

    /**
     * Read out the serial number, this can either be of the module (equipment identifier) or of the connected e-meter.
     * Note that reading out this register from the mirror logical device in the Beacon, the ObisCode must always be 0.0.96.1.0.255
     */
    @Override
    public String getSerialNumber() {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect() || !getDlmsSessionProperties().useEquipmentIdentifierAsSerialNumber()) {
            return getMeterInfo().getSerialNr();
        } else {
            return getMeterInfo().getEquipmentIdentifier();
        }
    }

    /**
     * Connect to the device and check the cached object list.
     */
    @Override
    public void logOn() {
        connectWithRetries(getDlmsSession());
        checkCacheObjects();
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     * @param dlmsSession
     */
    protected void connectWithRetries(DlmsSession dlmsSession) {
        int tries = 0;
        while (true) {
            ProtocolRuntimeException exception;
            try {
                getLogger().info("Connecting with client "+dlmsSession.getProperties().getClientMacAddress()+" to "+dlmsSession.getProperties().getServerUpperMacAddress());
                dlmsSession.getDLMSConnection().setRetries(0);   //Temporarily disable retries in the connection layer, AARQ retries are handled here
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.getDlmsV2Connection().connectMAC();
                    dlmsSession.createAssociation((int) getDlmsSessionProperties().getAARQTimeout());
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
                dlmsSession.getDLMSConnection().setRetries(getDlmsSessionProperties().getRetries());
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > getDlmsSessionProperties().getAARQRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries.");
                throw CommunicationException.protocolConnectFailed(exception);
            } else {
                getLogger().info("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (ProtocolRuntimeException e) {
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
                dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
            }
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new SioOpticalConnectionType(this.getPropertySpecService()),
                new RxTxOpticalConnectionType(this.getPropertySpecService()),
                new OutboundTcpIpConnectionType(this.getPropertySpecService())
        );
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell HS3300 DLMS Meter";
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(
                new SerialDeviceProtocolDialect(this.getPropertySpecService(), nlsService), // HDLC
                new TcpDeviceProtocolDialect(this.getPropertySpecService(), nlsService),    // Gateway
                new MirrorTcpDeviceProtocolDialect(this.getPropertySpecService(), nlsService)); // Mirror
    }

    @Override
    public HS3300Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new HS3300Properties(this.getPropertySpecService(), nlsService, certificateWrapperExtractor);
        }
        return (HS3300Properties) dlmsProperties;
    }

    @Override
    public HS3300Cache getDeviceCache() {
        if (deviceCache == null) {
            deviceCache = new HS3300Cache(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());
        }
        return deviceCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if (deviceProtocolCache instanceof HS3300Cache) {
            deviceCache = (HS3300Cache) deviceProtocolCache;
        }
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return null;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return null;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getDeviceMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getDeviceMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDeviceMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getDeviceMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    private HS3300Messaging getDeviceMessaging() {
        if (this.deviceMessaging == null) {
            this.deviceMessaging = new HS3300Messaging(this, getCollectedDataFactory(), getIssueFactory(),
                    getPropertySpecService(), nlsService, converter, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
        }
        return this.deviceMessaging;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return null;
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Unless of course the whole session is done with the public client, then there's no need to read out the FC.
     */
    protected void handleFC(ComChannel comChannel) {
        if (getDlmsSessionProperties().getAuthenticationSecurityLevel() < 5) {
            getLogger().info("Skipping FC handling due to lower security level.");
            return; // no need to handle any FC
        }

        if (!getDlmsSessionProperties().usesPublicClient()) {
            final int clientId = getDlmsSessionProperties().getClientMacAddress();
            //validateFCProperties(clientId);

            boolean weHaveValidCachedFrameCounter = false;
            if (getDlmsSessionProperties().useCachedFrameCounter()) {
                weHaveValidCachedFrameCounter = getCachedFrameCounter(comChannel, clientId);
            }

            if (!weHaveValidCachedFrameCounter) {
                //if (getDlmsSessionProperties().getRequestAuthenticatedFrameCounter() & (clientId != EVN_CLIENT_MANAGEMENT)) {
                    readFrameCounterSecure(comChannel);
                /*} else {
                    try {
                        //Attempt to read out the FC with the public client.
                        //Note that this possible for every client of the DEWA AM540, but not for the management client of the EVN AM540. The object_undefined error in that case is handled below.
                        getLogger().info("Attempting to read out frame counter using unsecured public client");
                        super.readFrameCounter(comChannel, (int) getDlmsSessionProperties().getAARQTimeout());
                    } catch (CommunicationException e) {
                        if (e.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE
                                || e.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_PROTOCOL_ERROR) {
                            getLogger().warning(e.getMessage());

                            //Abort session, the FC cannot be read out using the public client on the EVN AM540.
                            if (clientId == EVN_CLIENT_MANAGEMENT) {
                                if (!getDlmsSessionProperties().useCachedFrameCounter()) {
                                    throw CommunicationException.protocolConnectFailed(new IOException("Reading frame counter for client " + EVN_CLIENT_MANAGEMENT +
                                            " is not allowed. Enable property '" + AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER + "' to use the cached FC"));
                                } else {
                                    throw CommunicationException.protocolConnectFailed(new IOException("Could not create the DLMS association to the device. Possibly the cached frame counter is wrong, but it cannot be read out for the management client. Please check all security related properties and keys."));
                                }
                            } else {
                                throw CommunicationException.protocolConnectFailed(new IOException("Cannot read out the FC of client '" + clientId + "' using the public client. Enable property '" + AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER + "' to read it out using HMAC authentication."));
                            }
                        } else {
                            //Another communication exception, propagate
                            throw e;
                        }
                    }
                }*/
            }
        }
    }

    private String getNull() {
        return null;
    }

    /**
     * Read frame counter by calling a custom method in the Beacon
     */
    protected void readFrameCounterSecure(ComChannel comChannel) {
        getLogger().info("Reading frame counter using secure method");

        byte[] authenticationKey = getDlmsSessionProperties().getSecurityProvider().getAuthenticationKey();
        if (authenticationKey.length != 16) {
            throw DeviceConfigurationException.unsupportedPropertyValueLengthWithReason(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), String.valueOf(authenticationKey.length * 2), "Need a plain text AuthenticationKey (32 hex chars) to read out the frame counter securely using HMAC");
        }

        // construct a temporary session with 0:0 security and clientId=16 (public)
        final TypedProperties publicProperties = TypedProperties.copyOf(getDlmsSessionProperties().getProperties());
        publicProperties.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
        final HS3300Properties publicClientProperties = new HS3300Properties(this.getPropertySpecService(), nlsService, certificateWrapperExtractor);
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT), 0, 0, 0, 0, 0, publicProperties));    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getNull()); // TODO getDlmsSessionProperties().getSerialNumber());
        final ObisCode frameCounterObisCode = getFrameCounterForClient(getDlmsSessionProperties().getClientMacAddress());
        final long frameCounter;

        publicDlmsSession.getDlmsV2Connection().connectMAC();
        publicDlmsSession.createAssociation((int) getDlmsSessionProperties().getAARQTimeout());

        try {

            FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
            frameCounterProvider.setSkipValidation(this.getDlmsSessionProperties().skipFramecounterAuthenticationTag());

            frameCounter = frameCounterProvider.getFrameCounter(authenticationKey);

            getLogger().info("The read-out frame-counter is: " + frameCounter);

        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        } catch (Exception e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the secure frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } finally {
            publicDlmsSession.disconnect();
        }

        setTXFrameCounter(frameCounter + 1);
    }

    /**
     * Get the frame counter from the cache, for the given clientId.
     * If no frame counter is available in the cache (value -1), use the configured InitialFC property.
     * <p/>
     * Additionally, the FC value can be validated with ValidateCachedFrameCounterAndFallback
     */
    private boolean getCachedFrameCounter(ComChannel comChannel, int clientId) {
        getLogger().info("Will try to use a cached frame counter");
        boolean weHaveAFrameCounter = false;
        long cachedFrameCounter = getDeviceCache().getTXFrameCounter(clientId);
        long initialFrameCounter = getDlmsSessionProperties().getInitialFrameCounter();

        if (initialFrameCounter > cachedFrameCounter) { //Note that this is also the case when the cachedFrameCounter is unavailable (value -1).
            getLogger().info("Using initial frame counter: " + initialFrameCounter + " because it has a higher value than the cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(initialFrameCounter);
            weHaveAFrameCounter = true;
        } else if (cachedFrameCounter > 0) {
            getLogger().info("Using cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(cachedFrameCounter + 1);
            weHaveAFrameCounter = true;
        }

        if (weHaveAFrameCounter) {
            if (getDlmsSessionProperties().validateCachedFrameCounter()) {
                return testConnectionAndRetryWithFrameCounterIncrements(comChannel);
            } else {
                getLogger().warning(" - cached frame counter will not be validated - if the communication fails please set the cache property back to {No}, so a fresh one will be read-out");
                // do not validate, just use it and hope for the best
                return true;
            }
        }

        return false;
    }

    private void setTXFrameCounter(long frameCounter) {
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    private boolean testConnectionAndRetryWithFrameCounterIncrements(ComChannel comChannel) {
        DlmsSession testDlmsSession = getDlmsSessionForFCTesting(comChannel);
        int retries = getDlmsSessionProperties().getFrameCounterRecoveryRetries();
        int step = getDlmsSessionProperties().getFrameCounterRecoveryStep();
        boolean releaseOnce = true;

        getLogger().info("Will test the frameCounter. Recovery mechanism: retries=" + retries + ", step=" + step);
        if (retries <= 0) {
            retries = 0;
            step = 0;
        }

        do {
            try {
                testDlmsSession.getDlmsV2Connection().connectMAC();
                testDlmsSession.createAssociation();
                if (testDlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                    testDlmsSession.disconnect();
                    getLogger().info("Cached FrameCounter is valid!");
                    setTXFrameCounter(testDlmsSession.getAso().getSecurityContext().getFrameCounter());
                    return true;
                }
            } catch (CommunicationException ex) {
                if (isAssociationFailed(ex)) {
                    long frameCounter = testDlmsSession.getAso().getSecurityContext().getFrameCounter();
                    getLogger().warning("Current frame counter [" + frameCounter + "] is not valid, received exception " + ex.getMessage() + ", increasing frame counter by " + step);
                    frameCounter += step;
                    setTXFrameCounter(frameCounter);
                    testDlmsSession.getAso().getSecurityContext().setFrameCounter(frameCounter);

                    if (releaseOnce) {
                        releaseOnce = false;
                        //Try to release that association once, it may be that it was still open from a previous session, causing troubles to create the new association.
                        try {
                            testDlmsSession.getAso().releaseAssociation();
                        } catch (ProtocolRuntimeException e) {
                            testDlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                            // Absorb exception: in 99% of the cases we expect an exception here ...
                        }
                    }
                } else {
                    throw ex;       //Propagate any other exception
                }
            }
            retries--;
        } while (retries > 0);

        testDlmsSession.disconnect();
        getLogger().warning("Could not validate the frame counter, seems that it's out-of sync with the device. You'll have to read a fresh one.");
        return false;
    }

    private boolean isAssociationFailed(CommunicationException ex) {
        return ex.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE
                || ex.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_PROTOCOL_ERROR
                || ex.getMessageSeed() == ProtocolExceptionMessageSeeds.PROTOCOL_CONNECT;
    }

    /**
     * Sub classes (for example the crypto-protocol) can override
     */
    private DlmsSession getDlmsSessionForFCTesting(ComChannel comChannel) {
        return new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     */
    private void readFrameCounter(ComChannel comChannel) {
        TypedProperties clone = TypedProperties.copyOf(getDlmsSessionProperties().getProperties());
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
        HS3300Properties publicClientProperties = new HS3300Properties(this.getPropertySpecService(), nlsService, certificateWrapperExtractor);
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT), 0, 0, 0, 0, 0, clone)); // SecurityLevel 0:0

        long frameCounter;
        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        getLogger().info("Connecting to public client:" + PUBLIC_CLIENT);
        connectWithRetries(publicDlmsSession);
        try {
            ObisCode frameCounterObisCode = getFrameCounterForClient(getDlmsSessionProperties().getClientMacAddress());
            getLogger().info("Public client connected, reading frame-counter " + frameCounterObisCode.toString() + ", corresponding to client " + getDlmsSessionProperties().getClientMacAddress());
            FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
            frameCounter = frameCounterProvider.getFrameCounter(publicDlmsSession.getProperties().getSecurityProvider().getAuthenticationKey());
            getLogger().info("Frame counter received: " + frameCounter);
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame-counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        } finally {
            getLogger().info("Disconnecting public client");
            publicDlmsSession.disconnect();
        }

        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    private ObisCode getFrameCounterForClient(int clientId) {
        // TODO other client IDs
        switch (clientId) {
            case MANAGEMENT_CLIENT:
                return FC_MANAGEMENT;
            case PUBLIC_CLIENT:
            default:
                return FC_MANAGEMENT;
        }
    }

    /**
     * A collection of general AM500 properties.
     * These properties are not related to the security or the protocol dialects.
     */
    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = getNewInstanceOfConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }

    protected HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new HS3300ConfigurationSupport(this.getPropertySpecService());
    }

    @Override
    protected AdvancedDeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new DlmsSecuritySuite1And2Support(this.getPropertySpecService());
        }
        return (AdvancedDeviceProtocolSecurityCapabilities) dlmsSecuritySupport;
    }

    @Override
    public List<SecuritySuite> getSecuritySuites() {
        return getSecuritySupport().getSecuritySuites();
    }

    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return getSecuritySupport().getRequestSecurityLevels();
    }

    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return getSecuritySupport().getResponseSecurityLevels();
    }
}
