package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.ConnectionType;

import java.util.Collection;

/**
 * OSGI Service wrapper for a {@link ConnectionType}.
 * <p>
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:01
 */
public interface ConnectionTypeService {

    ConnectionType createConnectionType(String javaClassName);

    Collection<PluggableClassDefinition> getExistingConnectionTypePluggableClasses();

}