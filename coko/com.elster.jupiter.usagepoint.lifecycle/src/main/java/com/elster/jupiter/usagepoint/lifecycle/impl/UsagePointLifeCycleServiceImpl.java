package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleBuilder;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.MicroActionTranslationKeys;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.MicroCheckTranslationKeys;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component(name = "UsagePointLifeCycleServiceImpl",
        service = {UsagePointLifeCycleService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true)
public class UsagePointLifeCycleServiceImpl implements UsagePointLifeCycleService, MessageSeedProvider, TranslationKeyProvider, UsagePointLifeCycleBuilder {
    private DataModel dataModel;
    private Thesaurus thesaurus;
    private UpgradeService upgradeService;
    private UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @SuppressWarnings("unused") // OSGI
    public UsagePointLifeCycleServiceImpl() {
    }

    @Inject
    public UsagePointLifeCycleServiceImpl(OrmService ormService,
                                          NlsService nlsService,
                                          UpgradeService upgradeService,
                                          UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        setOrmService(ormService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setUsagePointLifeCycleConfigurationService(usagePointLifeCycleConfigurationService);
        activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(UsagePointLifeCycleService.COMPONENT_NAME, "UsagePoint lifecycle");
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointLifeCycleService.COMPONENT_NAME, Layer.DOMAIN)
                .join(nlsService.getThesaurus(UsagePointLifeCycleConfigurationService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setUsagePointLifeCycleConfigurationService(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    @Activate
    public void activate() {
        this.dataModel.register(getModule());
        this.usagePointLifeCycleConfigurationService.addUsagePointLifeCycleBuilder(this);
        this.upgradeService.register(InstallIdentifier.identifier("Pulse", UsagePointLifeCycleService.COMPONENT_NAME), this.dataModel, Installer.class, Collections.emptyMap());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UsagePointLifeCycleConfigurationService.class).toInstance(usagePointLifeCycleConfigurationService);
                bind(UsagePointLifeCycleService.class).toInstance(UsagePointLifeCycleServiceImpl.this);
            }
        };
    }

    @Override
    public void triggerTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties) {
        this.triggerMicroChecks(usagePoint, transition, transitionTime, properties);
        this.triggerMicroActions(usagePoint, transition, transitionTime, properties);
        this.performTransition(usagePoint, transition, transitionTime, properties);
    }

    private void triggerMicroChecks(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties) {
        transition.getChecks().stream()
                .map(ExecutableMicroCheck.class::cast)
                .forEach(action -> action.execute(usagePoint, properties, transitionTime));
    }

    private void triggerMicroActions(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties) {
        transition.getActions().stream()
                .map(ExecutableMicroAction.class::cast)
                .forEach(action -> action.execute(usagePoint, properties, transitionTime));
    }

    public void performTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties) {
        transition.doTransition(usagePoint.getMRID(), UsagePoint.class.getName(), transitionTime, properties);
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
        keys.addAll(Arrays.asList(MicroCheckTranslationKeys.values()));
        keys.addAll(Arrays.asList(MicroActionTranslationKeys.values()));
        keys.addAll(Arrays.asList(MicroCategoryTranslationKeys.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public void accept(UsagePointLifeCycle usagePointLifeCycle) {
        UsagePointState underConstruction = usagePointLifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.UNDER_CONSTRUCTION)).findFirst().get();
        UsagePointState active = usagePointLifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.ACTIVE)).findFirst().get();
        UsagePointState inactive = usagePointLifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.INACTIVE)).findFirst().get();
        UsagePointState demolished = usagePointLifeCycle.getStates().stream().filter(state -> state.isDefault(DefaultState.DEMOLISHED)).findFirst().get();

        usagePointLifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_INSTALL_ACTIVE).format(), underConstruction, active)
                .complete();
        usagePointLifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_INSTALL_INACTIVE).format(), underConstruction, inactive)
                .complete();
        usagePointLifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_DEACTIVATE).format(), active, inactive)
                .complete();
        usagePointLifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_ACTIVATE).format(), inactive, active)
                .complete();
        usagePointLifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_DEMOLISH_FROM_ACTIVE).format(), active, demolished)
                .complete();
        usagePointLifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_DEMOLISH_FROM_INACTIVE).format(), inactive, demolished)
                .complete();
    }
}
