package com.energyict.protocolimplv2.dlms.hon.as300n.dlms;


import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.hon.as300n.AS300N;
import com.energyict.protocolimplv2.dlms.hon.as300n.properties.AS300NProperties;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;

public class AS300NFrameCounterHandler {

    private final ComChannel comChannel;
    private final AS300N as300n;


    public AS300NFrameCounterHandler(AS300N as300n, ComChannel comChannel) {
        this.as300n = as300n;
        this.comChannel = comChannel;
    }

    private AS300N getProtocol() {
        return this.as300n;
    }

    public void handleFrameCounter() {
        return;
    }
/*
    public void handleFC(ComChannel comChannel) {
        if (getDlmsSessionProperties().getAuthenticationSecurityLevel() < 5) {
            getProtocol().journal("Skipping FC handling due to lower security level.");
            return; // no need to handle any FC
        }

        if (!getProtocol().getDlmsSessionProperties().usesPublicClient()) {
            final int clientId = getDlmsSessionProperties().getClientMacAddress();
            validateFCProperties(clientId);

            boolean weHaveValidCachedFrameCounter = false;
            if (getDlmsSessionProperties().useCachedFrameCounter()) {
                weHaveValidCachedFrameCounter = getCachedFrameCounter(comChannel, clientId);
            }

            if (!weHaveValidCachedFrameCounter) {
                if (getDlmsSessionProperties().getRequestAuthenticatedFrameCounter()) {
                    readFrameCounterSecure(comChannel);
                }
            } else {
                try {
                    //Attempt to read out the FC with the public client.
                    //Note that this possible for every client of the DEWA AM540, but not for the management client of the EVN AM540. The object_undefined error in that case is handled below.
                    getProtocol().journal("Attempting to read out frame counter using unsecured public client");
                    readFrameCounter(comChannel, (int) getDlmsSessionProperties().getAARQTimeout());
                } catch (CommunicationException e) {
                    if (e.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE
                        || e.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_PROTOCOL_ERROR) {
                        getProtocol().journal(Level.WARNING, e.getMessage());
                    } else {
                        throw CommunicationException.protocolConnectFailed(new IOException("Cannot read out the FC of client '" + clientId + "' using the public client. Enable property '" + AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER + "' to read it out using HMAC authentication."));
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
*/

/**
 * Get the frame counter from the cache, for the given clientId.
 * If no frame counter is available in the cache (value -1), use the configured InitialFC property.
 * <p/>
 * Additionally, the FC value can be validated with ValidateCachedFrameCounterAndFallback
 */
/*
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

*/

/**
 * Sub classes (for example the crypto-protocol) can override
 */
/*
protected DlmsSession getDlmsSessionForFCTesting(ComChannel comChannel) {
        return new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
        }
*/

    /**
     * First read out the frame counter for the management client, using the public client.
     */
    /*
    protected void readFrameCounter(ComChannel comChannel, int timeout) {
        TypedProperties clone = TypedProperties.copyOf(getDlmsSessionProperties().getProperties());
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(IDIS2_CLIENT_PUBLIC));
        IDISProperties publicClientProperties = getNewInstanceOfProperties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(IDIS2_CLIENT_PUBLIC), 0, 0, 0, 0, 0, clone));    //SecurityLevel 0:0

        long frameCounter;
        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        getLogger().info("Connecting to public client:" + IDIS2_CLIENT_PUBLIC);
        connectToPublicClient(publicDlmsSession);
        try {
            ObisCode frameCounterObisCode = getFrameCounterForClient(getDlmsSessionProperties().getClientMacAddress());
            getLogger().info("Public client connected, reading framecounter " + frameCounterObisCode.toString() + ", corresponding to client " + getDlmsSessionProperties().getClientMacAddress());
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(frameCounterObisCode).getValueAttr().longValue();
            getLogger().info("Frame counter received: " + frameCounter);
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the framecounter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        } finally {
            getLogger().info("Disconnecting public client");
            disconnectFromPublicClient(publicDlmsSession);
        }

        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }
    */

/**
 * Read frame counter by calling a custom method in the Beacon
 */
/*
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
*/
/**
 * There's 2 different ways to connect to the public client.
 * - on a mirror device, the public client has a pre-established association
 * - on an actual AM540 module, the public client requires a new association
 */
/*
protected void connectToPublicClient(DlmsSession publicDlmsSession) {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
        publicDlmsSession.assumeConnected(getDlmsSessionProperties().getMaxRecPDUSize(), getDlmsSessionProperties().getConformanceBlock());
        } else {
        super.connectToPublicClient(publicDlmsSession);
        }
        }
*/
/**
 * There's 2 different ways to disconnect from the public client.
 * - on a mirror device, the public client is pre-established, so no need to release the association
 * - on an actual AM540 module, the public client requires a new association
 */
/*
protected void disconnectFromPublicClient(DlmsSession publicDlmsSession) {
        if (!getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
        super.disconnectFromPublicClient(publicDlmsSession);
        }
        }

        }

    private void getDlmsSessionProperties() {
    }
    */

}