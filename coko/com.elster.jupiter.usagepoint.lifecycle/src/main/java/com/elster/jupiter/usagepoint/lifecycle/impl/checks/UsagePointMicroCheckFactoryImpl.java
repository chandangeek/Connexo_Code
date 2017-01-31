/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroCheckFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component(name = "UsagePointMicroCheckFactoryImpl",
        service = {UsagePointMicroCheckFactory.class},
        immediate = true)
public class UsagePointMicroCheckFactoryImpl implements UsagePointMicroCheckFactory {

    private DataModel dataModel;
    private Thesaurus thesaurus;

    private final Map<String, Class<? extends MicroCheck>> microCheckMapping = new HashMap<>();

    @SuppressWarnings("unused") // OSGI
    public UsagePointMicroCheckFactoryImpl() {
    }

    @Inject
    public UsagePointMicroCheckFactoryImpl(UpgradeService upgradeService,
                                           NlsService nlsService) {
        setUpgradeService(upgradeService);
        setNlsService(nlsService);
        activate();
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.dataModel = upgradeService.newNonOrmDataModel();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
        this.dataModel.register(getModule());
        addMicroCheckMappings();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        };
    }

    private void addMicroCheckMappings() {
        addMicroCheckMapping(MetrologyConfigurationIsDefinedCheck.class);
        addMicroCheckMapping(MeterRolesAreSpecifiedCheck.class);
    }

    private void addMicroCheckMapping(Class<? extends ExecutableMicroCheck> clazz) {
        this.microCheckMapping.put(clazz.getSimpleName(), clazz);
    }

    @Override
    public Optional<MicroCheck> from(String microCheckKey) {
        return Optional.ofNullable(this.microCheckMapping.get(microCheckKey))
                .map(this.dataModel::getInstance);
    }

    @Override
    public Set<MicroCheck> getAllChecks() {
        return this.microCheckMapping.values().stream()
                .map(this.dataModel::getInstance)
                .collect(Collectors.toSet());
    }
}
