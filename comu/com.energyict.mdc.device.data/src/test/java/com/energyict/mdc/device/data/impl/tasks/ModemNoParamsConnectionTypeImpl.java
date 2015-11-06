package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 7/4/14
 * Time: 2:56 PM
 */
public class ModemNoParamsConnectionTypeImpl implements ConnectionType {

    private static final int HASH_CODE = 91153; // Random prime number

    @Override
    public Optional<CustomPropertySet<ConnectionType, ? extends PersistentDomainExtension<ConnectionType>>> getCustomPropertySet() {
        return Optional.empty();
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
        return obj instanceof ModemNoParamsConnectionTypeImpl || super.equals(obj);
    }

}
