package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.UnableToCreateConnectionType;
import com.energyict.protocols.mdc.ConnectionTypeRule;
import com.energyict.protocols.mdc.protocoltasks.ServerConnectionType;
import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collection;

/**
 * Provides an implementation for the {@link ConnectionTypeService} interface
 * and registers as a OSGi component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/11/13
 * Time: 16:27
 */
@Component(name = "com.energyict.protocols.mdc.services.connectiontypeservice", service = ConnectionTypeService.class, immediate = true)
public class ConnectionTypeServiceImpl implements ConnectionTypeService {

    private volatile PropertySpecService propertySpecService;

    @Inject
    public ConnectionTypeServiceImpl(PropertySpecService propertySpecService) {
        super();
        this.setPropertySpecService(propertySpecService);
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public ConnectionType createConnectionType(String javaClassName) {
        try {
            ServerConnectionType connectionType = (ServerConnectionType) (getClass().getClassLoader().loadClass(javaClassName)).newInstance();
            connectionType.setPropertySpecService(this.propertySpecService);
            return connectionType;
        }
        catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new UnableToCreateConnectionType(e, javaClassName);
        }
    }

    @Override
    public Collection<PluggableClassDefinition> getExistingConnectionTypePluggableClasses() {
        return Arrays.asList((PluggableClassDefinition[]) ConnectionTypeRule.values());
    }

}