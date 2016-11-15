package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.obis.ObisCode;

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
    protected static final ObisCode OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    protected static final String PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY = "ProvideProtocolJavaClassName";
    protected boolean provideProtocolJavaClasName = true;

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
    public List<PropertySpec> getOptionalProperties() {
        final List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(PropertySpecFactory.notNullableBooleanPropertySpec(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, true));
        return optionalProperties;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-11-15 18:03:36 +0100 (Tue, 15 Nov 2016)$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        super.addProperties(properties);
        this.provideProtocolJavaClasName = properties.<Boolean>getTypedProperty(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, true);
    }
}