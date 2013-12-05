package com.energyict.protocols.mdc.services.impl;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.PluggableClass;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import org.osgi.service.component.annotations.Component;

/**
 * Copyrights EnergyICT
 * Date: 28/11/13
 * Time: 16:27
 */
@Component(name = "com.energyict.protocols.mdc.services.connectiontypeservice", service = ConnectionTypeService.class, immediate = true)
public class ConnectionTypeServiceImpl extends AbstractPluggableClassServiceImpl implements ConnectionTypeService {

    @Override
    public ConnectionType createConnectionType(PluggableClass pluggableClass) {
        try {
            return (ConnectionType) (Class.forName(pluggableClass.getJavaClassName())).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, pluggableClass.getJavaClassName());
        }
    }
}
