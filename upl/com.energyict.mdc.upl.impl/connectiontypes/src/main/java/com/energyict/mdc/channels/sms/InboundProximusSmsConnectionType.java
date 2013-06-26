package com.energyict.mdc.channels.sms;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the {@link com.energyict.mdc.tasks.ConnectionType} interface specific for outbound SMS communication using Proximus as carrier.
 *
 * @author sva
 * @since 19/06/13 - 9:12
 */
public class InboundProximusSmsConnectionType extends ConnectionTypeImpl {

    public static final String DEVICE_PHONE_NUMBER_PROPERTY_NAME = "phoneNumber";

    private PropertySpec phoneNumberPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(DEVICE_PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.TCP, ComPortType.UDP);
    }

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case DEVICE_PHONE_NUMBER_PROPERTY_NAME:
                return this.phoneNumberPropertySpec();
            default:
                return null;
        }
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return DEVICE_PHONE_NUMBER_PROPERTY_NAME.equals(name);
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Arrays.asList(phoneNumberPropertySpec());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>();
    }
}
