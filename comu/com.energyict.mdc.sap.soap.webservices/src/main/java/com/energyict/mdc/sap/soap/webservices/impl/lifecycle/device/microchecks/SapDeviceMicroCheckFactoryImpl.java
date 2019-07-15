/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.lifecycle.device.microchecks;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.UpgradeService;
import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.lifecycle.device.microchecks.SapDeviceMicroCheckFactoryImpl",
        service = {DeviceMicroCheckFactory.class},
        immediate = true)
public class SapDeviceMicroCheckFactoryImpl implements DeviceMicroCheckFactory {
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile SAPCustomPropertySets sapCustomPropertySets;

    private final Map<String, Class<? extends MicroCheck>> microCheckMapping = new HashMap<>();

    public SapDeviceMicroCheckFactoryImpl() {
        // for OSGI purposes
    }

    @Inject
    public SapDeviceMicroCheckFactoryImpl(DataModel dataModel, Thesaurus thesaurus, SAPCustomPropertySets sapCustomPropertySets) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SERVICE);
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        dataModel = upgradeService.newNonOrmDataModel();
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
            }
        });
        addMicroCheckMappings();
    }

    @Deactivate
    public void deactivate() {
        microCheckMapping.clear();
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
        addMicroCheckMapping(LRNWasSet.class);
    }

    private void addMicroCheckMapping(Class<? extends MicroCheck> clazz) {
        microCheckMapping.put(dataModel.getInstance(clazz).getKey(), clazz);
    }
}
