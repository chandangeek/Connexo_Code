package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Does pretty much the same as the PushEventNotification of the G3 gateway,
 * but uses the Beacon3100 protocol to connect to the DC device.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 11:33
 */
public class Beacon3100PushEventNotification extends PushEventNotification {

    //TODO junit test with encrypted traces

    /**
     * The obiscode of the logbook to store the received events in
     * Note that this one (Beacon main logbook) is different from the G3 gateway main logbook.
     */
    private static final ObisCode OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    private static final String PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY = "ProvideProtocolJavaClassName";
    private final PropertySpecService propertySpecService;
    private boolean provideProtocolJavaClasName = true;

    public Beacon3100PushEventNotification(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected BeaconPSKProvider getPskProvider() {
        return BeaconPSKProviderFactory.getInstance(provideProtocolJavaClasName).getPSKProvider(getDeviceIdentifier(), getContext());
    }

    @Override
    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new EventPushNotificationParser(comChannel, getContext(), OBIS_STANDARD_EVENT_LOG);
        }
        return parser;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        List<com.energyict.mdc.upl.properties.PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(
                UPLPropertySpecFactory
                        .specBuilder(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, false, this.propertySpecService::booleanSpec)
                        .setDefaultValue(Boolean.TRUE)
                        .finish());
        return propertySpecs;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-04-25 11:28:57 +0200 (Mon, 25 Apr 2016)$";
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        super.setUPLProperties(properties);
        this.provideProtocolJavaClasName = properties.getTypedProperty(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, true);
    }

}