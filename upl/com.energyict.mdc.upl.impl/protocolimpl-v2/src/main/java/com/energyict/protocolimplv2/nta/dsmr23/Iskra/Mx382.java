package com.energyict.protocolimplv2.nta.dsmr23.Iskra;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.dsmr.OutboundTcpIpWithWakeUpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
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
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.Dsmr23Properties;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class Mx382 extends AbstractSmartNtaProtocol {
    protected Mx382RegisterFactory registerFactory;
    private Dsmr23Messaging dsmr23Messaging;
    private Dsmr23MessageExecutor dsmr23MessageExecutor;

    private final NlsService nlsService;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final NumberLookupExtractor numberLookupExtractor;
    private final LoadProfileExtractor loadProfileExtractor;
    public Mx382(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                 NlsService nlsService,KeyAccessorTypeExtractor keyAccessorTypeExtractor,
                 Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor,
                 NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
        this.loadProfileExtractor = loadProfileExtractor;
    }

    protected NlsService getNlsService() {return this.nlsService;}
    protected KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {return keyAccessorTypeExtractor;}
    protected Converter getConverter () {return converter;}
    protected DeviceMessageFileExtractor getDeviceMessageFileExtractor () {return messageFileExtractor;}
    protected TariffCalendarExtractor getTariffCalendarExtractor () {return calendarExtractor;}
    protected NumberLookupExtractor getNumberLookupExtractor () {return numberLookupExtractor;}
    protected LoadProfileExtractor getLoadProfileExtractor () {return loadProfileExtractor;}

    private static final int PUBLIC_CLIENT_MAC_ADDRESS = 16;
    private static final long UNSIGNED32_MAX = 0xFFFFFFFFL; // 4294967295;
    private static final ObisCode FRAME_COUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.0.255");

    private Mx382Cache mx382Cache;

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Negative;
    }

    @Override
    public String getVersion() {
        return "$Date: 2020-07-14$";
    }

    @Override
    public void journal(String message) {
        super.journal("[Mx382] " + message);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        journal("Iskra Mx382 protocol init V2 " + getVersion());
        this.offlineDevice = offlineDevice;
        String serialNumber = getDlmsSessionProperties().getSerialNumber();
        getDlmsSessionProperties().setSerialNumber(serialNumber);
        journal("Initialize communication with device identified by serial number: " + serialNumber);

        HHUSignOnV2 hhuSignOn = null;
        if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }

        if (getDlmsSessionProperties().replayAttackPreventionEnabled()) {
            if (!testCachedFrameCounterAndEstablishAssociation(comChannel)) {
                readFrameCounter(comChannel);
                reEstablishAssociation(comChannel);
            } else {
                // Frame counter was validated and DLMSSession set so go on
                journal("Secure association established");
            }
        } else {
            // It uses a random initial frame counter
            setDlmsSession(newDlmsSession(comChannel));
        }

        getDlmsSession().getDLMSConnection().setSNRMType(1);//Uses a specific parameter length for the HDLC signon (SNRM request)
        journal("Initialization phase has ended.");
    }

    protected boolean testCachedFrameCounterAndEstablishAssociation(ComChannel comChannel) {
        boolean validCachedFrameCounter = false;
        long cachedFrameCounter = getDeviceCache().getFrameCounter();
        if (checkFrameCounterLimits(cachedFrameCounter)) {
            journal("Testing cached frame counter: " + cachedFrameCounter);
            getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(cachedFrameCounter);
            DlmsSession dlmsSession = newDlmsSession(comChannel);
            dlmsSession.getAso().getSecurityContext().setFrameCounter(cachedFrameCounter);
            try {
                dlmsSession.getDlmsV2Connection().connectMAC();
                dlmsSession.createAssociation();
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                    long frameCounter = dlmsSession.getAso().getSecurityContext().getFrameCounter();
                    journal("This frame counter was validated: " + frameCounter);
                    getDeviceCache().setFrameCounter(frameCounter);
                    validCachedFrameCounter = true;
                    setDlmsSession(dlmsSession);
                }
            } catch (CommunicationException | com.energyict.protocol.exceptions.ConnectionCommunicationException ex) {
                journal("Association using cached frame counter has failed.");
            } catch (Exception ex) {
                journal(Level.SEVERE, ex.getLocalizedMessage() + " caused by " + ex.getCause().getLocalizedMessage());
                throw ex;
            }
        } else {
            journal("Cached frame counter ("+cachedFrameCounter+") is outside acceptable limits, will force reading it again.");
        }
        return validCachedFrameCounter;
    }

    protected void readFrameCounter(ComChannel comChannel) {

        if (getDlmsSessionProperties().getClientMacAddress() == PUBLIC_CLIENT_MAC_ADDRESS) {
            return;
        }

        journal("Starting public DLMS session to read the frame counter.");

        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT_MAC_ADDRESS));
        Dsmr23Properties publicClientProperties = new Dsmr23Properties();
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT_MAC_ADDRESS), 0, 0, 0, 0, 0, clone));    //SecurityLevel 0:0

        long frameCounter;
        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        journal("Connecting to public client: " + PUBLIC_CLIENT_MAC_ADDRESS);
        connectWithRetries(publicDlmsSession);
        try {
            journal("Public client connected, reading frame counter " + FRAME_COUNTER_OBISCODE.toString() + ", corresponding to client "+getDlmsSessionProperties().getClientMacAddress());
            frameCounter = publicDlmsSession.getCosemObjectFactory().getData(FRAME_COUNTER_OBISCODE).getValueAttr().longValue();
            journal("Frame counter received: " + frameCounter);
            checkFrameCounterLimits(frameCounter);
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        }
        journal("Disconnecting public client");
        publicDlmsSession.disconnect();
        long incrementedFrameCounter = frameCounter + 1;
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(incrementedFrameCounter);
    }

    private boolean checkFrameCounterLimits(long frameCounter) {
        boolean isValid = true;
        long frameCounterLimit = getDlmsSessionProperties().getFrameCounterLimit();

        if (frameCounterLimit > 0 && frameCounter > frameCounterLimit) {
            journal(Level.WARNING, "Frame counter "+frameCounter+" is above the threshold (" + frameCounterLimit + "), consider key-roll to reset it!");
            isValid = false;
        }

        if (frameCounter <= 0 || frameCounter >= UNSIGNED32_MAX) {
            journal(Level.SEVERE, "Frame counter "+frameCounter+" is not within Unsigned32 acceptable limits, consider key-roll to reset it");
            isValid = false;
        }
        return isValid;
    }

    private void reEstablishAssociation(ComChannel comChannel) {
        DlmsSession dlmsSession = newDlmsSession(comChannel);
        long initialFrameCounter = getDlmsSessionProperties().getSecurityProvider().getInitialFrameCounter();
        journal("Re-establishing secure association with frame counter "+initialFrameCounter);
        dlmsSession.getAso().getSecurityContext().setFrameCounter(initialFrameCounter);
        setDlmsSession(dlmsSession);
        if (dlmsSession.getAso().getAssociationStatus() != ApplicationServiceObject.ASSOCIATION_CONNECTED) {
            try {
                dlmsSession.getDlmsV2Connection().connectMAC();
                dlmsSession.createAssociation();
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                    journal("Reconnection successful");
                }
            } catch (CommunicationException ex) {
                journal("Association using the new frame counter has failed ("+ex.getLocalizedMessage()+")");
                throw ex;
            } catch (Exception ex) {
                journal(Level.SEVERE, ex.getLocalizedMessage() + " caused by " + ex.getCause().getLocalizedMessage());
                throw ex;
            }
        } else {
            journal("Secure connection already established");
        }
    }

    @Override
    public void terminate() {
        if (saveFrameCounterToCache()) {
            long frameCounter = getDlmsSession().getAso().getSecurityContext().getFrameCounter();
            getDeviceCache().setFrameCounter(frameCounter);
            journal("Caching frame counter = " + frameCounter);
        }
    }

    private boolean saveFrameCounterToCache() {
        return getDlmsSession() != null &&
               getDlmsSession().getAso().getSecurityContext() != null &&
               getDlmsSessionProperties().replayAttackPreventionEnabled();
    }

    @Override
    public Mx382Cache getDeviceCache() {
        if (mx382Cache == null) {
            mx382Cache = new Mx382Cache();
        }
        return mx382Cache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if (deviceProtocolCache instanceof Mx382Cache) {
            mx382Cache = (Mx382Cache) deviceProtocolCache;
        }
    }

    @Override
    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new Mx382Cache());
        }
        DLMSCache dlmsCache = getDeviceCache();
        if (dlmsCache.getObjectList() == null) {
            journal("Cache is empty. Reading device object list.");
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
            journal("Cache exist, will not be read.");
        }
    }

    protected HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    protected DlmsSession newDlmsSession(ComChannel comChannel) {
        return new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
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
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
        result.add(new OutboundTcpIpWithWakeUpConnectionType(this.getPropertySpecService()));
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "Iskra Mx382 DLMS (NTA DSMR2.3)";
    }

    @Override
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new TcpDeviceProtocolDialect(this.getPropertySpecService(), this.nlsService));
    }

    protected Dsmr23MessageExecutor getMessageExecutor() {
        if (this.dsmr23MessageExecutor == null) {
            this.dsmr23MessageExecutor = new Dsmr23MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.keyAccessorTypeExtractor);
        }
        return this.dsmr23MessageExecutor;
    }

    protected Dsmr23Messaging getDsmr23Messaging() {
        if (this.dsmr23Messaging== null) {
            this.dsmr23Messaging= new Dsmr23Messaging(getMessageExecutor(), this.getPropertySpecService(),
                    this.nlsService, this.converter, this.messageFileExtractor, this.calendarExtractor,
                    this.numberLookupExtractor, this.loadProfileExtractor, this.keyAccessorTypeExtractor);
        }
        return this.dsmr23Messaging;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getDsmr23Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDsmr23Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getDsmr23Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public DeviceRegisterSupport getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Mx382RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.registerFactory;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new Mx382ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return false;
    }

    @Override
    public Dsmr23Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new Dsmr23Properties();
        }
        return (Dsmr23Properties) dlmsProperties;
    }

}
