package com.elster.jupiter.systemproperties.impl;


import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.systemproperties.security.Privileges;
import com.elster.jupiter.systemproperties.SystemProperty;
import com.elster.jupiter.systemproperties.SystemPropertyService;
import com.elster.jupiter.systemproperties.impl.properties.CacheIsEnabledSystemPropertySpec;
import com.elster.jupiter.systemproperties.impl.properties.EvictionTimeSystemPropertySpec;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;


@Component(name = "com.elster.jupiter.systemproperties.service",
        service = {SystemPropertyService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = "name=" + SystemPropertyService.COMPONENT_NAME,
        immediate = true)
public class SystemPropertyServiceImpl implements SystemPropertyService, TranslationKeyProvider, MessageSeedProvider {

    private volatile Thesaurus thesaurus;
    private volatile OrmService ormService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile UserService userService;
    private final Map<String, SystemPropertySpec> specs = new ConcurrentHashMap<>();
    private final Map<String, Object> props = new ConcurrentHashMap<>();
    private volatile SystemPropertyChangeHandlerLoop sysLoop;


    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }


    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Activate
    public void activate() {
        dataModel = ormService.newDataModel(COMPONENT_NAME, "System Property");

        for (SystemPropsTableSpecs spec : SystemPropsTableSpecs.values()) {
            spec.addTo(dataModel);
        }

        dataModel.register(getModule());
        upgradeService.register(identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
        initSystemPropertySpecs();
        initSystemProperties();
        sysLoop = new SystemPropertyChangeHandlerLoop(this);
        sysLoop.run();
    }

    @Deactivate
    public void deactivate() {
        sysLoop.shutDown();
        specs.clear();
        props.clear();
    }

    private void initSystemPropertySpecs() {
        specs.put(SystemPropertyTranslationKeys.ENABLE_CACHE.getKey(),
                dataModel.getInstance(CacheIsEnabledSystemPropertySpec.class));
        specs.put(SystemPropertyTranslationKeys.EVICTION_TIME.getKey(),
                dataModel.getInstance(EvictionTimeSystemPropertySpec.class));
    }

    private void initSystemProperties() {
        getAllSystemProperties().stream().forEach(x -> props.put(x.getKey(), x.getValueObject()));
    }

    @Override
    public PropertySpec findPropertySpec(String key) {
        return specs.get(key).getPropertySpec();
    }

    @Override
    public Object getPropertyValue(String key) {
        Object value = props.get(key);
        if (value == null) {
            value = specs.get(key).getPropertySpec().getPossibleValues().getDefault();
        }
        return value;
    }

    @Override
    public List<SystemProperty> getAllSystemProperties() {
        List<SystemProperty> lst = dataModel.mapper(SystemProperty.class).find();
        return lst;
    }

    @Override
    public Optional<SystemProperty> findSystemPropertyByKey(String key) {
        Optional<SystemProperty> property = dataModel.mapper(SystemProperty.class)
                .getOptional(key);
        return property;
    }


    @Override
    public void setPropertyValue(String key, Object newValue){
        SystemProperty sysprop = findSystemPropertyByKey(key).get();
        //Update system property if value changed.
        if (!newValue.equals(sysprop.getValueObject())) {
            String newValueStr = sysprop.getPropertySpec().getValueFactory().toStringValue(newValue);
            sysprop.setValue(newValueStr);
            actionOnPropertyChange(sysprop);
        }
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(SystemPropertyService.class).toInstance(SystemPropertyServiceImpl.this);
            }
        };
    }

    public synchronized void readAndProcessUpdatedProperties() {
        List<SystemProperty> newprops = getAllSystemProperties();
        for (SystemProperty prop : newprops) {
            if (!props.put(prop.getKey(), prop.getValueObject()).equals(prop.getValueObject())) {
                specs.get(prop.getKey()).actionOnChange(prop);
            }
        }
    }

    public synchronized void actionOnPropertyChange(SystemProperty systemProperty) {
        systemProperty.update();
        props.put(systemProperty.getKey(), systemProperty.getValueObject());
        specs.get(systemProperty.getKey()).actionOnChange(systemProperty);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Stream.of(Privileges.values()).forEach(keys::add);
        Stream.of(SystemPropertyTranslationKeys.values()).forEach(keys::add);
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}


