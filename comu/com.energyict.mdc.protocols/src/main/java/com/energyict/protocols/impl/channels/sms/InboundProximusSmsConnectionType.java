package com.energyict.protocols.impl.channels.sms;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import javax.inject.Inject;
import java.util.Arrays;
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

    private final PropertySpecService propertySpecService;

    @Inject
    public InboundProximusSmsConnectionType(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec phoneNumberPropertySpec() {
        return this.propertySpecService.basicPropertySpec(DEVICE_PHONE_NUMBER_PROPERTY_NAME, true, new StringFactory());
    }

    private PropertySpec callHomeIdPropertySpec() {
        return this.propertySpecService.basicPropertySpec(CALL_HOME_ID_PROPERTY_NAME, true, new StringFactory());
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for InboundProximusSmsConnectionType
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
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.phoneNumberPropertySpec(),
                this.callHomeIdPropertySpec());
    }

}