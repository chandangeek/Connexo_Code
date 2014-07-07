package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.mdc.protocoltasks.ServerConnectionType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Models a {@link ConnectionType} for modem based communication
 * that is designed for unit testing purposes only.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-05 (09:03)
 */
public class ModemConnectionType implements ServerConnectionType {

    public static final String PHONE_NUMBER_PROPERTY_NAME = "phoneNumber";
    private static final int HASH_CODE = 91153; // Random prime number

    public ModemConnectionType () {
        super();
    }

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {

    }

    @Override
    public boolean allowsSimultaneousConnections () {
        return false;
    }

    @Override
    public boolean supportsComWindow () {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes () {
        return EnumSet.of(ComPortType.SERIAL);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(this.phoneNumberPropertySpec());
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        if (PHONE_NUMBER_PROPERTY_NAME.equals(name)) {
            return this.phoneNumberPropertySpec();
        }
        else {
            return null;
        }
    }

    private PropertySpec phoneNumberPropertySpec () {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(PHONE_NUMBER_PROPERTY_NAME);
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        // Do not need this as it is for unit testing purposes only
    }

    @Override
    public String getVersion () {
        return "For Unit Testing purposes only";
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        return null;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }

    @Override
    public int hashCode () {
        return HASH_CODE;
    }

    @Override
    public boolean equals (Object obj) {
        return obj instanceof ModemConnectionType || super.equals(obj);
    }

}