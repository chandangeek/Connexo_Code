package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Models a {@link com.energyict.mdc.protocol.api.ConnectionType} for TCP/IP that does not support
 * multiple connections and that is designed for unit testing purposes only.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-17 (11:19)
 */
public class IpConnectionType implements ConnectionType {

    private static final int HASH_CODE = 35809; // Random prime number

    private final PropertySpecService propertySpecService;

    @Inject
    public IpConnectionType(PropertySpecService propertySpecService) {
        super();
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

    @Override
    public Optional<CustomPropertySet<ConnectionType, ? extends PersistentDomainExtension<ConnectionType>>> getCustomPropertySet() {
        return Optional.of(new IpConnectionCustomPropertySet(this.propertySpecService));
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(this.ipAddressPropertySpec(), this.portNumberPropertySpec());
    }

    private PropertySpec ipAddressPropertySpec () {
        return IpConnectionProperties.IP_ADDRESS.propertySpec(this.propertySpecService);
    }

    private PropertySpec portNumberPropertySpec () {
        return IpConnectionProperties.PORT.propertySpec(this.propertySpecService);
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
    public Direction getDirection() {
        return Direction.OUTBOUND;
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