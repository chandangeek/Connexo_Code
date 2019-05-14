/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.demo.impl.lifecycle.device.microchecks;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.demo.impl.lifecycle.device.microchecks.CustomDeviceMicroCheckFactoryImpl",
        service = {DeviceMicroCheckFactory.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true)
public class CustomDeviceMicroCheckFactoryImpl implements DeviceMicroCheckFactory, TranslationKeyProvider, MessageSeedProvider {
    static final String COMPONENT_NAME = "DLD"; // for translations only
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile BpmService bpmService;
    private final Map<String, Class<? extends MicroCheck>> microCheckMapping = new HashMap<>();

    public CustomDeviceMicroCheckFactoryImpl() {
        // for OSGi purposes
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.SERVICE);
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        dataModel = upgradeService.newNonOrmDataModel();
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(BpmService.class).toInstance(bpmService);
            }
        });
        addMicroCheckMappings();
    }

    @Deactivate
    public void deactivate() {
        microCheckMapping.clear();
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                CustomMicroCategory.values(),
                CustomMicroCheck.values()
        )
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.SERVICE;
    }

    @Override
    public Optional<? extends MicroCheck> from(String microCheckKey) {
        return Optional.ofNullable(microCheckMapping.get(microCheckKey))
                .map(dataModel::getInstance);
    }

    @Override
    public Set<? extends MicroCheck> getAllChecks() {
        return microCheckMapping.values().stream()
                .map(dataModel::getInstance)
                .collect(Collectors.toSet());
    }

    private void addMicroCheckMappings() {
        addMicroCheckMapping(SkpProcessNotRunning.class);
    }

    private void addMicroCheckMapping(Class<? extends MicroCheck> clazz) {
        microCheckMapping.put(dataModel.getInstance(clazz).getKey(), clazz);
    }
}
