package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exceptions.CommunicationException;

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
                pskProvider.providePSK(joiningMacAddress, getEventPushNotificationParser().getSecurityPropertySet());
            } catch (CommunicationException e) {
                context.logOnAllLoggerHandlers(e.getMessage(), Level.WARNING);
                pskProvider.provideError(joiningMacAddress);
            }
        }

        return DiscoverResultType.DATA;
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

    private boolean isJoinAttempt() {
        return getMeterProtocolEvent().getProtocolCode() == METER_JOIN_ATTEMPT;
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
            parser = new EventPushNotificationParser(comChannel, getContext());
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
        return "$Date: 2016-07-14 16:01:10 +0200 (Thu, 14 Jul 2016)$";
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