package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.ConnectionTypePluggableClassDefinition;
import com.energyict.mdc.protocol.api.PluggableClass;
import java.util.Collection;

/**
 * OSGI Service wrapper for a {@link ConnectionType}.
 *
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:01
 */
public interface ConnectionTypeService {

    public ConnectionType createConnectionType(PluggableClass pluggableClass);

    public Collection<ConnectionTypePluggableClassDefinition> getExistingConnectionTypePluggableClasses();

}