package com.energyict.protocols.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.LibraryType;
import com.energyict.mdc.io.ModemType;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.UnableToCreateConnectionType;

import com.energyict.protocols.mdc.ConnectionTypeRule;
import com.energyict.protocols.mdc.protocoltasks.ServerConnectionType;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.name.Names;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public static final String RXTX_AT_GUICE_INJECTION_NAME = "rxtx-at";
    public static final String SERIAL_AT_GUICE_INJECTION_NAME = "serial-at";
    public static final String SERIAL_CASE_GUICE_INJECTION_NAME = "serial-case";
    public static final String SERIAL_PAKNET_GUICE_INJECTION_NAME = "serial-paknet";
    public static final String SERIAL_PEMP_GUICE_INJECTION_NAME = "serial-pemp";

    private volatile PropertySpecService propertySpecService;
    private volatile Map<String, SerialComponentService> serialComponentServices = new HashMap<>();
    private Injector injector;

    // Need default constructor for OSGi framework
    public ConnectionTypeServiceImpl() {
        super();
    }

    @Inject
    public ConnectionTypeServiceImpl(PropertySpecService propertySpecService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.activate();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                this.bind(PropertySpecService.class).toInstance(propertySpecService);
                serialComponentServices
                    .forEach((k, v) -> this
                            .bind(SerialComponentService.class)
                            .annotatedWith(Names.named(k))
                            .toInstance(v));
            }
        };
    }

    @Activate
    public void activate() {
        this.injector = Guice.createInjector(this.getModule());
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference(target = "(&(library=" + LibraryType.Target.RXTX + ")(modem-type=" + ModemType.Target.AT + "))")
    public void setRxTxAtComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(RXTX_AT_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.AT + "))")
    public void setSioAtComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(SERIAL_AT_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.CASE + "))")
    public void setSioCaseComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(SERIAL_CASE_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.PAKNET + "))")
    public void setSioPaknetComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(SERIAL_PAKNET_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.PEMP + "))")
    public void setSioPempComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(SERIAL_PEMP_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Override
    public ConnectionType createConnectionType(String javaClassName) {
        try {
            // Attempt to load the class to verify that this class is managed by this bundle
            Class<?> connectionTypeClass = getClass().getClassLoader().loadClass(javaClassName);
            ServerConnectionType connectionType = (ServerConnectionType) this.injector.getInstance(connectionTypeClass);
            connectionType.setPropertySpecService(this.propertySpecService);
            return connectionType;
        }
        catch (ClassNotFoundException | ConfigurationException | ProvisionException e) {
            throw new UnableToCreateConnectionType(e, javaClassName);
        }
    }

    @Override
    public Collection<PluggableClassDefinition> getExistingConnectionTypePluggableClasses() {
        return Arrays.asList((PluggableClassDefinition[]) ConnectionTypeRule.values());
    }

}