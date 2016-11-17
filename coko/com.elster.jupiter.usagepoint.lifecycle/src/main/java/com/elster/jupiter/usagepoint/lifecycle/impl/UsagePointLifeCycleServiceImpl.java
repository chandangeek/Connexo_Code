package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckException;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleBuilder;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.MicroActionTranslationKeys;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.MicroCheckTranslationKeys;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "UsagePointLifeCycleServiceImpl",
        service = {UsagePointLifeCycleService.class, ServerUsagePointLifeCycleService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true)
public class UsagePointLifeCycleServiceImpl implements ServerUsagePointLifeCycleService, MessageSeedProvider, TranslationKeyProvider, UsagePointLifeCycleBuilder {
    private DataModel dataModel;
    private Thesaurus thesaurus;
    private UpgradeService upgradeService;
    private UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private ThreadPrincipalService threadPrincipalService;
    private MeteringService meteringService; // table model ordering
    private Clock clock;

    @SuppressWarnings("unused") // OSGI
    public UsagePointLifeCycleServiceImpl() {
    }

    @Inject
    public UsagePointLifeCycleServiceImpl(OrmService ormService,
                                          NlsService nlsService,
                                          UpgradeService upgradeService,
                                          UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
                                          ThreadPrincipalService threadPrincipalService,
                                          MeteringService meteringService,
                                          Clock clock) {
        setOrmService(ormService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setUsagePointLifeCycleConfigurationService(usagePointLifeCycleConfigurationService);
        setThreadPrincipalService(threadPrincipalService);
        setMeteringService(meteringService);
        setClock(clock);
        activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(UsagePointLifeCycleService.COMPONENT_NAME, "UsagePoint lifecycle");
        Stream.of(TableSpecs.values()).forEach(table -> table.addTo(this.dataModel));
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

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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
                bind(ServerUsagePointLifeCycleService.class).toInstance(UsagePointLifeCycleServiceImpl.this);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
            }
        };
    }

    @Override
    public UsagePointStateChangeRequest performTransition(UsagePoint usagePoint, UsagePointTransition transition, String application, Map<String, Object> properties) {
        return this.dataModel.getInstance(UsagePointStateChangeRequestImpl.class)
                .init(usagePoint, transition, this.clock.instant(), application, properties)
                .execute();
    }

    @Override
    public UsagePointStateChangeRequest scheduleTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, String application, Map<String, Object> properties) {
        UsagePointStateChangeRequestImpl changeRequest = this.dataModel.getInstance(UsagePointStateChangeRequestImpl.class)
                .init(usagePoint, transition, transitionTime, application, properties);
        // TODO fetch the recurrent task, check its schedule and reschedule if needed
        return changeRequest;
    }

    @Override
    public List<UsagePointStateChangeRequest> getHistory(UsagePoint usagePoint) {
        return this.dataModel.query(UsagePointStateChangeRequest.class, UsagePointStateChangePropertyImpl.class)
                .select(where(UsagePointStateChangeRequestImpl.Fields.USAGE_POINT.fieldName()).isEqualTo(usagePoint),
                        Order.descending(UsagePointStateChangeRequestImpl.Fields.TRANSITION_TIME.fieldName()));
    }

    @Override
    public void triggerMicroChecks(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime) {
        List<ExecutableMicroCheckViolation> violations = transition.getChecks().stream()
                .map(ExecutableMicroCheck.class::cast)
                .map(check -> check.execute(usagePoint, transitionTime))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!violations.isEmpty()) {
            throw new ExecutableMicroCheckException(this.thesaurus, MessageSeeds.MICRO_CHECKS_FAILED, violations);
        }
    }

    @Override
    public void triggerMicroActions(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties) {
        transition.getActions().stream()
                .map(ExecutableMicroAction.class::cast)
                .forEach(action -> action.execute(usagePoint, transitionTime, properties));
    }

    @Override
    public void performTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime) {
        transition.doTransition(usagePoint.getMRID(), UsagePoint.class.getName(), transitionTime, Collections.emptyMap());
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
