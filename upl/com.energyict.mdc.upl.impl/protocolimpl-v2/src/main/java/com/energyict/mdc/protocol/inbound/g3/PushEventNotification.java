package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.BinaryInboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exception.CommunicationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/03/2015 - 10:27
 */
public class PushEventNotification implements BinaryInboundDeviceProtocol {

    private static final int METER_JOIN_ATTEMPT = 0xC50000;

    protected InboundDiscoveryContext context;
    protected ComChannel comChannel;
    protected CollectedLogBook collectedLogBook;
    protected List<CollectedDeviceInfo> collectedDeviceInfoList;
    protected EventPushNotificationParser parser;
    List<CollectedData> collectedDatas;

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
        collectedDeviceInfoList = getEventPushNotificationParser().getCollectedDeviceInfoList();
        context.getLogger().info(this::getLoggingMessage);
        if (isJoinAttempt()) {
            G3GatewayPSKProvider pskProvider = getPskProvider();
            String joiningMacAddress = getMeterProtocolEvent().getMessage();
            pskProvider.addJoiningMacAddress(joiningMacAddress);
            try {
                doProvide(pskProvider, joiningMacAddress);
            } catch (CommunicationException e) {
                pskProvider.provideError(e.getMessage(), context);
            }
            getCollectedData().addAll(pskProvider.getCollectedDataList());
        }
        return DiscoverResultType.DATA;
    }

    protected void doProvide(G3GatewayPSKProvider pskProvider, String joiningMacAddress) throws CommunicationException {
        DeviceProtocolSecurityPropertySet securityPropertySet = getEventPushNotificationParser().getSecurityPropertySet();
        Boolean onHold = getEventPushNotificationParser().getInboundComTaskOnHold();
        if (onHold) {
            pskProvider.provideError(getErrorMessage(), context);
        } else {
            pskProvider.providePSK(joiningMacAddress, securityPropertySet, context);
        }
    }

    protected String getLoggingMessage() {
        StringBuilder logMessage = new StringBuilder();
        try {
            logMessage.append("Received inbound notification from [");
            if (getDeviceIdentifier() != null) {
                logMessage.append(getDeviceIdentifier().toString());
            } else {
                logMessage.append("unknown");
            }

            if (collectedLogBook!=null) {
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
            } else {
                logMessage.append("] - there are no events included.");
            }
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Subclass for the Beacon implementation overrides this, it returns a specific PSK provider that is customized for the Beacon.
     */
    protected G3GatewayPSKProvider getPskProvider() {
        return G3GatewayPSKProviderFactory.getInstance().getPSKProvider(getDeviceIdentifier());
    }

    protected MeterProtocolEvent getMeterProtocolEvent() {
        if (collectedLogBook!=null){
            if (collectedLogBook.getCollectedMeterEvents().size()>0) {
                return collectedLogBook.getCollectedMeterEvents().get(0);
            }
        }

        return null;
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
        if (collectedDatas == null) {
            collectedDatas = new ArrayList<>();
            collectedDatas.add(collectedLogBook);
        }
        if (collectedDeviceInfoList != null && !collectedDeviceInfoList.isEmpty()) {
            collectedDatas.addAll( collectedDeviceInfoList );
        }
        return collectedDatas;
    }

    @Override
    public String getVersion() {
        return "$Date: Fri Dec 30 14:28:09 2016 +0100 $";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) {
        //No properties
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }
}