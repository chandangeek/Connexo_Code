package com.energyict.protocols.mdc.services.impl;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.PluggableClass;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.protocols.mdc.ConnectionTypeRule;
import java.util.Arrays;
import java.util.Collection;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link ConnectionTypeService} interface
 * and registers as a OSGi component.
 *
 * Copyrights EnergyICT
 * Date: 28/11/13
 * Time: 16:27
 */
@Component(name = "com.energyict.protocols.mdc.services.connectiontypeservice", service = ConnectionTypeService.class, immediate = true)
public class ConnectionTypeServiceImpl implements ConnectionTypeService {

    @Override
    public ConnectionType createConnectionType(PluggableClass pluggableClass) {
        return createConnectionType(pluggableClass.getJavaClassName());
    }

    @Override
    public ConnectionType createConnectionType(String javaClassName) {
        try {
            return (ConnectionType) (getClass().getClassLoader().loadClass(javaClassName)).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, javaClassName);
        }
    }

    @Override
    public Collection<PluggableClassDefinition> getExistingConnectionTypePluggableClasses() {
        return Arrays.asList((PluggableClassDefinition[])ConnectionTypeRule.values());
   }

}