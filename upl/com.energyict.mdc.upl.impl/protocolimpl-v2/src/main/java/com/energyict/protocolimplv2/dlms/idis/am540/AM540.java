package com.energyict.protocolimplv2.dlms.idis.am540;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.TypedProperties;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.connection.FlagIEC1107Connection;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.*;
import com.energyict.protocol.support.FrameCounterCache;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.AM540RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The AM540 is a PLC E-meter designed according to IDIS package 2 specifications <br/>
 * The protocol is an extension of the AM130 protocol (which is the GPRS variant designed according to IDIS P2)
 *
 * The protocol supports also EVN Netz-NO Companion standard specification (security-related).
 *
 * @author sva
 * @since 11/08/2015 - 14:04
 */
public class AM540 extends AM130 implements SerialNumberSupport, FrameCounterCache {
    protected static final ObisCode EVN_FRAMECOUNTER_DATA_READOUT   = ObisCode.fromString("0.0.43.3.0.255");
    protected static final ObisCode EVN_FRAMECOUNTER_INSTALLATION   = ObisCode.fromString("0.0.43.4.0.255");
    protected static final ObisCode EVN_FRAMECOUNTER_MAINTENANCE    = ObisCode.fromString("0.0.43.5.0.255");
    protected static final ObisCode EVN_FRAMECOUNTER_CERTIFICATION  = ObisCode.fromString("0.0.43.6.0.255");

    protected static final int EVN_CLIENT_MANAGEMENT        = 1;
    protected static final int EVN_CLIENT_DATA_READOUT      = 2;
    protected static final int EVN_CLIENT_FW_UPGRADE        = 3;
    protected static final int EVN_CLIENT_INSTALLATION      = 5;
    protected static final int EVN_CLIENT_MAINTENANCE       = 6;
    protected static final int EVN_CLIENT_CERTIFICATION     = 7;
    protected static final int PUBLIC_CLIENT                = 16;
    protected static final int EVN_CLIENT_CUSTOMER_INFORMATION_PUSH    = 103;



    private AM540Cache am540Cache;
    private HHUSignOnV2 hhuSignOn;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        getLogger().info("Start protocol for " + offlineDevice.getSerialNumber());
        getLogger().info("-version: "+getVersion());
        initDlmsSession(comChannel);
        getLogger().info("Protocol initialization phase ended, executing tasks ...");
    }

    private void initDlmsSession(ComChannel comChannel) {
        setMeterToTransparentMode(comChannel);
        readFrameCounter(comChannel);
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
        initFrameCounterCache(null);
    }

    private void setMeterToTransparentMode(ComChannel comChannel) {
        if (getDlmsSessionProperties().useMeterInTransparentMode()){
            if (ComChannelType.SerialComChannel.is(comChannel) || ComChannelType.OpticalComChannel.is(comChannel)) {
                hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
                SerialPortConfiguration serialPortConfiguration = ((SerialPortComChannel) comChannel).getSerialPortConfiguration();
                int transparentConnectTime = getDlmsSessionProperties().getTransparentConnectTime();
                int transparentDatabits = serialPortConfiguration.getNrOfDataBits().getNrOfDataBits().intValue();
                int transparentStopbits = serialPortConfiguration.getNrOfStopBits().getNrOfStopBits().intValue();
                int transparentParity = getParityValue(serialPortConfiguration.getParity());
                int transparentBaudrate = serialPortConfiguration.getBaudrate().getBaudrate().intValue();
                int authenticationSecurityLevel = Integer.parseInt(getDlmsSessionProperties().getTransparentSecurityLevel().split(":")[0]);
                String strPassword = getDlmsSessionProperties().getTransparentPassword();
                FlagIEC1107Connection flagIEC1107Connection = null;
                try {
                    flagIEC1107Connection = new FlagIEC1107Connection((SerialPortComChannel) comChannel, transparentConnectTime, transparentBaudrate, transparentDatabits, transparentStopbits, transparentParity, authenticationSecurityLevel, strPassword, getLogger(), hhuSignOn);
                    flagIEC1107Connection.setMeterToTransparentMode();
                } catch (Exception e) {
                    getLogger().info("Failed to set the meter into transparent mode");
                    if(flagIEC1107Connection != null){
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

    private int getParityValue(Parities parities){
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
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList((DeviceProtocolDialect) new SerialDeviceProtocolDialect());
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList((ConnectionType) new SioOpticalConnectionType(), new RxTxOpticalConnectionType());
    }

    protected ConfigurationSupport getNewInstanceOfConfigurationSupport() {
        return new AM540ConfigurationSupport();
    }

    protected void initFrameCounterCache(SecurityContext securityContext){
        if (securityContext == null) {
            securityContext = getDlmsSession().getAso().getSecurityContext();
        }
        securityContext.setFrameCounterCache(getDlmsSessionProperties().getClientMacAddress(), getDeviceCache());
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Unless of course the whole session is done with the public client, then there's no need to read out the FC.
     */
    protected void readFrameCounter(ComChannel comChannel) {
        boolean weHaveValidCachedFrameCounter = false;

        if (getDlmsSessionProperties().useCachedFrameCounter()) {
            weHaveValidCachedFrameCounter = readAndTestCachedFrameCounter(comChannel);
        }

        if (!weHaveValidCachedFrameCounter) {
            if (getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()) {
                readFrameCounterSecure(comChannel);
            } else {
                if (!getDlmsSessionProperties().usesPublicClient()) {
                    getLogger().info("Reading frame counter using normal method");
                    super.readFrameCounter(comChannel, (int)getDlmsSessionProperties().getAARQTimeout());
                }
            }
        }
    }




    /**
     * Before using an cached frame counter, test it first to see if it's still good
     * If not, we shall resume back to public client initialization
     *
     * @return
     */
    protected boolean readAndTestCachedFrameCounter(ComChannel comChannel){
        getLogger().info("Will try to use a cached frame counter");
        final int clientId = getDlmsSessionProperties().getClientMacAddress();
        long cachedFrameCounter = getDeviceCache().getTXFrameCounter(clientId);
        if (cachedFrameCounter > 0) {
            getLogger().info(" - cached frame counter: "+cachedFrameCounter);
            this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(cachedFrameCounter + 1);
            if (getDlmsSessionProperties().validateCachedFrameCounter()){
                return testConnectionAndRetryWithFrameCounterIncrements(comChannel);
            } else {
                getLogger().warning(" - cached frame counter will not be validated - if the communication fails please set the cache property back to {No}, so a fresh one will be read-out");
                // do not validate, just use it and hope for the best
                return true;
            }
        } else {
            getLogger().warning("Cache does not have a cached frame counter for clientId="+clientId);
        }

        return false;
    }

    protected boolean testConnectionAndRetryWithFrameCounterIncrements(ComChannel comChannel){
        DlmsSession testDlmsSession = new DlmsSession(comChannel, getDlmsSessionProperties());
        int retries = getDlmsSessionProperties().getFrameCounterRecoveryRetries() ;
        int step = getDlmsSessionProperties().getFrameCounterRecoveryStep();

        getLogger().info("Will test the frameCounter. Recovery mechanism: retries="+retries+", step="+step);
        if (retries<=0){
            retries = 0;
            step = 0;
        }

        initFrameCounterCache(testDlmsSession.getAso().getSecurityContext());

        do{
            try {
                testDlmsSession.getDlmsV2Connection().connectMAC();
                testDlmsSession.createAssociation();
                if (testDlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                    //testDlmsSession.disconnect();
                    getLogger().info("Cached FrameCounter is valid!");
                    return true;
                }
            } catch (CommunicationException ex) {
                long frameCounter = this.getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter();
                getLogger().warning("Current frame counter ["+frameCounter+"] is not valid, received exception "+ex.getMessage()+", increasing frame counter by "+step);
                frameCounter += step;
                this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter);
                setTXFrameCounter(getDlmsSessionProperties().getClientMacAddress(), (int) frameCounter);
            } catch (Exception ex){
                getLogger().warning("Received an unexpected exception while testing the frame counter "+ex.getMessage());
                throw ex;
            }
            retries--;
        } while (retries>0);

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
        final TypedProperties publicProperties = getDlmsSessionProperties().getProperties().clone();
        publicProperties.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
        final AM540Properties publicClientProperties = new AM540Properties();
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(0, 0, publicProperties));    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsSessionProperties().getSerialNumber());
        final ObisCode frameCounterObisCode = getFrameCounterForClient(PUBLIC_CLIENT);
        final long frameCounter;

        publicDlmsSession.getDlmsV2Connection().connectMAC();
        publicDlmsSession.createAssociation((int) getDlmsSessionProperties().getAARQTimeout());

        try {

            FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
            frameCounter = frameCounterProvider.getFrameCounter(publicDlmsSession.getProperties().getSecurityProvider().getAuthenticationKey());

        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        } catch (Exception e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the secure frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } finally {
            publicDlmsSession.disconnect();
        }
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    @Override
    protected ObisCode getFrameCounterForClient(int clientId){
        // handle some special frame-counters for EVN
        switch (clientId){
            case EVN_CLIENT_DATA_READOUT:
                return EVN_FRAMECOUNTER_DATA_READOUT;
            case PUBLIC_CLIENT:
                if(getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()){
                    return EVN_FRAMECOUNTER_DATA_READOUT;
                }
                break;
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
        return new AM540Properties();
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


    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        getDeviceCache().setConnectionToBeaconMirror(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());

        boolean readCache = getDlmsSessionProperties().isReadCache();
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

    @Override
    public void setTXFrameCounter(int clientId, int frameCounter) {
        if (clientId == getDlmsSessionProperties().getClientMacAddress()) {
            getDlmsSessionProperties().getSecurityProvider().getRespondingFrameCounterHandler().resetRespondingFrameCounter(frameCounter);
        }
    }

    @Override
    public long getTXFrameCounter(int clientId) {
        return 0;
    }
}
