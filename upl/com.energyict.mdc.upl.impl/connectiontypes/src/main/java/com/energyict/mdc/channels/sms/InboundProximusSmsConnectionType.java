package com.energyict.mdc.channels.sms;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of the {@link com.energyict.mdc.tasks.ConnectionType} interface specific for inbound SMS communication using Proximus as carrier.
 *
 * @author sva
 * @since 19/06/13 - 9:12
 */
public class InboundProximusSmsConnectionType extends AbstractInboundSmsConnectionType {

    public static final String DEVICE_PHONE_NUMBER_PROPERTY_NAME = "phoneNumber";
    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";

    private PropertySpec phoneNumberPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(DEVICE_PHONE_NUMBER_PROPERTY_NAME);
    }

    private PropertySpec callHomeIdPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(CALL_HOME_ID_PROPERTY_NAME);
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
    public boolean isRequiredProperty(String name) {
        return DEVICE_PHONE_NUMBER_PROPERTY_NAME.equals(name) ||
                CALL_HOME_ID_PROPERTY_NAME.equals(name);
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Arrays.asList(phoneNumberPropertySpec(), callHomeIdPropertySpec());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>();
    }
}
