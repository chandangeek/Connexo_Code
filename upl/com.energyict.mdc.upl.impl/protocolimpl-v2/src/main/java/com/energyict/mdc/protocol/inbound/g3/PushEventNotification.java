package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exceptions.CommunicationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

    protected InboundDiscoveryContext context;
    protected ComChannel comChannel;
    protected CollectedLogBook collectedLogBook;
    protected EventPushNotificationParser parser;

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    @Override
    public String getAdditionalInformation() {
        return ""; //No additional info available
    }

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
                pskProvider.provideError(e.getMessage());
            }
        }
        return DiscoverResultType.DATA;
    }

    protected void doProvide(G3GatewayPSKProvider pskProvider, String joiningMacAddress) throws CommunicationException {
        DeviceProtocolSecurityPropertySet securityPropertySet = getEventPushNotificationParser().getSecurityPropertySet();
        Boolean onHold = getEventPushNotificationParser().getInboundComTaskOnHold();
        if (onHold) {
            pskProvider.provideError(getErrorMessage());
        } else {
            pskProvider.providePSK(joiningMacAddress, securityPropertySet);
        }
    }

    protected String getLoggingMessage() {
        StringBuilder logMessage = new StringBuilder();
        try {
            logMessage.append("Received inbound event notification from [");
            if (getDeviceIdentifier() != null) {
                logMessage.append(getDeviceIdentifier().toString());
            } else {
                logMessage.append("unknown");
            }

            if (collectedLogBook.getCollectedMeterEvents() != null) {
                logMessage.append("].  Message: '");
                Iterator<MeterProtocolEvent> iterator = collectedLogBook.getCollectedMeterEvents().iterator();
                while (iterator.hasNext()) {
                    MeterProtocolEvent collectedEvent = iterator.next();
                    if (collectedEvent != null) {
                        logMessage.append(collectedEvent.getMessage());
                        logMessage.append("', protocol code: '");
                        logMessage.append(collectedEvent.getProtocolCode());
                        logMessage.append("'.");
                    } else {
                        logMessage.append("NULL.'");
                    }
                }
            }
        } catch (Exception ex){
            logMessage.append(ex.getCause()).append(ex.getMessage());
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
        try {
            return getMeterProtocolEvent().getProtocolCode() == METER_JOIN_ATTEMPT;
        } catch (Exception ex){
            return false;
        }
    }

    /**
     * Subclass for the Beacon implementation overrides this, it returns a specific PSK provider that is customized for the Beacon.
     */
    protected G3GatewayPSKProvider getPskProvider() {
        return G3GatewayPSKProviderFactory.getInstance().getPSKProvider(getDeviceIdentifier(), getContext());
    }

    private MeterProtocolEvent getMeterProtocolEvent() {
        return collectedLogBook.getCollectedMeterEvents().get(0);
    }

    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new EventPushNotificationParser(comChannel, getContext(), collectedDataFactory);
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
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedDatas = new ArrayList<>();
        collectedDatas.add(collectedLogBook);
        return collectedDatas;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-05-31 16:24:54 +0300 (Tue, 31 May 2016)$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        //No properties
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }
}