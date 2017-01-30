package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocols.impl.channels.ConnectionTypeRule;
import com.energyict.protocols.impl.channels.TranslationKeys;
import com.energyict.protocols.mdc.adapter.UPLConnectionTypeAdapter;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an implementation for the {@link ConnectionTypeService} interface
 * and registers as a OSGi component.
 * <p>
 * Copyrights EnergyICT
 * Date: 28/11/13
 * Time: 16:27
 */
@Component(name = "com.energyict.protocols.mdc.services.connectiontypeservice", service = {ConnectionTypeService.class, TranslationKeyProvider.class}, immediate = true)
public class ConnectionTypeServiceImpl implements ConnectionTypeService, TranslationKeyProvider {

    private static final Map<String, InstanceFactory> uplFactories = new ConcurrentHashMap<>();

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    private Injector injector;

    // Need default constructor for OSGi framework
    public ConnectionTypeServiceImpl() {
        super();
    }

    @Inject
    public ConnectionTypeServiceImpl(PropertySpecService propertySpecService, NlsService nlsService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setNlsService(nlsService);
        this.activate();
    }

    public static ConnectionTypeServiceImpl withAllSerialComponentServices(
            PropertySpecService propertySpecService,
            NlsService nlsService) {
        ConnectionTypeServiceImpl service = new ConnectionTypeServiceImpl();
        service.setPropertySpecService(propertySpecService);
        service.setNlsService(nlsService);
        service.activate();
        return service;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                this.bind(PropertySpecService.class).toInstance(propertySpecService);
                this.bind(Thesaurus.class).toInstance(thesaurus);
                this.bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        };
    }

    @Override
    public String getComponentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Collections.addAll(keys, TranslationKeys.values());
        Collections.addAll(keys, ConnectionTypePropertySpecName.values());
        return keys;
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

    @Reference
    @SuppressWarnings("unused")
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        // Just making sure that this bundle activates after the bundle that provides connections (see com.energyict.mdc.protocol.api.ConnectionProvider)
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public ConnectionType createConnectionType(String javaClassName) {
        com.energyict.mdc.upl.io.ConnectionType uplConnectionType = (com.energyict.mdc.upl.io.ConnectionType) uplFactories
                .computeIfAbsent(javaClassName, ConstructorBasedUplServiceInjection::from)
                .newInstance();

        return new UPLConnectionTypeAdapter(uplConnectionType, injector);
    }

    @Override
    public Collection<PluggableClassDefinition> getExistingConnectionTypePluggableClasses() {
        return Arrays.asList((PluggableClassDefinition[]) ConnectionTypeRule.values());
    }
}