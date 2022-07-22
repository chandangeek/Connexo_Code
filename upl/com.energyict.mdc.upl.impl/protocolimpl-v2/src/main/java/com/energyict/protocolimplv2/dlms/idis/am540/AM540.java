package com.energyict.protocolimplv2.dlms.idis.am540;

import com.energyict.mdc.channel.serial.Parities;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.tasks.DeviceConnectionFunction;
import com.energyict.mdc.tasks.MirrorTcpDeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.UPLConnectionFunction;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.connection.FlagIEC1107Connection;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataEncryptionException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.events.AM540LogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.AM540RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.FIFOStoredValues;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * The AM540 is a PLC E-meter designed according to IDIS package 2 specifications <br/>
 * The protocol is an extension of the AM130 protocol (which is the GPRS variant designed according to IDIS P2)
 * <p/>
 * The protocol supports also EVN Netz-NO Companion standard specification (security-related).
 *
 * @author sva
 * @since 11/08/2015 - 14:04
 */
public class AM540 extends AM130 implements SerialNumberSupport {
    protected static final ObisCode EVN_FRAMECOUNTER_DATA_READOUT = ObisCode.fromString("0.0.43.3.0.255");
    protected static final ObisCode EVN_FRAMECOUNTER_INSTALLATION = ObisCode.fromString("0.0.43.4.0.255");
    protected static final ObisCode EVN_FRAMECOUNTER_MAINTENANCE = ObisCode.fromString("0.0.43.5.0.255");
    protected static final ObisCode EVN_FRAMECOUNTER_CERTIFICATION = ObisCode.fromString("0.0.43.6.0.255");

    /**
     * OBIS code for the billing profile.
     */
    private static final ObisCode OBIS_BILLING_PROFILE = ObisCode.fromString("0.0.98.1.0.255");

    protected static final int EVN_CLIENT_MANAGEMENT = 1;
    protected static final int EVN_CLIENT_DATA_READOUT = 2;
    protected static final int EVN_CLIENT_FW_UPGRADE = 3;
    protected static final int EVN_CLIENT_INSTALLATION = 5;
    protected static final int EVN_CLIENT_MAINTENANCE = 6;
    protected static final int EVN_CLIENT_CERTIFICATION = 7;
    protected static final int PUBLIC_CLIENT = 16;
    protected static final int EVN_CLIENT_CUSTOMER_INFORMATION_PUSH = 103;


    private AM540Cache am540Cache;
    private HHUSignOnV2 hhuSignOn;

    /**
     * For billing registers.
     */
    private StoredValues storedValues;

    public AM540(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        getDeviceCache().setConnectionToBeaconMirror(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());
        getLogger().info("Start protocol for " + offlineDevice.getSerialNumber());
        getLogger().info("-version: " + getVersion());
        setMeterToTransparentMode(comChannel);
        handleFC(comChannel);
        initDlmsSession(comChannel);
        getLogger().info("Protocol initialization phase ended, executing tasks ...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoredValues getStoredValues() {
        if (this.storedValues == null) {
            try {
                this.storedValues = new FIFOStoredValues(OBIS_BILLING_PROFILE,
                        this.getDlmsSession().getCosemObjectFactory(),
                        this.getDlmsSessionProperties(),
                        this.getIDISProfileDataReader(),
                        this.getTimeZone(),
                        this.getLogger());
            } catch (IOException e) {
                if (this.getLogger().isLoggable(Level.WARNING)) {
                    this.getLogger().log(Level.WARNING, "IO error when creating StoredValues : [" + e.getMessage() + "]", e);
                }

                throw DLMSIOExceptionHandler.handle(e, getDlmsSessionProperties().getRetries() + 1);
            }
        }

        return this.storedValues;
    }

    protected void initDlmsSession(ComChannel comChannel) {
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger()));
    }

    private void setMeterToTransparentMode(ComChannel comChannel) {
        if (getDlmsSessionProperties().useMeterInTransparentMode()) {
            if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
                HHUSignOnV2 hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
                SerialPortConfiguration serialPortConfiguration = ((SerialPortComChannel) comChannel).getSerialPortConfiguration();
                int transparentConnectTime = getDlmsSessionProperties().getTransparentConnectTime();
                int transparentDatabits = serialPortConfiguration.getNrOfDataBits().getNrOfDataBits().intValue();
                int transparentStopbits = serialPortConfiguration.getNrOfStopBits().getNrOfStopBits().intValue();
                int transparentParity = getParityValue(serialPortConfiguration.getParity());
                int transparentBaudrate = serialPortConfiguration.getBaudrate().getBaudrate().intValue();
                int authenticationSecurityLevel = Integer.parseInt(getDlmsSessionProperties().getTransparentSecurityLevel().split(":")[0]);
                FlagIEC1107Connection flagIEC1107Connection = null;
                try {
                    String strPassword = getDlmsSessionProperties().getTransparentPassword();
                    flagIEC1107Connection = new FlagIEC1107Connection((SerialPortComChannel) comChannel, transparentConnectTime, transparentBaudrate, transparentDatabits, transparentStopbits, transparentParity, authenticationSecurityLevel, strPassword, getLogger(), hhuSignOn);
                    flagIEC1107Connection.setMeterToTransparentMode();
                } catch (Exception e) {
                    getLogger().info("Failed to set the meter into transparent mode");
                    if (flagIEC1107Connection != null) {
                        flagIEC1107Connection.switchBaudrate(transparentBaudrate, HHUSignOn.PROTOCOL_HDLC);
                    }
                }
                flagIEC1107Connection = null;//cleanUp this connection
            }
        }
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    private int getParityValue(Parities parities) {
        switch (parities) {
            case NONE:
                return 0;
            case ODD:
                return 2;
            case EVEN:
                return 1;
            default:
                return -1;
        }
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM540 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2017-04-13 16:22:14 +0300 (Thu, 13 Apr 2017)$";
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
            try {
                final boolean useUnspecifiedClockStatus = this.getDlmsSessionProperties().useUnspecifiedAsClockStatus();
                final AXDRDateTime dateTime = new AXDRDateTime(timeToSet, getTimeZone(), useUnspecifiedClockStatus);
                dateTime.useUnspecifiedAsDeviation(getDlmsSessionProperties().useUndefinedAsTimeDeviation());
                dateTime.setSetHSByte(this.getDlmsSessionProperties().supportsHundredthsTimeField());

                getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(dateTime);
            } catch (IOException e) {
                getLogger().log(Level.FINEST, e.getMessage());
                throw DLMSIOExceptionHandler.handle(e, getDlmsSessionProperties().getRetries() + 1);
            }
        }
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(
                (DeviceProtocolDialect) new SerialDeviceProtocolDialect(this.getPropertySpecService(), getNlsService()), // HDLC.
                new TcpDeviceProtocolDialect(this.getPropertySpecService(), getNlsService()), // Gateway.
                new MirrorTcpDeviceProtocolDialect(this.getPropertySpecService(), getNlsService())); // Mirror.
    }

    @Override
    public List<UPLConnectionFunction> getConsumableConnectionFunctions() {
        return Arrays.asList(DeviceConnectionFunction.MIRROR, DeviceConnectionFunction.GATEWAY, DeviceConnectionFunction.INBOUND);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList((ConnectionType) new SioOpticalConnectionType(this.getPropertySpecService()), new RxTxOpticalConnectionType(this.getPropertySpecService()), new OutboundTcpIpConnectionType(this.getPropertySpecService()));
    }

    @Override
    protected HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new AM540ConfigurationSupport(this.getPropertySpecService());
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
            validateFCProperties(clientId);

            boolean weHaveValidCachedFrameCounter = false;
            if (getDlmsSessionProperties().useCachedFrameCounter()) {
                weHaveValidCachedFrameCounter = getCachedFrameCounter(comChannel, clientId);
            }

            if (!weHaveValidCachedFrameCounter) {
                if (getDlmsSessionProperties().getRequestAuthenticatedFrameCounter() & (clientId != EVN_CLIENT_MANAGEMENT)) {
                    readFrameCounterSecure(comChannel);
                } else {
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
                }
            }
        }
    }

    private void validateFCProperties(int clientId) {
        if (clientId == EVN_CLIENT_MANAGEMENT
                && getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()
                && !getDlmsSessionProperties().useCachedFrameCounter()) {

            String msg = "When Client 1 is configured and "
                    + AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER
                    + " is active, we also need "
                    + AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER
                    + " to be active";
            getLogger().info(msg);

            throw DeviceConfigurationException.unsupportedPropertyValueWithReason(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER, "false", msg);
        }
    }

    /**
     * Get the frame counter from the cache, for the given clientId.
     * If no frame counter is available in the cache (value -1), use the configured InitialFC property.
     * <p/>
     * Additionally, the FC value can be validated with ValidateCachedFrameCounterAndFallback
     */
    protected boolean getCachedFrameCounter(ComChannel comChannel, int clientId) {
        getLogger().info("Will try to use a cached frame counter for client=" + clientId);
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

    protected boolean testConnectionAndRetryWithFrameCounterIncrements(ComChannel comChannel) {
        DlmsSession testDlmsSession = getDlmsSessionForFCTesting(comChannel);
        int retries = getDlmsSessionProperties().getFrameCounterRecoveryRetries();
        int step = getDlmsSessionProperties().getFrameCounterRecoveryStep();
        boolean releaseOnce = true;

        getLogger().info("Will test the frameCounter (" + testDlmsSession.getAso().getSecurityContext().getFrameCounter() + ". Recovery mechanism: retries=" + retries + ", step=" + step);
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
                    getLogger().info("This FrameCounter was validated: " + testDlmsSession.getAso().getSecurityContext().getFrameCounter());
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
        getLogger().warning("Could not validate the frame counter, seems that it's out-of-sync with the device. You'll have to read a fresh one.");
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
        return new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
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
        final AM540Properties publicClientProperties = new AM540Properties(this.getPropertySpecService(), this.getNlsService());
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT), 0, 0, 0, 0, 0, publicProperties));    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsSessionProperties().getSerialNumber());
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

    @Override
    protected ObisCode getFrameCounterForClient(int clientId) {
        // handle some special frame-counters for EVN
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            if (clientId != IDIS2_CLIENT_PUBLIC) { // for public client fall back to standard IDIS
                return new ObisCode(0, 0, 43, 1, clientId, 255);
            }
        } else {
            //secure frame counters
            switch (clientId) {
                case EVN_CLIENT_DATA_READOUT:
                    return EVN_FRAMECOUNTER_DATA_READOUT;
                case EVN_CLIENT_INSTALLATION:
                    return EVN_FRAMECOUNTER_INSTALLATION;
                case EVN_CLIENT_MAINTENANCE:
                    return EVN_FRAMECOUNTER_MAINTENANCE;
                case EVN_CLIENT_CERTIFICATION:
                    return EVN_FRAMECOUNTER_CERTIFICATION;
                default:
            }
        }
        return super.getFrameCounterForClient(clientId); // get the standard IDIS ones
    }

    /**
     * There's 2 different ways to connect to the public client.
     * - on a mirror device, the public client has a pre-established association
     * - on an actual AM540 module, the public client requires a new association
     */
    protected void connectToPublicClient(DlmsSession publicDlmsSession) {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            publicDlmsSession.assumeConnected(getDlmsSessionProperties().getMaxRecPDUSize(), getDlmsSessionProperties().getConformanceBlock());
        } else {
            super.connectToPublicClient(publicDlmsSession);
        }
    }

    /**
     * There's 2 different ways to disconnect from the public client.
     * - on a mirror device, the public client is pre-established, so no need to release the association
     * - on an actual AM540 module, the public client requires a new association
     */
    protected void disconnectFromPublicClient(DlmsSession publicDlmsSession) {
        if (!getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            super.disconnectFromPublicClient(publicDlmsSession);
        }
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     *
     * @param dlmsSession
     */
    protected void connectWithRetries(DlmsSession dlmsSession) {
        int tries = 0;
        while (true) {
            ProtocolRuntimeException exception;
            try {
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
                    dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
            }
        }
    }

    @Override
    public AM540Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = getNewInstanceOfProperties();
        }
        return (AM540Properties) dlmsProperties;
    }

    @Override
    protected AM540Properties getNewInstanceOfProperties() {
        return new AM540Properties(this.getPropertySpecService(), getNlsService());
    }

    @Override
    public AM540Cache getDeviceCache() {
        if (am540Cache == null) {
            am540Cache = new AM540Cache(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());
        }
        return am540Cache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof AM540Cache)) {
            am540Cache = (AM540Cache) deviceProtocolCache;
        }
    }

    @Override
    public void terminate() {
        //As a last step, update the cache with the last FC
        if (getDlmsSession() != null && getDlmsSession().getAso() != null && getDlmsSession().getAso().getSecurityContext() != null) {
            long frameCounter = getDlmsSession().getAso().getSecurityContext().getFrameCounter();
            int clientMacAddress = getDlmsSessionProperties().getClientMacAddress();
            getDeviceCache().setTXFrameCounter(clientMacAddress, frameCounter);
            getLogger().info("Caching frameCounter=" + frameCounter + " for client=" + clientMacAddress);
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            IOException cause = new IOException("When connected to the mirror logical device, execution of device commands is not allowed.");
            throw DeviceConfigurationException.notAllowedToExecuteCommand("send of device messages", cause);
        } else {
            return getIDISMessaging().executePendingMessages(pendingMessages);
        }
    }

    @Override
    protected AM130RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AM540RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public AbstractMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new IDISMeterTopology(this, this.getCollectedDataFactory());
            meterTopology.searchForSlaveDevices();
        }
        return meterTopology;
    }

    @Override
    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AM540Messaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return idisMessaging;
    }

    @Override
    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new AM540LogBookFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return idisLogBookFactory;
    }

    /**
     * Set the initial frame counter to be used when starting this DLMS session.
     */
    protected void setTXFrameCounter(long frameCounter) {
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(getOfflineDevice().getId());
        CollectedTopology deviceTopology = super.getDeviceTopology();
        Date now = new Date();

        // save the last see date when the meter is read-out successfully
        deviceTopology.addAdditionalCollectedDeviceInfo(
                this.getCollectedDataFactory()
                        .createCollectedDeviceProtocolProperty(
                                deviceIdentifier,
                                G3Properties.PROP_LASTSEENDATE,
                                BigDecimal.valueOf(now.getTime())
                        )
        );

        return super.getDeviceTopology();
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    @Override
    protected final void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        final int clientId = this.getDlmsSessionProperties().getClientMacAddress();

        if ((getDeviceCache().getObjectList(clientId) == null) || (readCache)) {
            getLogger().info(readCache ? "ReReadCache property is true, reading cache!" : "The cache was empty, reading out the object list!");

            readObjectList();

            final UniversalObject[] objectList = this.getDlmsSession().getMeterConfig().getInstantiatedObjectList();
            this.getDeviceCache().setObjectList(clientId, objectList);
        } else {
            getLogger().info("Cache exist, will not be read!");
        }

        this.getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList(clientId));
    }

    /**
     * Boolean used to decide if we should specify or not the TimeDeviation and ClockStatus when we readout a class 7 COSEM object (LoadProfile and Logbooks)
     *
     * @return
     */
    public boolean useDsmr4SelectiveAccessFormat() {
        return !getDlmsSessionProperties().useUnspecifiedAsClockStatus() && !getDlmsSessionProperties().useUndefinedAsTimeDeviation();
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion result = this.getCollectedDataFactory().createFirmwareVersionsCollectedData(new DeviceIdentifierById(this.offlineDevice.getId()));

        ObisCode activeMetrologyFirmwareVersionObisCode = ObisCode.fromString("1.0.0.2.0.255");
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

        ObisCode activeCommunicationFirmwareVersion = ObisCode.fromString("1.2.0.2.0.255");
        try {
            AbstractDataType valueAttr = getDlmsSession().getCosemObjectFactory().getData(activeCommunicationFirmwareVersion).getValueAttr();
            String fwVersion = valueAttr.isOctetString() ? valueAttr.getOctetString().stringValue() : valueAttr.toBigDecimal().toString();
            result.setActiveCommunicationFirmwareVersion(fwVersion);
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue problem = this.getIssueFactory().createProblem(activeCommunicationFirmwareVersion, "issue.protocol.readingOfFirmwareFailed", e.toString());
                result.setFailureInformation(ResultType.InCompatible, problem);
            }   //Else a communication exception is thrown
        }

        return result;
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }

    // TODO: 19.03.2018 CXO-8215 for demo purposes
    @Override
    public boolean firmwareSignatureCheckSupported() {
        return true;
    }

    // TODO: 19.03.2018 CXO-8215 for demo purposes
    @Override
    public boolean verifyFirmwareSignature(File firmwareFile, PublicKey pubKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        return false;
    }
}
