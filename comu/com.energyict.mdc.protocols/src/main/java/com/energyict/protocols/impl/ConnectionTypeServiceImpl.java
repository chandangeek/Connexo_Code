package com.energyict.protocols.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.LibraryType;
import com.energyict.mdc.io.ModemType;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.UnableToCreateConnectionType;
import com.energyict.protocols.impl.channels.ConnectionTypeRule;
import com.energyict.protocols.impl.channels.ServerConnectionType;

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
import javax.validation.MessageInterpolator;
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

    public static final String RXTX_PLAIN_GUICE_INJECTION_NAME = "rxtx-none";
    public static final String RXTX_AT_GUICE_INJECTION_NAME = "rxtx-at";
    public static final String SERIAL_PLAIN_GUICE_INJECTION_NAME = "serialio-none";
    public static final String SERIAL_AT_GUICE_INJECTION_NAME = "serialio-at";
    public static final String SERIAL_CASE_GUICE_INJECTION_NAME = "serialio-case";
    public static final String SERIAL_PAKNET_GUICE_INJECTION_NAME = "serialio-paknet";
    public static final String SERIAL_PEMP_GUICE_INJECTION_NAME = "serialio-pemp";

    private volatile com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    private volatile PropertySpecService propertySpecService;
    private volatile SocketService socketService;
    private volatile TransactionService transactionService;
    private volatile Map<String, SerialComponentService> serialComponentServices = new HashMap<>();
    private Injector injector;
    private volatile Thesaurus thesaurus;

    // Need default constructor for OSGi framework
    public ConnectionTypeServiceImpl() {
        super();
    }

    @Inject
    public ConnectionTypeServiceImpl(com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService, PropertySpecService propertySpecService, SocketService socketService, NlsService nlsService, TransactionService transactionService) {
        this();
        this.setJupiterPropertySpecService(jupiterPropertySpecService);
        this.setPropertySpecService(propertySpecService);
        this.setSocketService(socketService);
        this.setNlsService(nlsService);
        this.setTransactionService(transactionService);
        this.activate();
    }

    public static ConnectionTypeServiceImpl withAllSerialComponentServices(
            com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService,
            PropertySpecService propertySpecService,
            SocketService socketService,
            NlsService nlsService,
            TransactionService transactionService,
            SerialComponentService serialComponentService) {
        ConnectionTypeServiceImpl service = new ConnectionTypeServiceImpl();
        service.setJupiterPropertySpecService(jupiterPropertySpecService);
        service.setPropertySpecService(propertySpecService);
        service.setSocketService(socketService);
        service.setNlsService(nlsService);
        service.setTransactionService(transactionService);
        service.setRxTxPlainComponentService(serialComponentService);
        service.setRxTxAtComponentService(serialComponentService);
        service.setSioPlainComponentService(serialComponentService);
        service.setSioAtComponentService(serialComponentService);
        service.setSioCaseComponentService(serialComponentService);
        service.setSioPaknetComponentService(serialComponentService);
        service.setSioPempComponentService(serialComponentService);
        service.activate();
        return service;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                this.bind(com.elster.jupiter.properties.PropertySpecService.class).toInstance(jupiterPropertySpecService);
                this.bind(PropertySpecService.class).toInstance(propertySpecService);
                this.bind(SocketService.class).toInstance(socketService);
                this.bind(Thesaurus.class).toInstance(thesaurus);
                this.bind(MessageInterpolator.class).toInstance(thesaurus);
                this.bind(TransactionService.class).toInstance(transactionService);
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
    @SuppressWarnings("unused")
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        // Just making sure that this bundle activates after the bundle that provides connections (see com.energyict.mdc.protocol.api.ConnectionProvider)
    }

    @Reference
    public void setJupiterPropertySpecService(com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService) {
        this.jupiterPropertySpecService = jupiterPropertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setTransactionService(TransactionService transationService) {
        this.transactionService = transationService;
    }

    @Reference(target = "(&(library=" + LibraryType.Target.RXTX + ")(modem-type=" + ModemType.Target.NONE + "))")
    @SuppressWarnings("unused")
    public void setRxTxPlainComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(RXTX_PLAIN_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.RXTX + ")(modem-type=" + ModemType.Target.AT + "))")
    @SuppressWarnings("unused")
    public void setRxTxAtComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(RXTX_AT_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.NONE + "))")
    @SuppressWarnings("unused")
    public void setSioPlainComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(SERIAL_PLAIN_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.AT + "))")
    @SuppressWarnings("unused")
    public void setSioAtComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(SERIAL_AT_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.CASE + "))")
    @SuppressWarnings("unused")
    public void setSioCaseComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(SERIAL_CASE_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.PAKNET + "))")
    @SuppressWarnings("unused")
    public void setSioPaknetComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(SERIAL_PAKNET_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Reference(target = "(&(library=" + LibraryType.Target.SERIALIO + ")(modem-type=" + ModemType.Target.PEMP + "))")
    @SuppressWarnings("unused")
    public void setSioPempComponentService(SerialComponentService serialComponentService) {
        this.serialComponentServices.put(SERIAL_PEMP_GUICE_INJECTION_NAME, serialComponentService);
    }

    @Override
    public ConnectionType createConnectionType(String javaClassName) {
        try {
            // Attempt to load the class to verify that this class is managed by this bundle
            Class<?> connectionTypeClass = getClass().getClassLoader().loadClass(javaClassName);
            return (ServerConnectionType) this.injector.getInstance(connectionTypeClass);
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