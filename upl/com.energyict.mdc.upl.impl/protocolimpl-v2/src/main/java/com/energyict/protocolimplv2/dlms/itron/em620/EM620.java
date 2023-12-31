/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.itron.em620;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
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
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;

import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.itron.em620.logbooks.EM620LogBookFactory;
import com.energyict.protocolimplv2.dlms.itron.em620.messages.EM620Messaging;
import com.energyict.protocolimplv2.dlms.itron.em620.profiledata.EM620ProfileDataReader;
import com.energyict.protocolimplv2.dlms.itron.em620.properties.EM620ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.itron.em620.properties.EM620Properties;
import com.energyict.protocolimplv2.dlms.itron.em620.registers.EM620DlmsStoredValues;
import com.energyict.protocolimplv2.dlms.itron.em620.registers.EM620RegisterFactory;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.energyict.dlms.common.DlmsProtocolProperties.READCACHE_PROPERTY;
import static com.energyict.protocolimplv2.dlms.itron.em620.registers.EM620RegisterFactory.BILLING_PROFILE_OBIS_CODE;

public class EM620 extends AbstractDlmsProtocol {

    private EM620Properties em620Properties;
    private EM620RegisterFactory registerFactory;
    private final NlsService nlsService;
    private EM620ConfigurationSupport em620ConfigurationSupport;
    private EM620Messaging messaging;
    private EM620LogBookFactory logBookFactory;
    private EM620ProfileDataReader loadProfileReader;
    private final Converter converter;
    private EM620Cache deviceCache = null;
    private final TariffCalendarExtractor calendarExtractor;

    protected static final int PUBLIC_CLIENT = 16;
    protected static final int HIGH_SECURITY_LEVEL = 5;

    private static final ObisCode LOGICAL_DEVICE_NAME_OBIS = ObisCode.fromString("0.0.96.1.0.255");
    protected static final ObisCode FRAME_COUNTER_MANAGEMENT_ONLINE = ObisCode.fromString("0.0.43.1.0.255");

    public EM620(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        journal(getLogPrefix() + "Starting protocol for " + offlineDevice.getSerialNumber() + " (version: " + getVersion() + ")");
        handleFC(comChannel);
        setDlmsSession(new EM620DlmsSession(comChannel, getDlmsSessionProperties()));
        journal(getLogPrefix() + "Protocol init successful");
    }

    /**
     * Connect to the device and check the cached object list.
     */
    @Override
    public void logOn() {
        connectWithRetries(getDlmsSession());
        checkCacheObjects();
    }

    @Override
    public void logOff() {
        getDlmsSession().getDlmsV2Connection().disconnectMAC();
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
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(getPropertySpecService()));
        result.add(new SioAtModemConnectionType(getPropertySpecService()));
        result.add(new RxTxAtModemConnectionType(getPropertySpecService()));
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "Itron EM620 DLMS";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileReader().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileReader().getLoadProfileData(loadProfiles);
    }

    private EM620ProfileDataReader getLoadProfileReader() {
        if (loadProfileReader == null) {
            loadProfileReader = createLoadProfileReader();
        }
        return loadProfileReader;
    }

    protected EM620ProfileDataReader createLoadProfileReader() {
        return new EM620ProfileDataReader(this, getCollectedDataFactory(), getIssueFactory());
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

    private EM620LogBookFactory createLogBookFactory() {
        return new EM620LogBookFactory(this);
    }

    private EM620Messaging createEM620Messaging() {
        return new EM620Messaging(this, this.converter, getNlsService(), getPropertySpecService(), this.calendarExtractor);
    }

    private EM620Messaging getEM620Messaging() {
        if (this.messaging == null) {
            this.messaging = createEM620Messaging();
        }
        return messaging;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getEM620Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getEM620Messaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getEM620Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getEM620Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(
                new SerialDeviceProtocolDialect(getPropertySpecService(), getNlsService()),
                new TcpDeviceProtocolDialect(getPropertySpecService(), getNlsService()));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    private EM620RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new EM620RegisterFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return this.registerFactory;
    }

    @Override
    public String getVersion() {
        return "$Date: 2023-05-04$";
    }

    @Override
    protected void readObjectList() {
        if (getDlmsSessionProperties().useHardcodedObjectList()) {
            getLogger().info("Hardcoded object list will be used");
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(EM620ObjectList.getObjectList());
        } else {
            getLogger().info("Reading the object list from the meter. This might take several minutes.");
            super.readObjectList();
        }
    }

    @Override
    public EM620Cache getDeviceCache() {
        return deviceCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceCache = (EM620Cache) deviceProtocolCache;
    }

    @Override
    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new EM620Cache());
        }
        EM620Cache dlmsCache = getDeviceCache();

        if (dlmsCache.getObjectList() == null || getDlmsSessionProperties().getProperties().getTypedProperty(READCACHE_PROPERTY, false)) {
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    protected NlsService getNlsService() {
        return nlsService;
    }

    @Override
    public EM620Properties getDlmsSessionProperties() {
        if (em620Properties == null) {
            em620Properties = new EM620Properties();
        }
        return em620Properties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (em620ConfigurationSupport == null) {
            em620ConfigurationSupport = new EM620ConfigurationSupport(getPropertySpecService());
        }
        return em620ConfigurationSupport;
    }

    @Override
    public String getSerialNumber() {
        // The configured serial number in EiServer should match the Logical device name of the device not the manufacturing number
        try {
            return getLogicalDeviceName();
        } catch (IOException e) {
            journal(Level.SEVERE, e.getLocalizedMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
        }
    }

    @Override
    public void terminate() {
        // As a last step, update the cache with the last FC
        if (getDlmsSession() != null && getDlmsSession().getAso() != null && getDlmsSession().getAso().getSecurityContext() != null) {
            long frameCounter = getDlmsSession().getAso().getSecurityContext().getFrameCounter();
            int clientMacAddress = getDlmsSessionProperties().getClientMacAddress();
            if (getDeviceCache() != null) {
                getDeviceCache().setTXFrameCounter(clientMacAddress, frameCounter);
                journal(getLogPrefix() + "Caching frameCounter=" + frameCounter + " for client=" + clientMacAddress);
            }
            journal(getLogPrefix() + "Meter protocol session ended.");
        }
    }

    public EM620DlmsStoredValues getStoredValues() {
        return new EM620DlmsStoredValues(getDlmsSession(), BILLING_PROFILE_OBIS_CODE);
    }

    protected long getFrameCounter(DlmsSession publicDlmsSession) throws IOException {
        journal("Public client connected, reading frame counter " + FRAME_COUNTER_MANAGEMENT_ONLINE.toString() + ", corresponding to client " + getDlmsSessionProperties().getClientMacAddress());
        long frameCounter = publicDlmsSession.getCosemObjectFactory().getData(FRAME_COUNTER_MANAGEMENT_ONLINE).getValueAttr().longValue();
        journal("Frame counter received: " + frameCounter);
        return frameCounter;
    }

    protected String getLogicalDeviceName() throws IOException {
        journal("Reading COSEM logical device name " + LOGICAL_DEVICE_NAME_OBIS.toString() + ", corresponding to client " + getDlmsSessionProperties().getClientMacAddress());
        String logicalDeviceName = getDlmsSession().getCosemObjectFactory().getData(LOGICAL_DEVICE_NAME_OBIS).getValueAttr().getOctetString().stringValue();
        journal("COSEM logical device name received: " + logicalDeviceName);
        return logicalDeviceName;
    }

    protected String getLogPrefix() {
        return "[" + this.offlineDevice.getSerialNumber() + "] ";
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Unless of course the whole session is done with the public client, then there's no need to read out the FC.
     */
    private void handleFC(ComChannel comChannel) {
        if (getDlmsSessionProperties().getAuthenticationSecurityLevel() < HIGH_SECURITY_LEVEL) {
            journal(getLogPrefix() + "Skipping FC handling due to lower security level.");
            return; // no need to handle any FC
        }

        if (!getDlmsSessionProperties().usesPublicClient()) {
            final int clientId = getDlmsSessionProperties().getClientMacAddress();

            boolean weHaveValidCachedFrameCounter = false;
            if (getDlmsSessionProperties().useCachedFrameCounter()) {
                weHaveValidCachedFrameCounter = getAndValidateCachedFrameCounter(comChannel, clientId);
            }

            if (!weHaveValidCachedFrameCounter) {
                readFrameCounter(comChannel);
            }
        }
    }

    protected void readFrameCounter(ComChannel comChannel) {
        // construct a temporary session with 0:0 security and clientId=16 (public)
        final TypedProperties publicProperties = TypedProperties.copyOf(getDlmsSessionProperties().getProperties());
        publicProperties.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
        final EM620Properties publicClientProperties = new EM620Properties();
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT), 0, 0, 0, 0, 0, publicProperties));    //SecurityLevel 0:0

        final EM620DlmsSession publicDlmsSession = new EM620DlmsSession(comChannel, publicClientProperties);
        final long frameCounter;

        try {
            publicDlmsSession.getDlmsV2Connection().connectMAC();
            publicDlmsSession.createAssociation((int) getDlmsSessionProperties().getAARQTimeout());
            frameCounter = getFrameCounter(publicDlmsSession);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handleRecoverable(e, publicDlmsSession.getProperties().getRetries() + 1);
        } catch (Exception e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the secure frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException); // this leaves the connection intact
        } finally {
            publicDlmsSession.getDlmsV2Connection().disconnectMAC();
        }
        setTXFrameCounter(frameCounter + 1);
    }

    private void setTXFrameCounter(long frameCounter) {
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    /**
     * Get the frame counter from the cache, for the given clientId.
     * If no frame counter is available in the cache (value -1), use the configured InitialFC property.
     * <p/>
     * Additionally, the FC value can be validated with ValidateCachedFrameCounterAndFallback
     */
    private boolean getAndValidateCachedFrameCounter(ComChannel comChannel, int clientId) {
        journal(getLogPrefix() + "Will use the cached frame counter");
        boolean weHaveAFrameCounter = false;
        long cachedFrameCounter = getDeviceCache().getTXFrameCounter(clientId);

        if (cachedFrameCounter > 0) {
            journal(getLogPrefix() + "Using cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(cachedFrameCounter + 1);
            weHaveAFrameCounter = true;
        }

        if (weHaveAFrameCounter) {
            if (getDlmsSessionProperties().validateCachedFrameCounter()) {
                return testConnectionAndRetryWithFrameCounterIncrements(comChannel);
            } else {
                journal(getLogPrefix() + " - cached frame counter will not be validated - if the communication fails please set the cache property back to {No}, so a fresh one will be read-out");
                // do not validate, just use it and hope for the best
                return true;
            }
        }
        return false;
    }

    private boolean testConnectionAndRetryWithFrameCounterIncrements(ComChannel comChannel) {
        DlmsSession testDlmsSession;
        try {
            testDlmsSession = getDlmsSessionForFCTesting(comChannel);
        } catch (Exception ex) {
            final ProtocolException protocolException = new ProtocolException(ex, "Error while establishing a test DLMS session: " + ex.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException); // this leaves the connection intact
        }

        int retries = getDlmsSessionProperties().getFrameCounterRecoveryRetries();
        int step = getDlmsSessionProperties().getFrameCounterRecoveryStep();
        boolean releaseOnce = true;

        journal(getLogPrefix() + "Will test the frameCounter. Recovery mechanism: retries=" + retries + ", step=" + step);
        if (retries <= 0) {
            retries = 0;
            step = 0;
        }
        do {
            try {
                testDlmsSession.getDlmsV2Connection().connectMAC();
                testDlmsSession.createAssociation();
                if (testDlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                    testDlmsSession.getDlmsV2Connection().disconnectMAC();
                    journal(getLogPrefix() + "Cached FrameCounter is valid!");
                    setTXFrameCounter(testDlmsSession.getAso().getSecurityContext().getFrameCounter());
                    return true;
                }
            } catch (CommunicationException ex) {
                if (isAssociationFailed(ex)) {
                    long frameCounter = testDlmsSession.getAso().getSecurityContext().getFrameCounter();
                    journal(getLogPrefix() + "Current frame counter [" + frameCounter + "] is not valid, received exception " + ex.getMessage() + ", increasing frame counter by " + step);
                    frameCounter += step;
                    setTXFrameCounter(frameCounter);
                    testDlmsSession.getAso().getSecurityContext().setFrameCounter(frameCounter);

                    if (releaseOnce) {
                        releaseOnce = false;
                        //Try to release that association once, it may be that it was still open from a previous session, causing troubles to create the new association.
                        try {
                            testDlmsSession.getDlmsV2Connection().disconnectMAC();
                        } catch (ProtocolRuntimeException e) {
                            testDlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                            // Absorb exception: in 99% of the cases we expect an exception here ...
                        }
                    }
                } else {
                    final ProtocolException protocolException = new ProtocolException(ex, "Communication-Exception while recovering the frame counter, cannot continue! " + ex.getMessage());
                    throw ConnectionCommunicationException.unExpectedProtocolError(protocolException); // this leaves the connection intact
                }
            } catch (Exception ex) {
                final ProtocolException protocolException = new ProtocolException(ex, "Error while recovering the frame counter, cannot continue! " + ex.getMessage());
                throw ConnectionCommunicationException.unExpectedProtocolError(protocolException); // this leaves the connection intact
            }
            retries--;
        } while (retries > 0);

        testDlmsSession.getDlmsV2Connection().disconnectMAC();
        journal(getLogPrefix() + "Could not validate the frame counter, seems that it's out-of sync with the device. You'll have to read a fresh one.");
        return false;
    }

    protected DlmsSession getDlmsSessionForFCTesting(ComChannel comChannel) {
        return new EM620DlmsSession(comChannel, getDlmsSessionProperties());
    }

    private boolean isAssociationFailed(CommunicationException ex) {
        return ex.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE
                || ex.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_PROTOCOL_ERROR
                || ex.getMessageSeed() == ProtocolExceptionMessageSeeds.PROTOCOL_CONNECT;
    }
}
