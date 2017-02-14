package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLConnectionTypeAdapter;
import com.energyict.protocols.impl.channels.ConnectionTypeRule;
import com.energyict.protocols.impl.channels.TranslationKeys;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
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

    private volatile CustomPropertySetInstantiatorService customPropertySetInstantiatorService;

    // Need default constructor for OSGi framework
    public ConnectionTypeServiceImpl() {
        super();
    }

    @Inject
    public ConnectionTypeServiceImpl(CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
        this();
        this.setCustomPropertySetInstantiatorService(customPropertySetInstantiatorService);
        this.activate();
    }

    //TODO fix this, used in JUnit tests
    public static ConnectionTypeServiceImpl withAllSerialComponentServices() {
        ConnectionTypeServiceImpl service = new ConnectionTypeServiceImpl();
        service.activate();
        return service;
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
    }

    @Reference
    public void setCustomPropertySetInstantiatorService(CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
        this.customPropertySetInstantiatorService = customPropertySetInstantiatorService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        // Just making sure that this bundle activates after the bundle that provides connections (see com.energyict.mdc.protocol.api.ConnectionProvider)
    }

    @Override
    public ConnectionType createConnectionType(String javaClassName) {
        com.energyict.mdc.upl.io.ConnectionType uplConnectionType = (com.energyict.mdc.upl.io.ConnectionType) uplFactories
                .computeIfAbsent(javaClassName, ConstructorBasedUplServiceInjection::from)
                .newInstance();

        return new UPLConnectionTypeAdapter(uplConnectionType, customPropertySetInstantiatorService);
    }

    @Override
    public Collection<PluggableClassDefinition> getExistingConnectionTypePluggableClasses() {
        return Arrays.asList((PluggableClassDefinition[]) ConnectionTypeRule.values());
    }
}