package com.energyict.protocols.mdc.channels.sms;

import com.energyict.mdc.protocol.ConnectionType;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;

import java.util.List;

/**
 * An implementation of the {@link ConnectionType} interface specific for inbound SMS communication using Proximus as carrier.
 *
 * @author sva
 * @since 19/06/13 - 9:12
 */
public class InboundProximusSmsConnectionType extends AbstractInboundSmsConnectionType {

    public static final String DEVICE_PHONE_NUMBER_PROPERTY_NAME = "phoneNumber";
    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";

    private PropertySpec phoneNumberPropertySpec() {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(DEVICE_PHONE_NUMBER_PROPERTY_NAME);
    }

    private PropertySpec callHomeIdPropertySpec() {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(CALL_HOME_ID_PROPERTY_NAME);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case DEVICE_PHONE_NUMBER_PROPERTY_NAME:
                return this.phoneNumberPropertySpec();
            case CALL_HOME_ID_PROPERTY_NAME:
                return this.callHomeIdPropertySpec();
            default:
                return null;
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-28 16:52:59 +0200 (Fre, 28 Jun 2013) $";
    }

    @Override
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        propertySpecs.add(this.phoneNumberPropertySpec());
        propertySpecs.add(this.callHomeIdPropertySpec());
    }

}