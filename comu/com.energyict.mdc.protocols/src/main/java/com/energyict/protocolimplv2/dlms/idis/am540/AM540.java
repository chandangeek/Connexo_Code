package com.energyict.protocolimplv2.dlms.idis.am540;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOnV2;
import com.energyict.mdc.protocol.api.exceptions.DataEncryptionException;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceBasicSupport;
import com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.mdc.protocoltasks.SerialDeviceProtocolDialect;

import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.AM540RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The AM540 is a PLC E-meter designed according to IDIS package 2 specifications <br/>
 * The protocol is an extension of the AM130 protocol (which is the GPRS variant designed according to IDIS P2)
 * <p>
 * The protocol supports also EVN Netz-NO Companion standard specification (security-related).
 *
 * @author sva
 * @since 11/08/2015 - 14:04
 */
public class AM540 extends AM130 implements DeviceBasicSupport {
    protected static final ObisCode EVN_FRAMECOUNTER_DATA_READOUT = ObisCode.fromString("0.0.43.3.0.255");
    protected static final ObisCode EVN_FRAMECOUNTER_INSTALLATION = ObisCode.fromString("0.0.43.4.0.255");
    protected static final ObisCode EVN_FRAMECOUNTER_MAINTENANCE = ObisCode.fromString("0.0.43.5.0.255");
    protected static final ObisCode EVN_FRAMECOUNTER_CERTIFICATION = ObisCode.fromString("0.0.43.6.0.255");

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

    @Inject
    public AM540(Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(clock, thesaurus, propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory, dsmrSecuritySupportProvider);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsProperties().setSerialNumber(offlineDevice.getSerialNumber());
        getDeviceCache().setConnectionToBeaconMirror(getDlmsProperties().useBeaconMirrorDeviceDialect());
        getLogger().info("Start protocol for " + offlineDevice.getSerialNumber());
        getLogger().info("-version: " + getVersion());
        initDlmsSession(comChannel);
        getLogger().info("Protocol initialization phase ended, executing tasks ...");
    }

    private void initDlmsSession(ComChannel comChannel) {
        setMeterToTransparentMode(comChannel);
        handleFC(comChannel);
        setDlmsSession(new DlmsSession(comChannel, getDlmsProperties()));
//        initFrameCounterCache(null);
    }

    private void setMeterToTransparentMode(ComChannel comChannel) {
        if (getDlmsProperties().useMeterInTransparentMode()) {
            // TODO we ain't doing serial for the demo
//            if (ComChannelType.SERIAL_COM_CHANNEL.is(comChannel) || ComChannelType.OPTICAL_COM_CHANNEL.is(comChannel)) {
//                hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
//                SerialPortConfiguration serialPortConfiguration = ((SerialPortComChannel) comChannel).getSerialPortConfiguration();
//                int transparentConnectTime = getDlmsSessionProperties().getTransparentConnectTime();
//                int transparentDatabits = serialPortConfiguration.getNrOfDataBits().getNrOfDataBits().intValue();
//                int transparentStopbits = serialPortConfiguration.getNrOfStopBits().getNrOfStopBits().intValue();
//                int transparentParity = getParityValue(serialPortConfiguration.getParity());
//                int transparentBaudrate = serialPortConfiguration.getBaudrate().getBaudrate().intValue();
//                int authenticationSecurityLevel = Integer.parseInt(getDlmsSessionProperties().getTransparentSecurityLevel().split(":")[0]);
//                String strPassword = getDlmsSessionProperties().getTransparentPassword();
//                FlagIEC1107Connection flagIEC1107Connection = null;
//                try {
//                    flagIEC1107Connection = new FlagIEC1107Connection((SerialPortComChannel) comChannel, transparentConnectTime, transparentBaudrate, transparentDatabits, transparentStopbits, transparentParity, authenticationSecurityLevel, strPassword, getLogger(), hhuSignOn);
//                    flagIEC1107Connection.setMeterToTransparentMode();
//                } catch (Exception e) {
//                    getLogger().info("Failed to set the meter into transparent mode");
//                    if (flagIEC1107Connection != null) {
//                        flagIEC1107Connection.switchBaudrate(transparentBaudrate, HHUSignOn.PROTOCOL_HDLC);
//                    }
//                }
//                flagIEC1107Connection = null;//cleanUp this connection
//            }
        }
    }

//    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
//        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
//        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
//        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
//        hhuSignOn.enableDataReadout(false);
//        return hhuSignOn;
//    }

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
        return "$Date: 2016-09-12 15:52:00 +0300 (Mon, 12 Sep 2016)$";
    }

    /**
     * Read out the serial number, this can either be of the module (equipment identifier) or of the connected e-meter.
     * Note that reading out this register from the mirror logical device in the Beacon, the obiscode must always be 0.0.96.1.0.255
     */
    @Override
    public String getSerialNumber() {
        if (getDlmsProperties().useBeaconMirrorDeviceDialect() || !getDlmsProperties().useEquipmentIdentifierAsSerialNumber()) {
            return getMeterInfo().getSerialNr();
        } else {
            return getMeterInfo().getEquipmentIdentifier();
        }
    }

    @Override
    public Date getTime() {
        if (getDlmsProperties().useBeaconMirrorDeviceDialect()) {
            return new Date();  //Don't read out the clock of the mirror logical device, it does not know the actual meter time.
        } else {
            return super.getTime();
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        if (getDlmsProperties().useBeaconMirrorDeviceDialect()) {
            IOException cause = new IOException("When connected to the mirror logical device, writing of the clock is not allowed.");
            throw DeviceConfigurationException.notAllowedToExecuteCommand("date/time change", cause);
        } else {
            super.setTime(timeToSet);
        }
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList((DeviceProtocolDialect) new SerialDeviceProtocolDialect(thesaurus, propertySpecService));
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList((ConnectionType) new SioOpticalConnectionType(getSerialComponentService(), thesaurus), new RxTxOpticalConnectionType(getSerialComponentService(), thesaurus));
    }

    protected ConfigurationSupport getNewInstanceOfConfigurationSupport() {
        return new AM540ConfigurationSupport(propertySpecService);
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Unless of course the whole session is done with the public client, then there's no need to read out the FC.
     */
    protected void handleFC(ComChannel comChannel) {
        final int clientId = getDlmsProperties().getClientMacAddress();
        validateFCProperties(clientId);

        boolean weHaveValidCachedFrameCounter = false;
        if (getDlmsProperties().useCachedFrameCounter()) {
            weHaveValidCachedFrameCounter = getCachedFrameCounter(comChannel, clientId);
        }

        if (!weHaveValidCachedFrameCounter) {
            if (getDlmsProperties().getRequestAuthenticatedFrameCounter()) {
                if (clientId != EVN_CLIENT_MANAGEMENT) {
                    readFrameCounterSecure(comChannel);
                } else {
                    getLogger().info("Reading frame counter with client " + EVN_CLIENT_MANAGEMENT + " is not allowed. " +
                            "If communication fails please adjust your initial frame counter value to a proper one");
                }
            } else {
                if (!getDlmsProperties().usesPublicClient()) {
                    getLogger().info("Reading frame counter using normal method");
                    super.readFrameCounter(comChannel, (int) getDlmsProperties().getAARQTimeout());
                }
            }
        }
    }

    private void validateFCProperties(int clientId) {
        if (clientId == EVN_CLIENT_MANAGEMENT
                && getDlmsProperties().getRequestAuthenticatedFrameCounter()
                && !getDlmsProperties().useCachedFrameCounter()) {

            String msg = "When Client 1 is configured and "
                    + AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER
                    + " is active, we also need "
                    + AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER
                    + " to be active";
            getLogger().info(msg);

            throw DeviceConfigurationException.unsupportedPropertyValue(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER, "false");
        }
    }

    /**
     * Get the frame counter from the cache, for the given clientId.
     * If no frame counter is available in the cache (value -1), use the configured InitialFC property.
     * <p/>
     * Additionally, the FC value can be validated with ValidateCachedFrameCounterAndFallback
     */
    protected boolean getCachedFrameCounter(ComChannel comChannel, int clientId) {
        getLogger().info("Will try to use a cached frame counter");
        boolean weHaveAFrameCounter = false;
        long cachedFrameCounter = getDeviceCache().getTXFrameCounter(clientId);
        long initialFrameCounter = getDlmsProperties().getInitialFrameCounter().longValue();

        if (initialFrameCounter > cachedFrameCounter) { //Note that this is also the case when the cachedFrameCounter is unavailable (value -1)
            getLogger().info("Using initial frame counter: " + initialFrameCounter + " because it has a higher value than the cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(initialFrameCounter);
            weHaveAFrameCounter = true;
        } else if (cachedFrameCounter > 0) {
            getLogger().info("Using cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(cachedFrameCounter + 1);
            weHaveAFrameCounter = true;
        }

        if (weHaveAFrameCounter) {
            if (getDlmsProperties().validateCachedFrameCounter()) {
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
        DlmsSession testDlmsSession = new DlmsSession(comChannel, getDlmsProperties());
        int retries = getDlmsProperties().getFrameCounterRecoveryRetries();
        int step = getDlmsProperties().getFrameCounterRecoveryStep();
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
            } catch (Exception ex) {
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
                    } catch (CommunicationException e) {
                        testDlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                        // Absorb exception: in 99% of the cases we expect an exception here ...
                    }
                }
            }
            retries--;
        } while (retries > 0);

        testDlmsSession.disconnect();
        getLogger().warning("Could not validate the frame counter, seems that it's out-of synch whith the device. You'll have to read a fresh one.");
        return false;
    }

    /**
     * Read frame counter by calling a custom method in the Beacon
     */
    private void readFrameCounterSecure(ComChannel comChannel) {
        getLogger().info("Reading frame counter using secure method");
        // construct a temporary session with 0:0 security and clientId=16 (public)
        final TypedProperties publicProperties = getDlmsProperties().getProperties().clone();
        publicProperties.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
        final AM540Properties publicClientProperties = new AM540Properties(propertySpecService,thesaurus);
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return 0;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return 0;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return publicProperties;
            }
        });    //SecurityLevel 0:0
        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsProperties().getSerialNumber());
        final ObisCode frameCounterObisCode = getFrameCounterForClient(getDlmsProperties().getClientMacAddress());
        final long frameCounter;

        publicDlmsSession.getDlmsV2Connection().connectMAC();
        publicDlmsSession.createAssociation((int) getDlmsProperties().getAARQTimeout());

        try {

            FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
            frameCounter = frameCounterProvider.getFrameCounter(publicDlmsSession.getProperties().getSecurityProvider().getAuthenticationKey());

        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        } catch (Exception e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the secure frame counter, cannot continue! " + e.getMessage());
            throw new ConnectionCommunicationException(MessageSeeds.PROTOCOL_IO_PARSE_ERROR, protocolException);
        } finally {
            publicDlmsSession.disconnect();
        }

        setTXFrameCounter(frameCounter + 1);
    }

    @Override
    protected ObisCode getFrameCounterForClient(int clientId) {
        // handle some special frame-counters for EVN
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
        return super.getFrameCounterForClient(clientId); // get the standard IDIS ones
    }

    /**
     * There's 2 different ways to connect to the public client.
     * - on a mirror device, the public client has a pre-established association
     * - on an actual AM540 module, the public client requires a new association
     */
    protected void connectToPublicClient(DlmsSession publicDlmsSession) {
        if (getDlmsProperties().useBeaconMirrorDeviceDialect()) {
            publicDlmsSession.assumeConnected(getDlmsProperties().getMaxRecPDUSize(), getDlmsProperties().getConformanceBlock());
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
        if (!getDlmsProperties().useBeaconMirrorDeviceDialect()) {
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
            CommunicationException exception;
            try {
                dlmsSession.getDLMSConnection().setRetries(0);   //Temporarily disable retries in the connection layer, AARQ retries are handled here
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.getDlmsV2Connection().connectMAC();
                    dlmsSession.createAssociation((int) getDlmsProperties().getAARQTimeout());
                }
                return;
            } catch (CommunicationException e) {
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;        //Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (e instanceof ConnectionCommunicationException) {
                    throw e;
                } else if (e instanceof DataEncryptionException) {
                    throw e;
                }
                exception = e;
            } finally {
                dlmsSession.getDLMSConnection().setRetries(getDlmsProperties().getRetries());
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > getDlmsProperties().getAARQRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (getDlmsProperties().getAARQRetries() + 1) + "] tries.");
                throw new CommunicationException(com.energyict.protocols.mdc.services.impl.MessageSeeds.PROTOCOL_CONNECT_FAILED, exception);
            } else {
                getLogger().info("Unable to establish association after [" + tries + "/" + (getDlmsProperties().getAARQRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (CommunicationException e) {
                    dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
            }
        }
    }

    @Override
    public AM540Properties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = getNewInstanceOfProperties();
        }
        return (AM540Properties) dlmsProperties;
    }

    @Override
    protected AM540Properties getNewInstanceOfProperties() {
        return new AM540Properties(propertySpecService, thesaurus);
    }

    @Override
    public AM540Cache getDeviceCache() {
        if (am540Cache == null) {
            am540Cache = new AM540Cache(getDlmsProperties().useBeaconMirrorDeviceDialect());
        }
        return am540Cache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof AM540Cache)) {
            am540Cache = (AM540Cache) deviceProtocolCache;
        }
    }


    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        getDeviceCache().setConnectionToBeaconMirror(getDlmsProperties().useBeaconMirrorDeviceDialect());

        boolean readCache = getDlmsProperties().isReadCache();
        if ((getDeviceCache().getObjectList() == null) || (readCache)) {
            getLogger().info(readCache ? "ReReadCache property is true, reading cache!" : "The cache was empty, reading out the object list!");
            readObjectList();
            getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            getLogger().info("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        if (getDlmsProperties().useBeaconMirrorDeviceDialect()) {
            IOException cause = new IOException("When connected to the mirror logical device, execution of device commands is not allowed.");
            throw DeviceConfigurationException.notAllowedToExecuteCommand("send of device messages", cause);
        } else {
            return getIDISMessaging().executePendingMessages(pendingMessages);
        }
    }

    @Override
    protected AM130RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AM540RegisterFactory(this);
        }
        return registerFactory;
    }

    @Override
    public AbstractMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new IDISMeterTopology(this);
            meterTopology.searchForSlaveDevices();
        }
        return meterTopology;
    }

    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AM540Messaging(this);
        }
        return idisMessaging;
    }

    /**
     * Set the initial frame counter to be used when starting this DLMS session.
     */
    private void setTXFrameCounter(long frameCounter) {
        this.getDlmsProperties().getSecurityProvider().setInitialFrameCounter(frameCounter);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        DeviceIdentifier deviceIdentifier = getOfflineDevice().getDeviceIdentifier();
        CollectedTopology deviceTopology = super.getDeviceTopology();
        Date now = new Date();

        // save the last see date when the meter is read-out successfully
        Optional<PropertySpec> propertySpec = getPropertySpec(G3Properties.PROP_LASTSEENDATE);
        if (propertySpec.isPresent()) {
            deviceTopology.addAdditionalCollectedDeviceInfo(
                    this.getCollectedDataFactory().createCollectedDeviceProtocolProperty(
                            deviceIdentifier,
                            propertySpec.get(),
                            BigDecimal.valueOf(now.getTime())
                    )
            );
        }

        return super.getDeviceTopology();
    }
}
