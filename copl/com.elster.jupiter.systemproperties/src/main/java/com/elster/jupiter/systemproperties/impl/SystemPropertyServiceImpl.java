package com.elster.jupiter.systemproperties.impl;


import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.systemproperties.SystemProperty;
import com.elster.jupiter.systemproperties.SystemPropertyService;
import com.elster.jupiter.systemproperties.SystemPropertySpec;
import com.elster.jupiter.systemproperties.impl.properties.CacheIsEnabledSystemPropertySpec;
import com.elster.jupiter.systemproperties.impl.properties.EvictionTimeSystemPropertySpec;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;

import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;


@Component(name = "com.elster.jupiter.systemproperties.service",
        service = {SystemPropertyService.class},
        property = "name=" + SystemPropertyService.COMPONENT_NAME,
        immediate = true)
public class SystemPropertyServiceImpl implements SystemPropertyService, TranslationKeyProvider {

    private volatile Thesaurus thesaurus;
    private volatile OrmService ormService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile UserService userService;
    private Map<String, SystemPropertySpec> specs = new HashMap<>();
    private Map<String, String> props = new ConcurrentHashMap<>();
    private volatile SystemPropertyLoop sysLoop;


    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
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
        dataModel = ormService.newDataModel(COMPONENT_NAME, "System Property Name");

        for (SystemPropsTableSpecs spec : SystemPropsTableSpecs.values()) {
            spec.addTo(dataModel);
        }

        dataModel.register(getModule());
        upgradeService.register(identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
        initSystemPropertySpecs();
        initSystemProperties();
        sysLoop = new SystemPropertyLoop(this);
        sysLoop.run();
    }

    @Deactivate
    public void deactivate() {
        sysLoop.shutDown();
    }

    private void initSystemPropertySpecs() {
        specs.put(SystemPropertyTranslationKeys.ENABLE_CACHE.getKey(),
                new CacheIsEnabledSystemPropertySpec(ormService, propertyValueInfoService, propertySpecService, thesaurus));
        specs.put(SystemPropertyTranslationKeys.EVICTION_TIME.getKey(),
                new EvictionTimeSystemPropertySpec(ormService, propertyValueInfoService, propertySpecService, thesaurus));
    }

    private void initSystemProperties() {
        props = getAllSystemProperties().stream().collect(
                Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    @Override
    public Optional<SystemPropertySpec> getPropertySpec(String key) {
        return Optional.ofNullable(specs.get(key));
    }

    @Override
    public List<SystemProperty> getAllSystemProperties() {
        List<SystemProperty> lst = dataModel.mapper(SystemProperty.class).find();
        return lst;
    }

    @Override
    public Optional<SystemProperty> getSystemPropertiesByKey(String key) {

        Optional<SystemProperty> property = dataModel.mapper(SystemProperty.class)
                .getUnique("key", key);

        return property;
    }

    ;

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
            }
        };
    }


    public synchronized void readAndCheckProperties() {
        List<SystemProperty> newprops = getAllSystemProperties();
        for (SystemProperty prop : newprops) {
            if (!props.put(prop.getKey(), prop.getValue()).equals(prop.getValue())) {
                specs.get(prop.getKey()).actionOnChange(prop);
            }
        }
    }

    @Override
    public synchronized void actionOnPropertyChange(SystemProperty systemProperty, SystemPropertySpec spec) {
        systemProperty.update();
        props.put(systemProperty.getKey(), systemProperty.getValue());
        spec.actionOnChange(systemProperty);
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
}


