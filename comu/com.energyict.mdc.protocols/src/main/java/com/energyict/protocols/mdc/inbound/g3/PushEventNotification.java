package com.energyict.protocols.mdc.inbound.g3;


import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/03/2015 - 10:27
 */
public class PushEventNotification implements BinaryInboundDeviceProtocol {

    private static final int METER_JOIN_ATTEMPT = 0xC5;
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider;
    private final SocketService socketService;
    private final IssueService issueService;

    protected InboundDiscoveryContext context;
    protected ComChannel comChannel;
    protected CollectedLogBook collectedLogBook;
    protected EventPushNotificationParser parser;

    @Inject
    public PushEventNotification(IdentificationService identificationService, CollectedDataFactory collectedDataFactory, PropertySpecService propertySpecService, Thesaurus thesaurus, MeteringService meteringService, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider, SocketService socketService, IssueService issueService) {
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.dsmrSecuritySupportProvider = dsmrSecuritySupportProvider;
        this.socketService = socketService;
        this.issueService = issueService;
    }

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }
//
//    @Override
//    public String getAdditionalInformation() {
//        return ""; //No additional info available
//    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return context;
    }

    @Override
    public DiscoverResultType doDiscovery() {
        getEventPushNotificationParser().readAndParseInboundFrame();
        collectedLogBook = getEventPushNotificationParser().getCollectedLogBook();
        context.logOnAllLoggerHandlers(getLoggingMessage(), Level.INFO);
        if (isJoinAttempt()) {
            G3GatewayPSKProvider pskProvider = getPskProvider();
            String joiningMacAddress = getMeterProtocolEvent().getMessage();
            pskProvider.addJoiningMacAddress(joiningMacAddress);
            try {
                doProvide(pskProvider, joiningMacAddress);
            } catch (CommunicationException e) {
                pskProvider.provideError(e.getMessage(), context);
            }
        }
        return DiscoverResultType.DATA;
    }

    protected void doProvide(G3GatewayPSKProvider pskProvider, String joiningMacAddress) throws CommunicationException {
        DeviceProtocolSecurityPropertySet securityPropertySet = getEventPushNotificationParser().getSecurityPropertySet();
//        Boolean onHold = getEventPushNotificationParser().getInboundComTaskOnHold();
//        if (onHold) {
//            pskProvider.provideError(getErrorMessage());
//        } else {
        pskProvider.providePSK(joiningMacAddress, securityPropertySet, getContext());
//        }
    }

    protected String getLoggingMessage() {
        StringBuilder logMessage = new StringBuilder();

        logMessage.append("Received inbound event notification from [");
        if (getDeviceIdentifier() != null) {
            logMessage.append(getDeviceIdentifier().toString());
        } else {
            logMessage.append("unknown");
        }
        logMessage.append("].  Message: '");
        if (getMeterProtocolEvent() != null) {
            logMessage.append(getMeterProtocolEvent().getMessage());
            logMessage.append("', protocol code: '");
            logMessage.append(getMeterProtocolEvent().getProtocolCode());
            logMessage.append("'");
        } else {
            logMessage.append("NULL.'");
        }

        return logMessage.toString();
    }

    protected String getErrorMessage() {
        StringBuilder errMessage = new StringBuilder();
        errMessage.append("Inbound comm task is configured for [");
        if (getDeviceIdentifier() != null) {
            errMessage.append(getDeviceIdentifier().toString());
        } else {
            errMessage.append("unknown");
        }
        errMessage.append("], but it is set on hold");
        return errMessage.toString();
    }

    private boolean isJoinAttempt() {
        return getMeterProtocolEvent().getProtocolCode() == METER_JOIN_ATTEMPT;
    }

    /**
     * Subclass for the Beacon implementation overrides this, it returns a specific PSK provider that is customized for the Beacon.
     */
    protected G3GatewayPSKProvider getPskProvider() {
        return G3GatewayPSKProviderFactory.getInstance().getPSKProvider(getDeviceIdentifier(), getContext(), dsmrSecuritySupportProvider, thesaurus, propertySpecService, socketService, issueService, identificationService, collectedDataFactory, meteringService);
    }

    private MeterProtocolEvent getMeterProtocolEvent() {
        return collectedLogBook.getCollectedMeterEvents().get(0);
    }

    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new EventPushNotificationParser(comChannel, getContext(), identificationService, collectedDataFactory, thesaurus, propertySpecService, meteringService);
        }
        return parser;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        //Nothing to do here
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return parser != null ? parser.getDeviceIdentifier() : null;
    }

    @Override
    public List<CollectedData> getCollectedData(OfflineDevice device) {
        List<CollectedData> collectedDatas = new ArrayList<>();
        collectedDatas.add(collectedLogBook);
        return collectedDatas;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-05-31 16:24:54 +0300 (Tue, 31 May 2016)$";
    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }

    public void addProperties(TypedProperties properties) {
        //No properties
    }

    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }
}