package com.energyict.mdc.protocol.services;

import com.energyict.mdc.protocol.ConnectionType;
import com.energyict.mdc.protocol.PluggableClass;

/**
 * OSGI Service wrapper for a {@link ConnectionType}.
 *
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:01
 */
public interface ConnectionTypeService {

    public ConnectionType createConnectionType(PluggableClass pluggableClass);

}