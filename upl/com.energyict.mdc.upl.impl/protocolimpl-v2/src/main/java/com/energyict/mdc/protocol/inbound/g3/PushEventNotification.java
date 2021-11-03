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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/03/2015 - 10:27
 */
public class PushEventNotification implements BinaryInboundDeviceProtocol {

    private static final int METER_JOIN_ATTEMPT = 0xC50000;

    protected Logger logger = Logger.getLogger(this.getClass().getName());

    protected InboundDiscoveryContext context;
    protected ComChannel comChannel;
    protected CollectedLogBook collectedLogBook;
    protected EventPushNotificationParser parser;
    private List<CollectedDeviceInfo> collectedDeviceInfoList;
    private List<CollectedData> collectedDataList;

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
            try {
                if (!pskProvider.isInUse()) {
                    doProvide(pskProvider);
                } else {
                    logger.warning(getLoggingPrefix()+"provider in use");
                }
            } catch (Exception e) {
                pskProvider.provideError(e.getMessage(), context);
            } finally {
                getCollectedData().addAll(pskProvider.getCollectedDataList());
            }
        }
        return DiscoverResultType.DATA;
    }

    private void doProvide(G3GatewayPSKProvider pskProvider) throws CommunicationException {
        DeviceProtocolSecurityPropertySet securityPropertySet = getEventPushNotificationParser().getSecurityPropertySet();
        Boolean onHold = getEventPushNotificationParser().getInboundComTaskOnHold();
        if (onHold) {
            pskProvider.provideError(getErrorMessage(), context);
        } else {
            pskProvider.providePSK(securityPropertySet, context);
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

            if (collectedLogBook != null) {
                if (collectedLogBook.getCollectedMeterEvents() != null) {
                    logMessage.append("].  Message: '");
                    for (MeterProtocolEvent collectedEvent : collectedLogBook.getCollectedMeterEvents()) {
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
            long protocolCode = getMeterProtocolEvent().getProtocolCode();
            boolean isJoin = (protocolCode == METER_JOIN_ATTEMPT);
            logger.info(getLoggingPrefix()+"Event code "+protocolCode+" "+(isJoin?"is":"is not")+" a join request");
            return isJoin;
        } catch (Exception ex) {
            logger.log(Level.SEVERE,getLoggingPrefix()+"Cannot detect if is a join attempt: "+ex.getMessage(), ex);
            return false;

        }
    }

    private String getLoggingPrefix() {
        return "[Inbound]["+((getDeviceIdentifier()!=null)?getDeviceIdentifier().toString():"unknown")+"] ";
    }

    /**
     * Subclass for the Beacon implementation overrides this, it returns a specific PSK provider that is customized for the Beacon.
     */
    protected G3GatewayPSKProvider getPskProvider() {
        return G3GatewayPSKProviderFactory.getInstance().getPSKProvider(getDeviceIdentifier());
    }

    protected MeterProtocolEvent getMeterProtocolEvent() {
        if (collectedLogBook != null) {
            if (collectedLogBook.getCollectedMeterEvents().size() > 0) {
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
        // Nothing to do here
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return parser != null ? parser.getDeviceIdentifier() : null;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        if (collectedDataList == null) {
            collectedDataList = new ArrayList<>();
            collectedDataList.add(collectedLogBook);

            if (collectedDeviceInfoList != null && !collectedDeviceInfoList.isEmpty()) {
                collectedDataList.addAll(collectedDeviceInfoList);
            }
        }
        return collectedDataList;
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-09-21$";
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