package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.cbo.ObservationDateProperty;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.cosem.PLCOFDMType2MACSetup;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.TLSConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.tasks.DeviceConnectionFunction;
import com.energyict.mdc.tasks.GatewayTcpDeviceProtocolDialect;
import com.energyict.mdc.tasks.MirrorTcpDeviceProtocolDialect;
import com.energyict.mdc.upl.*;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.TopologyPathSegment;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.migration.MigratePropertiesFromPreviousSecuritySet;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.utils.IPv6Utils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.firmware.BeaconFirmwareSignatureCheck;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks.Beacon3100LogBookFactory;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.Beacon3100Messaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.profiles.BeaconProfileDataReader;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100SecurityProvider;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers.Beacon3100RegisterFactory;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.G3Neighbor;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.G3NeighborList;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.G3Node;

import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodeNodeModulationScheme;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodePhaseInfo;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodeTxModulation;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.security.DlmsSecuritySuite1And2Support;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;
import com.energyict.protocolimplv2.security.SecurityPropertySpecTranslationKeys;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.macAddress;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/06/2015 - 15:07
 */
public class Beacon3100 extends AbstractDlmsProtocol implements MigratePropertiesFromPreviousSecuritySet, AdvancedDeviceProtocolSecurityCapabilities {

    // https://confluence.eict.vpdc/display/G3IntBeacon3100/DLMS+management
    // https://jira.eict.vpdc/browse/COMMUNICATION-1552
    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final String MIRROR_LOGICAL_DEVICE_PREFIX = "ELS-MIR-";
    private static final String GATEWAY_LOGICAL_DEVICE_PREFIX = "ELS-UGW-";
    private static final String UTF_8 = "UTF-8";
    private static final int MAC_ADDRESS_LENGTH = 8;    //In bytes
    private final NlsService nlsService;
    private final Converter converter;
    private final ObjectMapperService objectMapperService;
    private final DeviceMasterDataExtractor extractor;
    private final DeviceGroupExtractor deviceGroupExtractor;
    private final CertificateWrapperExtractor certificateWrapperExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private final DeviceExtractor deviceExtractor;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;
    protected Beacon3100Messaging beacon3100Messaging;
    private BeaconCache beaconCache = null;
    private Beacon3100RegisterFactory registerFactory;
    private Beacon3100LogBookFactory logBookFactory;
    private BeaconProfileDataReader beaconProfileDataReader;
    private Array neighbourTable = null;
    private Set<String> topologySegments = new LinkedHashSet<>();
    private List<CollectedLoadProfileConfiguration> loadProfileConfigurations;
    private String logPrefix = "";


    public Beacon3100(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, ObjectMapperService objectMapperService, DeviceMasterDataExtractor extractor, DeviceGroupExtractor deviceGroupExtractor, CertificateWrapperExtractor certificateWrapperExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor, DeviceExtractor deviceExtractor, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.converter = converter;
        this.objectMapperService = objectMapperService;
        this.extractor = extractor;
        this.deviceGroupExtractor = deviceGroupExtractor;
        this.certificateWrapperExtractor = certificateWrapperExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
        this.deviceExtractor = deviceExtractor;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    protected NlsService getNlsService() {
        return nlsService;
    }

    protected Converter getConverter() {
        return converter;
    }

    protected ObjectMapperService getObjectMapperService() {
        return objectMapperService;
    }

    protected DeviceMasterDataExtractor getExtractor() {
        return extractor;
    }

    protected DeviceGroupExtractor getDeviceGroupExtractor() {
        return deviceGroupExtractor;
    }

    protected CertificateWrapperExtractor getCertificateWrapperExtractor() {
        return certificateWrapperExtractor;
    }

    protected KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {
        return keyAccessorTypeExtractor;
    }

    protected DeviceExtractor getDeviceExtractor() {
        return deviceExtractor;
    }

    protected DeviceMessageFileExtractor getDeviceMessageFileExtractor() {
        return deviceMessageFileExtractor;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        this.logPrefix = "[" + offlineDevice.getSerialNumber() + "] ";
        logInfo(logPrefix + "Start Beacon 3200 protocol for " + offlineDevice.getSerialNumber() + "(" + getVersion() + ")");
        handleFrameCounter(comChannel);
        initDlmsSession(comChannel);
    }

    /**
     * Can be overridden by the crypto-protocols
     */
    protected void initDlmsSession(ComChannel comChannel) {
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    /**
     * Either get the FC from the device cache, or read it out using the public client. This can be configured with general properties.
     * Unless of course the whole session is done with the public client, then there's no need to read out the FC.
     */
    protected void handleFrameCounter(ComChannel comChannel) {
        if (!frameCounterReadoutRequired()) {
            logInfo("Skipping FC handling due to lower security level.");
            return; // there is no need to read-out the frame-counter
        }

        int clientMacAddress = getDlmsSessionProperties().getClientMacAddress();
        if (clientMacAddress != ClientConfiguration.PUBLIC.clientId) {
            boolean weHaveValidCachedFrameCounter = false;
            if (getDlmsSessionProperties().useCachedFrameCounter()) {
                weHaveValidCachedFrameCounter = getCachedFrameCounter(comChannel, clientMacAddress);
            }

            if (!weHaveValidCachedFrameCounter) {
                //No cached FC available. Read it out using the public client.
                readFrameCounter(comChannel);
            }
        }
    }

    private boolean frameCounterReadoutRequired() {
        return getDlmsSessionProperties().getAuthenticationSecurityLevel() >= 5;
    }

    @Override
    public BeaconCache getDeviceCache() {
        if (beaconCache == null) {
            beaconCache = new BeaconCache();
        }
        return beaconCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if (deviceProtocolCache instanceof BeaconCache) {
            beaconCache = (BeaconCache) deviceProtocolCache;
        }
    }

    @Override
    public void terminate() {
        //As a last step, update the cache with the last FC
        if (getDlmsSession() != null && getDlmsSession().getAso() != null && getDlmsSession().getAso().getSecurityContext() != null) {
            getDeviceCache().setTXFrameCounter(getDlmsSessionProperties().getClientMacAddress(), getDlmsSession().getAso().getSecurityContext().getFrameCounter());
        }
    }

    /**
     * Get the frame counter from the cache, for the given clientId.
     * If no frame counter is available in the cache (value -1), use the configured InitialFC property.
     * <p/>
     * Additionally, the FC value can be validated with ValidateCachedFrameCounterAndFallback
     */
    private boolean getCachedFrameCounter(ComChannel comChannel, int clientId) {
        getLogger().info(logPrefix + "Will try to use a cached frame counter");
        boolean weHaveAFrameCounter = false;
        long cachedFrameCounter = getDeviceCache().getTXFrameCounter(clientId);
        long initialFrameCounter = getDlmsSessionProperties().getInitialFrameCounter();

        if (initialFrameCounter > cachedFrameCounter) { //Note that this is also the case when the cachedFrameCounter is unavailable (value -1).
            logInfo("Using initial frame counter: " + initialFrameCounter + " because it has a higher value than the cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(initialFrameCounter);
            weHaveAFrameCounter = true;
        } else if (cachedFrameCounter > 0) {
            logInfo("Using cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(cachedFrameCounter + 1);
            weHaveAFrameCounter = true;
        }

        if (weHaveAFrameCounter) {
            if (getDlmsSessionProperties().validateCachedFrameCounter()) {
                return testConnectionAndRetryWithFrameCounterIncrements(comChannel);
            } else {
                logInfo(" - cached frame counter will not be validated - if the communication fails please set the cache property back to {No}, so a fresh one will be read-out");
                // do not validate, just use it and hope for the best
                return true;
            }
        }

        return false;
    }

    private boolean testConnectionAndRetryWithFrameCounterIncrements(ComChannel comChannel) {
        DlmsSession testDlmsSession = getDlmsSessionForFCTesting(comChannel);
        int retries = getDlmsSessionProperties().getFrameCounterRecoveryRetries();
        int step = getDlmsSessionProperties().getFrameCounterRecoveryStep();
        boolean releaseOnce = true;

        logInfo("Will test the frameCounter. Recovery mechanism: retries=" + retries + ", step=" + step);
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
                    logInfo("Cached FrameCounter is valid!");
                    setTXFrameCounter(testDlmsSession.getAso().getSecurityContext().getFrameCounter());
                    return true;
                }
            } catch (CommunicationException ex) {
                if (isAssociationFailed(ex)) {
                    long frameCounter = testDlmsSession.getAso().getSecurityContext().getFrameCounter();
                    logWarn("Current frame counter [" + frameCounter + "] is not valid, received exception " + ex.getMessage() + ", increasing frame counter by " + step);
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
        logWarn("Could not validate the frame counter, seems that it's out-of sync with the device. You'll have to read a fresh one.");
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
    protected DlmsSession getDlmsSessionForFCTesting(ComChannel comChannel) {
        return new DlmsSession(comChannel, getDlmsSessionProperties());
    }

    private void setTXFrameCounter(long frameCounter) {
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    /**
     * Will return the correct frame counter obis code, for each client ID.
     *
     * @param clientId - DLMS Client ID used in association
     * @return - the correct obis code for this client
     */
    protected ObisCode getFrameCounterObisCode(final int clientId) {
        final ClientConfiguration client = ClientConfiguration.getByID(clientId);

        if (client == null) {
            throw new IllegalArgumentException("No client with ID [" + clientId + "] defined for this device !");
        }

        return client.getFrameCounterOBIS();
    }

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

    /**
     * First read out the frame counter for the management client, using the public client. It has a pre-established association.
     * Note that this happens without setting up an association, since the it's pre-established for the public client.
     * <p/>
     * For EVN we'll read the frame counter using the frame counter provider custom method in the beacon
     */
    protected void readFrameCounter(ComChannel comChannel) {
        //TODO: uncoment this once we have sepparate FC for agreed, dedicated and global key. for now global FC is always used
        /*
        if (this.usesSessionKey()) {
            //No need to read out the global FC if we're going to use a new session key in this AA.
            return;
        }*/

        if (getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()) {
            byte[] authenticationKey = getDlmsSessionProperties().getSecurityProvider().getAuthenticationKey();
            if (authenticationKey.length != ((Beacon3100SecurityProvider) getDlmsSessionProperties().getSecurityProvider()).getKeyLength()) {
                throw DeviceConfigurationException.unsupportedPropertyValueLengthWithReason(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), String.valueOf(authenticationKey.length * 2), "Need a plain text AuthenticationKey (32 hex chars) to read out the frame counter securely using HMAC");
            }
        }

        final Beacon3100Properties publicClientProperties = new Beacon3100Properties(certificateWrapperExtractor);

        publicClientProperties.addProperties(com.energyict.mdc.upl.TypedProperties.copyOf(getDlmsSessionProperties().getProperties()));
        publicClientProperties.getProperties().setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS,
                BigDecimal.valueOf(ClientConfiguration.PUBLIC.clientId));

        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(ClientConfiguration.PUBLIC.clientId), 0, 0, 0, 0, 0, getDlmsSessionProperties().getProperties()));    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsSessionProperties().getSerialNumber());

        final long frameCounter;

        final com.energyict.dlms.protocolimplv2.DlmsSessionProperties sessionProperties = this.getDlmsSessionProperties();

        if (this.getDlmsSessionProperties().isPublicClientPreEstablished() && this.getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()) {
            if (this.getLogger().isLoggable(Level.WARNING)) {
                logWarn("Invalid configuration detected : cannot use a pre-established public client association in combination with and authenticated frame counter, overriding to non-pre-established.");
            }
        }
        final boolean preEstablished = sessionProperties.isPublicClientPreEstablished() && !this.getDlmsSessionProperties().getRequestAuthenticatedFrameCounter();

        try {
            // Associate if necessary.
            if (preEstablished) {
                if (this.getLogger().isLoggable(Level.FINE)) {
                    this.getLogger().log(Level.FINE, "Public client association is pre-established.");
                }

                publicDlmsSession.assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());
            } else {
                if (this.getLogger().isLoggable(Level.FINE)) {
                    this.getLogger().log(Level.FINE, "Public client association is not pre-established.");
                }

                publicDlmsSession.getDlmsV2Connection().connectMAC();
                publicDlmsSession.createAssociation();
            }

            // Then read out the frame counter.
            final ObisCode frameCounterObisCode = this.getFrameCounterObisCode(getDlmsSessionProperties().getClientMacAddress());

            if (getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()) {
                logInfo("Requesting authenticated frame counter");
                try {
                    FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
                    frameCounter = frameCounterProvider.getFrameCounter(publicDlmsSession.getProperties().getSecurityProvider().getAuthenticationKey());
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, logPrefix + e.getCause() + e.getMessage(), e);
                    throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
                } catch (Exception e) {
                    getLogger().log(Level.SEVERE, logPrefix + e.getCause() + e.getMessage(), e);
                    final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame counter, cannot continue! " + e.getMessage());
                    throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
                }
            } else {
                try {
                    frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, logPrefix + e.getCause() + e.getMessage(), e);
                    throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
                }
            }
        } finally {
            // Only disconnect if the association is not pre-established.
            if (!preEstablished) {
                publicDlmsSession.disconnect();
            }
        }

        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    /**
     * General ciphering (wrapped-key and agreed-key) are sessions keys
     */
    private boolean usesSessionKey() {
        return getDlmsSessionProperties().getCipheringType().equals(CipheringType.GENERAL_CIPHERING) && getDlmsSessionProperties().getGeneralCipheringKeyType() != GeneralCipheringKeyType.IDENTIFIED_KEY;
    }

    @Override
    public String getSerialNumber() {
        try {
            return getDlmsSession().getCosemObjectFactory().getData(SERIAL_NUMBER_OBISCODE).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<UPLConnectionFunction> getConsumableConnectionFunctions() {
        return Arrays.asList(DeviceConnectionFunction.MIRROR, DeviceConnectionFunction.GATEWAY, DeviceConnectionFunction.INBOUND);
    }

    @Override
    public List<UPLConnectionFunction> getProvidedConnectionFunctions() {
        return Arrays.asList(DeviceConnectionFunction.MIRROR, DeviceConnectionFunction.GATEWAY, DeviceConnectionFunction.INBOUND);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new OutboundTcpIpConnectionType(this.getPropertySpecService()),
                new InboundIpConnectionType(),
                new TLSConnectionType(
                        this.getPropertySpecService(),
                        this.certificateWrapperExtractor));
    }

    @Override
    public String getProtocolDescription() {
        return "Elster EnergyICT Beacon3100 G3 DLMS";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getBeaconProfileDataReader().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    private BeaconProfileDataReader getBeaconProfileDataReader() {
        if (beaconProfileDataReader == null) {
            beaconProfileDataReader = new BeaconProfileDataReader(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return beaconProfileDataReader;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getBeaconProfileDataReader().getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getBeacon3100LogBookFactory().getLogBookData(logBooks);
    }

    private Beacon3100LogBookFactory getBeacon3100LogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new Beacon3100LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return logBookFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getBeacon3100Messaging().getSupportedMessages();
    }

    protected Beacon3100Messaging getBeacon3100Messaging() {
        if (beacon3100Messaging == null) {
            beacon3100Messaging = new Beacon3100Messaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), objectMapperService, this.getPropertySpecService(), this.nlsService, this.converter, this.extractor, this.deviceGroupExtractor, deviceExtractor, certificateWrapperExtractor, keyAccessorTypeExtractor, deviceMessageFileExtractor);
        }
        return beacon3100Messaging;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getBeacon3100Messaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getBeacon3100Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return getBeacon3100Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return getBeacon3100Messaging().prepareMessageContext(device, offlineDevice, deviceMessage);
    }

    /**
     * There's 2 dialects:
     * - 1 used to communicate to the mirrored device (= cached meter data) in the Beacon DC
     * - 1 used to communicate straight to the actual meter, using the Beacon as a gateway.
     */
    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(new MirrorTcpDeviceProtocolDialect(this.getPropertySpecService(), nlsService), new GatewayTcpDeviceProtocolDialect(this.getPropertySpecService(), nlsService));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    private Beacon3100RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new Beacon3100RegisterFactory(getDlmsSession(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        DeviceIdentifier dcIdentifier = new DeviceIdentifierById(offlineDevice.getId());
        CollectedTopology deviceTopology = this.getCollectedDataFactory().createCollectedTopology(dcIdentifier);


        /** Step 1 - read IPv6 parameters */
        long macPANId = -1;
        try {
            macPANId = getDlmsSession().getCosemObjectFactory().getPLCOFDMType2MACSetup().readPanId().longValue();
            logInfo("PanID=0x" + String.format("%x", macPANId));
        } catch (NotInObjectListException e) {
            logWarn("Could not read PAN ID: NotInObjectListException");
        } catch (Exception e) {
            logWarn("IOException while reading PAN ID: " + e.getMessage());
        }

        String ipV6Prefix = "";
        try {
            ipV6Prefix = offlineDevice.getAllProperties().getProperty(Beacon3100ConfigurationSupport.IPV6_ADDRESS_AND_PREFIX_LENGTH).toString();
            logInfo("IPv6Prefix: " + ipV6Prefix);
        } catch (Exception ex) {
            logWarn("Could not get IPv6Prefix");
        }

        /** Step 2 - read and parse the node-list (SAP assignment list), with simple slave information */

        List<SAPAssignmentItem> sapAssignmentList;      //List that contains the SAP id's and the MAC addresses of all logical devices (= gateway + slaves)
        final Array nodeList;
        try {
            sapAssignmentList = this.getDlmsSession().getCosemObjectFactory().getSAPAssignment().getSapAssignmentList();
            nodeList = getG3NetworkManagement().getNodeList();
            logInfo("There are " + nodeList.nrOfDataTypes() + " nodes in the SAP assignment list");
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
        final List<G3Node> g3Nodes = getDlmsSessionProperties().hasPre20Firmware() ?
                com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.G3Topology.convertNodeList(nodeList, this.getDlmsSession().getTimeZone()) :
                com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.G3Topology.convertNodeListV2(nodeList, this.getDlmsSession().getTimeZone());


        final Optional<String> dcMacAddress = extractDCMacAddress(g3Nodes);
        if (dcMacAddress.isPresent()) {
            logInfo("Beacon's MAC address: " + dcMacAddress.get());
        } else {
            logInfo("Cannot determine Beacon's MAC address!");
        }


        /** Step 3 - get path to slaves and update neighbor table as we go (TTL < 1 min in beacon now!) */
        G3NeighborList g3NeighborList = new G3NeighborList();

        long start = System.currentTimeMillis();

        if (!getDlmsSessionProperties().hasPre20Firmware() && dcMacAddress.isPresent()) {
            int nodes = g3Nodes.size();
            int current = 0;
            for(G3Node g3Node : g3Nodes) {
                boolean pathOk = false;

                try {
                    if (getDlmsSessionProperties().doPathRequestOnTopology()){
                        String progress = String.format("%.1f", 100.0 * current++ / nodes);
                        logInfo("Requesting path to: " + g3Node.getMacAddressString() + " (" + progress + "%)");

                        pathOk = pathRequest(g3Node, dcMacAddress.get()); //first try
                    }

                    if (!pathOk) {
                        // error while requesting path, do a ROUTE request now then retry
                        if (getDlmsSessionProperties().doRouteRequestsOnTopology()) {
                            if (routeRequest(g3Node)) {
                                pathOk = pathRequest(g3Node, dcMacAddress.get());
                            }
                        }
                    }

                } catch (Exception ex) {
                    // do now allow ComServer to stop communication, we can experience various timeouts here
                    logWarn("Cannot get path to " + g3Node.getMacAddressString() + ": " + ex.getMessage());
                }

                if (!pathOk) { // no luck getting the path (or not necessary), just link it directly to the Beacon
                    addDirectLinkToBeacon(dcMacAddress.get(), g3Node);
                }


                // neighbor table TTL expires in 1 minute, there is a bug in the Beacon, refresh it asap
                // TODO: remove this part after Beacon entry-TTL is fixed and we can read this only once
                if ((System.currentTimeMillis() - start > 50 * 1000)
                        && (getDlmsSessionProperties().doPathRequestOnTopology() || getDlmsSessionProperties().doRouteRequestsOnTopology())){
                    try {
                        neighbourTable = null; // invalidate current table
                        Optional<Array> tempNeighborTable = getNeighbourTable();
                        if (tempNeighborTable.isPresent()) {
                            g3NeighborList.update(getNeighbourTable().get());
                            start = System.currentTimeMillis();

                            logInfo("Refreshed " + getNeighbourTable().get().nrOfDataTypes() + " entries from the neighbor table.");
                        }
                    } catch (Exception ex) {
                        // do now allow ComServer to stop communication, we can experience various timeouts here
                        logWarn("Cannot refresh neighbor table: " + ex.getMessage());
                    }
                }
            }

                /** Step 4 - read neighbor table (after path is constructed, final round) */
                try {
                    if (getNeighbourTable().isPresent()) {
                        g3NeighborList.update(getNeighbourTable().get());
                        logInfo("There are " + getNeighbourTable().get().nrOfDataTypes() + " entries in the neighbor table.");
                    } else {
                        logWarn("Neighbor table not available!");
                    }


                } catch (Exception ex) {
                    // do now allow ComServer to stop communication
                    logWarn("Cannot refresh neighbor table: " + ex.getMessage());
                }


                /** Step 5 - parse information and populate topology */
                for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {

                    byte[] logicalDeviceNameBytes = sapAssignmentItem.getLogicalDeviceNameBytes();
                    if (hasLogicalDevicePrefix(logicalDeviceNameBytes, GATEWAY_LOGICAL_DEVICE_PREFIX)) {
                        byte[] logicalNameMacBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, GATEWAY_LOGICAL_DEVICE_PREFIX.length(), GATEWAY_LOGICAL_DEVICE_PREFIX.length() + MAC_ADDRESS_LENGTH);
                        final String macAddress = ProtocolTools.getHexStringFromBytes(logicalNameMacBytes, "");

                        final G3Node g3Node = findG3Node(macAddress, g3Nodes);
                        if (g3Node != null) {
                            //Always include the slave information if it is present in the SAP assignment list and the G3 node list.
                            //It is the ComServer framework that will then do a smart update in EIServer, taking the readout LastSeenDate into account.

                            BigDecimal gatewayLogicalDeviceId = BigDecimal.valueOf(sapAssignmentItem.getSap());
                            BigDecimal mirrorLogicalDeviceId = BigDecimal.valueOf(findMatchingMirrorLogicalDevice(macAddress, sapAssignmentList));
                            BigDecimal lastSeenDate = BigDecimal.valueOf(g3Node.getLastSeenDate().getTime());
                            BigDecimal persistedGatewayLogicalDeviceId = null;
                            BigDecimal persistedMirrorLogicalDeviceId = null;
                            BigDecimal persistedLastSeenDate = null;
                            try {
                                persistedMirrorLogicalDeviceId = getGeneralProperty(macAddress, AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
                                persistedGatewayLogicalDeviceId = getGeneralProperty(macAddress, AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
                                persistedLastSeenDate = getGeneralProperty(macAddress, G3Properties.PROP_LASTSEENDATE);
                            } catch (Exception ignored) {
                            }

                            DialHomeIdDeviceIdentifier slaveDeviceIdentifier = new DialHomeIdDeviceIdentifier(macAddress);  //Using callHomeId as a general property
                            ObservationDateProperty observationTimestampProperty = new ObservationDateProperty(G3Properties.PROP_LASTSEENDATE, lastSeenDate);
                            deviceTopology.addSlaveDevice(slaveDeviceIdentifier, observationTimestampProperty);

                            try {
                                if ( getDlmsSessionProperties().updateIpv6OnTopology()
                                        && ipV6Prefix!=null && !ipV6Prefix.isEmpty() && macPANId>0) {
                                    String nodeIpv6Address = IPv6Utils.getNodeAddress(ipV6Prefix, (int) macPANId, g3Node.getShortAddress());
                                    if (IPv6Utils.isValid(nodeIpv6Address)) {
                                        logInfo("Node " + g3Node.getMacAddressString() + " has IPv6 " + nodeIpv6Address);

                                        deviceTopology.addAdditionalCollectedDeviceInfo(
                                                this.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                                        slaveDeviceIdentifier,
                                                        HS3300ConfigurationSupport.IP_V6_ADDRESS,
                                                        nodeIpv6Address
                                                )
                                        );
                                    }
                                }
                            } catch (Exception ignored) {
                                logWarn("Cannot update IPv6 address for node " + g3Node.getMacAddressString() + ": " + ignored.getMessage());
                            }

                            if (!gatewayLogicalDeviceId.equals(persistedGatewayLogicalDeviceId)) {
                                deviceTopology.addAdditionalCollectedDeviceInfo(
                                        this.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                                slaveDeviceIdentifier,
                                                AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID,
                                                gatewayLogicalDeviceId
                                        )
                                );
                            }
                            if (!mirrorLogicalDeviceId.equals(persistedMirrorLogicalDeviceId)) {
                                deviceTopology.addAdditionalCollectedDeviceInfo(
                                        this.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                                slaveDeviceIdentifier,
                                                AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID,
                                                mirrorLogicalDeviceId
                                        )
                                );
                            }
                            if (!lastSeenDate.equals(persistedLastSeenDate)) {
                                deviceTopology.addAdditionalCollectedDeviceInfo(
                                        this.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                                                slaveDeviceIdentifier,
                                                G3Properties.PROP_LASTSEENDATE,
                                                lastSeenDate
                                        )
                                );
                            }

                            if (!getDlmsSessionProperties().hasPre20Firmware()) {

                                logInfo("Processing " + g3Node.toString());

                                /* Init general information available from the node list */
                                String nodeAddress = g3Node.getMacAddressString();
                                int shortAddress = g3Node.getShortAddress();
                                Date lastUpdate = g3Node.getLastSeenDate();
                                ;
                                Date lastPathRequest = g3Node.getLastPathRequest();
                                int state = g3Node.getNodeState().getValue();
                                long roundTrip = g3Node.getRoundTrip();
                                int linkCost = g3Node.getLinkCost();

                                int modulationSchema = g3Node.getModulationScheme() == null ? G3NodeNodeModulationScheme.UNKNOWN.getValue() : g3Node.getModulationScheme().getValue();
                                int modulation = g3Node.getTxModulation() == null ? G3NodeTxModulation.UNKNOWN.getValue() : g3Node.getTxModulation().getValue();
                                int lqi = g3Node.getLqi();
                                int phaseDifferential = g3Node.getPhaseInfo() == null ? G3NodePhaseInfo.NOPHASEINFO.getValue() : g3Node.getPhaseInfo().getValue();

                                /* Prepare additional information which is available only from the neighbor-table */
                                long toneMap = 0;
                                int txGain = 0;
                                int txRes = 0;
                                int txCoeff = 0;
                                int tmrValidTime = 0;
                                int neighbourValidTime = 0;

                                if (g3NeighborList.available()) {
                                    Optional<G3Neighbor> neighbourOpt = g3NeighborList.findByShortAddress(g3Node.getShortAddress());

                            /*
                              Neighbour information for g3Node might not be available in a particular case:

                              Meter1 <-> Concentrator <-> Meter2   Meter3

                              The concentrator is linked to Meter1 and Meter2 but Meter3 is hanging outside.
                              This is also shown on the G3 interface > Topology tab graph on the Beacon web portal.
                              It might be that Meter3 is attenuated, as in my case and it will get mapped by the Beacon
                              eventually to the corresponding meter or because other interference (weak PLC signal).
                             */
                                    if (neighbourOpt.isPresent()) {
                                        G3Neighbor g3Neighbor = neighbourOpt.get();

                                        modulationSchema = g3Neighbor.getModulationSchema().getValue();
                                        toneMap = g3Neighbor.getToneMap();
                                        modulation = g3Neighbor.getModulation().getValue();
                                        txGain = g3Neighbor.getTxGain();
                                        txRes = g3Neighbor.getTxRes();
                                        txCoeff = g3Neighbor.getTxCoeff();
                                        lqi = g3Neighbor.getLqi();
                                        phaseDifferential = g3Neighbor.getPhaseDifferential().getValue();
                                        tmrValidTime = g3Neighbor.getTmrValidTime();
                                        neighbourValidTime = g3Neighbor.getNeighbourValidTime();
                                    } else {
                                        logInfo("Node with shortAddress " + g3Node.getShortAddress() + " and MAC " + g3Node.getMacAddressString() + " is not a Beacon neighbor!");
                                    }

                                    // sanitise for null dates so we don't crash the comserver storage
                                    if (lastUpdate == null) {
                                        lastUpdate = new Date(0);
                                    }
                                    if (lastPathRequest == null) {
                                        lastPathRequest = new Date(0);
                                    }

                                }
                                //TODO fix parsing in core for unknown modulation(255) in com.energyict.mdc.device.topology.ModulationScheme.fromId
                                if (modulationSchema == 255) {
                                    modulationSchema = 0; // 255 is not known in ComServer :(
                                }

                                /* in this point we have the complete data about the node */
                                deviceTopology.addTopologyNeighbour(slaveDeviceIdentifier, modulationSchema, toneMap, modulation, txGain, txRes,
                                        txCoeff, lqi, phaseDifferential, tmrValidTime, neighbourValidTime, macPANId,
                                        nodeAddress, shortAddress, lastUpdate, lastPathRequest, state, roundTrip, linkCost
                                );

                                StringBuilder sb = new StringBuilder();
                                sb.append("Topology entry added:");
                                sb.append(" shortAddress: ").append(Math.toIntExact(shortAddress));
                                sb.append(", nodeAddress: ").append(nodeAddress);
                                sb.append(", modulationSchema: ").append(modulationSchema);
                                sb.append(", toneMap: ").append(toneMap);
                                sb.append(", modulation: ").append(modulation);
                                sb.append(", txGain: ").append(txGain);
                                sb.append(", txRes: ").append(txRes);
                                sb.append(", txCoeff: ").append(txCoeff);
                                sb.append(", lqi: ").append(lqi);
                                sb.append(", phaseDifferential: ").append(phaseDifferential);
                                sb.append(", tmrValidTime: ").append(tmrValidTime);
                                sb.append(", macPANId: ").append(macPANId);
                                sb.append(", state: ").append(state);
                                sb.append(", roundTrip: ").append(roundTrip);
                                sb.append(", linkCost: ").append(linkCost);
                                sb.append(", lastUpdate: ").append(lastUpdate);
                                sb.append(", lastPathRequest: ").append(lastPathRequest);
                                sb.append(", neighbourValidTime: ").append(neighbourValidTime);

                                logInfo(sb.toString());


                            } else {
                                logWarn("No neighbor table for " + offlineDevice.getSerialNumber());
                            }

                        }

                    } else {
                        logWarn("Cannot find G3 node for " + macAddress);
                    }
                }
            }





                if (dcMacAddress.isPresent()) {
                    for (final String key : topologySegments) {
                        // 0 = source, 1 = target, 2 = hop
                        List<String> tokens = Arrays.asList(key.split(";"));

                        if (tokens.get(0).equals(dcMacAddress.get())) {
                            deviceTopology.addPathSegmentFor(
                                    dcIdentifier,
                                    new DialHomeIdDeviceIdentifier(tokens.get(1)),
                                    new DialHomeIdDeviceIdentifier(tokens.get(2)),
                                    Duration.ofDays(5), 100
                            );
                        } else {
                            deviceTopology.addPathSegmentFor(
                                    new DialHomeIdDeviceIdentifier(tokens.get(0)),
                                    new DialHomeIdDeviceIdentifier(tokens.get(1)),
                                    new DialHomeIdDeviceIdentifier(tokens.get(2)),
                                    Duration.ofDays(5), 100
                            );
                        }
                    }
                }

                for (TopologyPathSegment tps : deviceTopology.getTopologyPathSegments()) {
                    logInfo("Topology path segment " + tps.toString());
                }

                logWarn("Added " +
                        deviceTopology.getTopologyNeighbours().size() + " topology nodes and " +
                        deviceTopology.getTopologyPathSegments().size() + " path segments");


                return deviceTopology;
            }


            private void addDirectLinkToBeacon (String dcMacAddress, G3Node g3Node){
                if (dcMacAddress == null || dcMacAddress.isEmpty()) {
                    return;
                }
                addPathSegment(
                        dcMacAddress.concat(";").concat(g3Node.getMacAddressString()).concat(";").concat(g3Node.getMacAddressString())
                );
            }

            private void writeMacNeighbourTableEntryTTL ( int ttl) throws IOException {
                final PLCOFDMType2MACSetup plcMACSetup = this.getDlmsSession().getCosemObjectFactory().getPLCOFDMType2MACSetup();

                plcMACSetup.writeNeighbourTableEntryTTL(ttl);
            }

            /**
             * Asks for a route request towards a node. WARNING: this is a PLC-intensive tasks, use with caution!
             */
            private boolean routeRequest (G3Node g3Node){
                boolean result;

                try {
                    result = getG3NetworkManagement().requestRoute(g3Node.getMacAddressString());

                    logInfo("Route request to " + g3Node.getMacAddressString() + " result: " + result);
                } catch (Exception e) {
                    result = false;
                    logWarn("Error while requesting route to " + g3Node.getMacAddressString() + ": " + e.getMessage());
                }

                return result;
            }

            /**
             * Will trigger a path-request for each node, even if it's reported as directly connected to the Beacon!
             *
             */
            private boolean pathRequest (G3Node g3Node, String dcMacAddress){
                try {
                    // date:meterMac:mac1;mac2;mac3:mac1;mac2;mac3
                    final String requestPath = getG3NetworkManagement().requestPath(g3Node.getMacAddressString());

                    logInfo("Path to node " + g3Node.getMacAddressString() + " = " + requestPath);

                    List<String> pathTokens = Arrays.asList(requestPath.split(":"));
                    List<String> forwardPaths = Arrays.asList(pathTokens.get(2).split(";"));
                    List<String> reversePaths = Arrays.asList(pathTokens.get(3).split(";"));

                    List<String> completePath = new ArrayList<>(Lists.reverse(reversePaths));
                    completePath.addAll(forwardPaths.subList(1, forwardPaths.size()));

                    if (completePath.size() == 1) {
                        // direct connection to Beacon
                        addDirectLinkToBeacon(dcMacAddress, g3Node);
                    }

                    for (int i = 0; i < completePath.size(); i++) {
                        final String source = completePath.get(i);

                        for (int j = i + 1; j < completePath.size(); j++) {
                            final String target = completePath.get(j);
                            String segment;
                            if (j - i == 1) {
                                // no hop
                                segment = source.concat(";").concat(target).concat(";").concat(target);

                            } else {
                                final String hop = completePath.get(i + 1);
                                segment = source.concat(";").concat(target).concat(";").concat(hop);
                            }

                            addPathSegment(segment);
                        }
                    }

                } catch (IOException e) {
                    getLogger().severe(logPrefix + "Cannot get G3 path to " + g3Node.getMacAddressString() + " : " + e.getMessage());
                    return false;
                }

                return true;
            }

            private void addPathSegment (String segment){
                logInfo("Adding path segment: " + segment);
                topologySegments.add(segment);
            }


            private Optional<Array> getNeighbourTable () {
                try {
                    if (neighbourTable == null) {
                        final PLCOFDMType2MACSetup plcMACSetup = this.getDlmsSession().getCosemObjectFactory().getPLCOFDMType2MACSetup();
                        neighbourTable = (Array) plcMACSetup.readNeighbourTable();
                        return Optional.of(neighbourTable);
                    } else {
                        return Optional.of(neighbourTable);
                    }
                } catch (IOException e) {
                    logWarn("Could not read neighbour table: " + e.getMessage());
                }
                return Optional.empty();
            }

            private Optional<String> extractDCMacAddress (List < G3Node > g3Nodes) {
                Optional<String> result = Optional.empty();
                for (final G3Node g3Node : g3Nodes) {
                    if (g3Nodes.stream().noneMatch(n -> n.getMacAddressString().equals(g3Node.getParentMacAddressString()))) {
                        return Optional.of(g3Node.getParentMacAddressString());
                    }
                }
                return result;
            }

            private G3NetworkManagement getG3NetworkManagement () throws NotInObjectListException {
                if (getDlmsSessionProperties().getReadOldObisCodes()) {
                    return this.getDlmsSession().getCosemObjectFactory().getG3NetworkManagement();
                } else {
                    return this.getDlmsSession().getCosemObjectFactory().getG3NetworkManagement(Beacon3100Messaging.G3_NETWORK_MANAGEMENT_NEW_OBISCODE);
                }
            }

            /**
             * Return the general property with the given name, for the device with the given macAddress.
             * Return null if the device does not exist, or if the property does not exist.
             */
            private BigDecimal getGeneralProperty (String macAddress, String propertyName){
                for (OfflineDevice offlineSlaveDevice : offlineDevice.getAllSlaveDevices()) {
                    String callHomeId = offlineSlaveDevice.getAllProperties().getTypedProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
                    if (callHomeId != null && callHomeId.equals(macAddress)) {
                        return offlineSlaveDevice.getAllProperties().getTypedProperty(propertyName);
                    }
                }
                return null;
            }


    private G3Node findG3Node(final String macAddress, final List<G3Node> g3Nodes) {
        if (macAddress != null && g3Nodes != null && !g3Nodes.isEmpty()) {
            for (final G3Node g3Node : g3Nodes) {
                if (g3Node != null) {
                    final String nodeMac = ProtocolTools.getHexStringFromBytes(g3Node.getMacAddress(), "");
                    if (nodeMac.equalsIgnoreCase(macAddress)) {
                        return g3Node;
                    }
                }
            }
        }
        return null;
    }

            private boolean hasLogicalDevicePrefix ( byte[] logicalDeviceNameBytes, String expectedPrefix){
                byte[] actualPrefixBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, 0, expectedPrefix.length());
                String actualPrefix = new String(actualPrefixBytes, Charset.forName(UTF_8));
                return actualPrefix.equals(expectedPrefix);
            }

            private long findMatchingMirrorLogicalDevice (String
            gatewayMacAddress, List < SAPAssignmentItem > sapAssignmentList){
                for (SAPAssignmentItem sapAssignmentItem : sapAssignmentList) {
                    byte[] logicalDeviceNameBytes = sapAssignmentItem.getLogicalDeviceNameBytes();
                    if (hasLogicalDevicePrefix(logicalDeviceNameBytes, MIRROR_LOGICAL_DEVICE_PREFIX)) {
                        byte[] logicalNameMacBytes = ProtocolTools.getSubArray(logicalDeviceNameBytes, MIRROR_LOGICAL_DEVICE_PREFIX.length(), MIRROR_LOGICAL_DEVICE_PREFIX.length() + MAC_ADDRESS_LENGTH);
                        final String mirrorMacAddress = ProtocolTools.getHexStringFromBytes(logicalNameMacBytes, "");

                        if (gatewayMacAddress.equals(mirrorMacAddress)) {
                            return sapAssignmentItem.getSap();
                        }
                    }
                }
                return -1;
            }

            @Override
            public String getVersion () {
                return "$Date: 2021-06-16$";
            }

            @Override
            public void logOn () {
                getDlmsSession().connect();
                checkCacheObjects();
            }

            @Override
            public void logOff() {
                if (getDlmsSession() != null) {
                    try {
                        getDlmsSession().disconnect();
                    } catch (Exception ignored){
                        // ignoring exception to be able to save collected data
                        getLogger().info(logPrefix+"Exception while disconnecting: "+ignored.getMessage());
                    }
                }
            }

            protected void checkCacheObjects () {
                if (getDeviceCache() == null) {
                    setDeviceCache(new DLMSCache());
                }
                DLMSCache dlmsCache = getDeviceCache();
                if (dlmsCache.getObjectList() == null || getDlmsSessionProperties().isReadCache()) {
                    readObjectList();
                    dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
                } else {
                    getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
                }
            }

            public Beacon3100Properties getDlmsSessionProperties () {
                if (dlmsProperties == null) {
                    dlmsProperties = new Beacon3100Properties(certificateWrapperExtractor);
                }
                return (Beacon3100Properties) dlmsProperties;
            }

            /**
             * A collection of general DLMS properties.
             * These properties are not related to the security or the protocol dialects.
             */
            protected HasDynamicProperties getDlmsConfigurationSupport () {
                if (dlmsConfigurationSupport == null) {
                    dlmsConfigurationSupport = new Beacon3100ConfigurationSupport(this.getPropertySpecService());
                }
                return dlmsConfigurationSupport;
            }

            /**
             * The used security support has changed from DlmsSecuritySupport to DlmsSecuritySuite1And2Support, so we indicate here
             * that indeed the DlmsSecuritySupport was the previous type of security support.
             * This is used by the ProtocolSecurityRelationTypeUpgrader to migrate old, existing security properties that were created for
             * the previous security relation to the new security relation.
             */
            @Override
            public DeviceProtocolSecurityCapabilities getPreviousSecuritySupport () {
                return new DsmrSecuritySupport(this.getPropertySpecService());
            }

            @Override
            public ManufacturerInformation getManufacturerInformation () {
                return null;
            }

            @Override
            public DeviceFunction getDeviceFunction () {
                return DeviceFunction.NONE;
            }

            @Override
            public CollectedFirmwareVersion getFirmwareVersions () {
                CollectedFirmwareVersion result = this.getCollectedDataFactory().createFirmwareVersionsCollectedData(new DeviceIdentifierById(this.offlineDevice.getId()));

                ObisCode activeMetrologyFirmwareVersionObisCode = ObisCode.fromString("0.0.0.2.1.255");
                try {
                    AbstractDataType valueAttr = getDlmsSession().getCosemObjectFactory().getData(activeMetrologyFirmwareVersionObisCode).getValueAttr();
                    String fwVersion = valueAttr.isOctetString() ? valueAttr.getOctetString().stringValue() : valueAttr.toBigDecimal().toString();
                    result.setActiveMeterFirmwareVersion(fwVersion);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                        Issue problem = this.getIssueFactory().createProblem(activeMetrologyFirmwareVersionObisCode, "issue.protocol.readingOfFirmwareFailed", e.toString());
                        result.setFailureInformation(ResultType.InCompatible, problem);
                    }   //Else a communication exception is thrown
                }

                return result;
            }

            /**
             * Enumerates the different clients for the Beacon.
             *
             * @author alex
             */
            public enum ClientConfiguration {

                /**
                 * Management client.
                 */
                MANAGEMENT(1, ObisCode.fromString("0.0.43.1.1.255"), ObisCode.fromString("0.0.43.0.2.255"), ObisCode.fromString("0.0.40.0.2.255")),

                /**
                 * Public client.
                 */
                PUBLIC(16, null, ObisCode.fromString("0.0.40.0.1.255"), ObisCode.fromString("0.0.40.0.1.255")),

                /**
                 * Read-write client.
                 */
                READ_WRITE(32, ObisCode.fromString("0.0.43.1.2.255"), ObisCode.fromString("0.0.43.0.3.255"), ObisCode.fromString("0.0.40.0.3.255")),

                /**
                 * Firmware upgrade client.
                 */
                FIRMWARE(64, ObisCode.fromString("0.0.43.1.3.255"), ObisCode.fromString("0.0.43.0.4.255"), ObisCode.fromString("0.0.40.0.4.255")),

                /**
                 * Read only client.
                 */
                READ_ONLY(127, ObisCode.fromString("0.0.43.1.4.255"), ObisCode.fromString("0.0.43.0.5.255"), ObisCode.fromString("0.0.40.0.5.255"));

                /**
                 * Client ID to be used.
                 */
                protected final int clientId;

                /**
                 * OBIS code of the frame counter.
                 */
                private final ObisCode frameCounterOBIS;

                /**
                 * OBIS code of the Security Setup
                 */
                private final ObisCode securitySetupOBIS;

                /**
                 * OBIS code of the Association LN setup
                 */
                private final ObisCode associationLnOBIS;

                /**
                 * Create a new instance.
                 *
                 * @param id               Client ID.
                 * @param frameCounterOBIS Frame counter OBIS code.
                 */
                ClientConfiguration(final int id, final ObisCode frameCounterOBIS, final ObisCode securitySetupOBIS, final ObisCode associationLNOBIS) {
                    this.clientId = id;
                    this.frameCounterOBIS = frameCounterOBIS;
                    this.securitySetupOBIS = securitySetupOBIS;
                    this.associationLnOBIS = associationLNOBIS;
                }

                /**
                 * Returns the client with the given ID.
                 *
                 * @param clientId ID of the requested client.
                 * @return The matching client, <code>null</code> if not known.
                 */
                public static ClientConfiguration getByID(final int clientId) {
                    for (final ClientConfiguration client : ClientConfiguration.values()) {
                        if (client.clientId == clientId) {
                            return client;
                        }
                    }

                    return null;
                }

                /**
                 * Returns the OBIS of the FC.
                 *
                 * @return The OBIS of the FC.
                 */
                public final ObisCode getFrameCounterOBIS() {
                    return frameCounterOBIS;
                }

                /**
                 * Returns the OBIS of the SecuritySetup.
                 *
                 * @return The OBIS of the SecuritySetup.
                 */
                public final ObisCode getSecuritySetupOBIS() {
                    return securitySetupOBIS;
                }

                /**
                 * Return the Association LN ObisCode
                 */
                public ObisCode getAssociationLN() {
                    return associationLnOBIS;
                }
            }

            @Override
            public boolean firmwareSignatureCheckSupported () {
                return BeaconFirmwareSignatureCheck.firmwareSignatureCheckSupported();
            }

            @Override
            public boolean verifyFirmwareSignature (File firmwareFile, PublicKey pubKey) throws
            NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
                return BeaconFirmwareSignatureCheck.verifyFirmwareSignature(firmwareFile, pubKey);
            }

            private void logInfo (String infoMessage){
                getLogger().info(logPrefix + infoMessage);
            }


            private void logWarn (String infoMessage){
                getLogger().warning(logPrefix + infoMessage);
            }
        }