package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocol.exceptions.ConnectionException;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Models a {@link ConnectionType} for modem based communication
 * that is designed for unit testing purposes only.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-05 (09:03)
 */
public class ModemConnectionType implements ConnectionType {

    private static final int HASH_CODE = 91153; // Random prime number

    private final PropertySpecService propertySpecService;

    @Inject
    public ModemConnectionType(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
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
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.of(new ModemConnectionCustomPropertySet(this.propertySpecService));
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
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
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