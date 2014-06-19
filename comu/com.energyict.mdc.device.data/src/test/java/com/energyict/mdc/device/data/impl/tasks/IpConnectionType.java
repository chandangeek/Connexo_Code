package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
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
 * Models a {@link ConnectionType} for TCP/IP taht does not support
 * multiple connections and that is designed for unit testing purposes only.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-17 (11:19)
 */
public class IpConnectionType implements ServerConnectionType {

    public static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";
    public static final String PORT_PROPERTY_NAME = "port";
    private static final int HASH_CODE = 35809; // Random prime number

    private PropertySpecService propertySpecService;

    public IpConnectionType () {
        super();
    }

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public boolean allowsSimultaneousConnections () {
        return true;
    }

    @Override
    public boolean supportsComWindow () {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes () {
        return EnumSet.of(ComPortType.TCP, ComPortType.UDP);
    }

    private PropertySpec ipAddressPropertySpec () {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(IP_ADDRESS_PROPERTY_NAME);
    }

    private PropertySpec portNumberPropertySpec () {
        return OptionalPropertySpecFactory.newInstance().bigDecimalPropertySpec(PORT_PROPERTY_NAME);
    }

     @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(this.ipAddressPropertySpec(), this.portNumberPropertySpec());
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        switch (name) {
            case IP_ADDRESS_PROPERTY_NAME:
                return this.ipAddressPropertySpec();
            case PORT_PROPERTY_NAME:
                return this.portNumberPropertySpec();
            default:
                return null;
        }
    }

    @Override
    public String getVersion () {
        return "For Unit Testing purposes only";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        // Do not need this as it is for unit testing purposes only
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        return null;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

    @Override
    public int hashCode () {
        return HASH_CODE;
    }

    @Override
    public boolean equals (Object obj) {
        return obj instanceof IpConnectionType || super.equals(obj);
    }

}