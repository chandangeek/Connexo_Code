package com.energyict.protocols.impl.channels.sms;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;

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

    private final PropertySpecService propertySpecService;

    @Inject
    public InboundProximusSmsConnectionType(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec phoneNumberPropertySpec() {
        return this.propertySpecService.basicPropertySpec(DeviceProtocolProperty.phoneNumber.name(), true, new StringFactory());
    }

    private PropertySpec callHomeIdPropertySpec() {
        return this.propertySpecService.basicPropertySpec(DeviceProtocolProperty.callHomeId.name(), true, new StringFactory());
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for InboundProximusSmsConnectionType
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