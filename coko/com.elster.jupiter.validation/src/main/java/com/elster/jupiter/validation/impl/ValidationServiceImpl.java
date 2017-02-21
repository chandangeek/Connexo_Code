/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.DataValidationTaskStatus;
import com.elster.jupiter.validation.EventType;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.elster.jupiter.validation.impl.kpi.DataValidationKpiServiceImpl;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Operator.EQUAL;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.jupiter.validation",
        service = {ValidationService.class, ServerValidationService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = "name=" + ValidationService.COMPONENTNAME,
        immediate = true)
public class ValidationServiceImpl implements ServerValidationService, MessageSeedProvider, TranslationKeyProvider {

    static final String DESTINATION_NAME = "DataValidation";
    public static final String SUBSCRIBER_NAME = "DataValidation";
    public static final String VALIDATION_USER = "validation";
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile SearchService searchService;
    private volatile Clock clock;
    private volatile MessageService messageService;
    private volatile TaskService taskService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile UserService userService;
    private volatile UpgradeService upgradeService;

    private volatile KpiService kpiService;

    private final List<ValidatorFactory> validatorFactories = new CopyOnWriteArrayList<>();
    private final List<ValidationRuleSetResolver> ruleSetResolvers = new CopyOnWriteArrayList<>();
    private DestinationSpec destinationSpec;
    private DataValidationKpiService dataValidationKpiService;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    public ValidationServiceImpl() {
    }

    @Inject
    ValidationServiceImpl(BundleContext bundleContext, Clock clock, MessageService messageService, EventService eventService, TaskService taskService, MeteringService meteringService, MeteringGroupsService meteringGroupsService,
                          OrmService ormService, QueryService queryService, NlsService nlsService, UserService userService, Publisher publisher, UpgradeService upgradeService, KpiService kpiService, MetrologyConfigurationService metrologyConfigurationService, SearchService searchService) {
        this.clock = clock;
        this.messageService = messageService;
        this.setMetrologyConfigurationService(metrologyConfigurationService);
        this.setSearchService(searchService);
        setMessageService(messageService);
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.taskService = taskService;
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        this.setKpiService(kpiService);
        setUpgradeService(upgradeService);
        activate(bundleContext);

        // subscribe manually when not using OSGI
        ValidationEventHandler handler = new ValidationEventHandler();
        handler.setValidationService(this);
        publisher.addSubscriber(handler);
    }

    @Activate
    public final void activate(BundleContext context) {
        this.dataValidationKpiService = new DataValidationKpiServiceImpl(this);
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(EventService.class).toInstance(eventService);
                bind(TaskService.class).toInstance(taskService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(DataModel.class).toInstance(dataModel);
                bind(ValidationService.class).toInstance(ValidationServiceImpl.this);
                bind(ServerValidationService.class).toInstance(ValidationServiceImpl.this);
                bind(ValidationServiceImpl.class).toInstance(ValidationServiceImpl.this);
                bind(ValidatorCreator.class).toInstance(new DefaultValidatorCreator());
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(KpiService.class).toInstance(kpiService);
                bind(MessageService.class).toInstance(messageService);
                bind(DataValidationKpiService.class).toInstance(dataValidationKpiService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(DestinationSpec.class).toProvider(ValidationServiceImpl.this::getDestination);
                bind(MessageService.class).toInstance(messageService);
                bind(SearchService.class).toInstance(searchService);
                bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
            }
        });
        this.registerDataValidationKpiService(context);
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                dataModel,
                InstallerImpl.class,
                ImmutableMap.of(
                        Version.version(10, 2), UpgraderV10_2.class,
                        Version.version(10, 3), UpgraderV10_3.class
                ));
    }

    @Deactivate
    public void deactivate() {
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(ValidationService.COMPONENTNAME, "Validation");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setKpiService(KpiService kpiService) {
        this.kpiService = kpiService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ValidationService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public ValidationRuleSet createValidationRuleSet(String name, QualityCodeSystem qualityCodeSystem) {
        return createValidationRuleSet(name, qualityCodeSystem, null);
    }

    @Override
    public ValidationRuleSet createValidationRuleSet(String name, QualityCodeSystem qualityCodeSystem, String description) {
        ValidationRuleSet set = dataModel.getInstance(ValidationRuleSetImpl.class).init(name, qualityCodeSystem, description);
        set.save();
        return set;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    private DestinationSpec getDestination() {
        if (destinationSpec == null) {
            destinationSpec = messageService.getDestinationSpec(DESTINATION_NAME).orElse(null);
        }
        return destinationSpec;
    }

    @Override
    public void activateValidation(Meter meter) {
        Optional<MeterValidationImpl> meterValidation = getMeterValidation(meter);
        if (meterValidation.isPresent()) {
            if (!meterValidation.get().getActivationStatus()) {
                meterValidation.get().setActivationStatus(true);
                meterValidation.get().save();
                meter.getCurrentMeterActivation()
                        .map(MeterActivation::getChannelsContainer)
                        .map(channelsContainer -> updatedChannelsContainerValidationsFor(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDC), channelsContainer)))
                        .ifPresent(ChannelsContainerValidationList::activate);
            } // else already active
        } else {
            createMeterValidation(meter, true, false);
            meter.getCurrentMeterActivation()
                    .map(MeterActivation::getChannelsContainer)
                    .ifPresent(channelsContainer -> updatedChannelsContainerValidationsFor(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDC), channelsContainer)));
        }
    }

    @Override
    public void deactivateValidation(Meter meter) {
        getMeterValidation(meter)
                .filter(MeterValidationImpl::getActivationStatus)
                .ifPresent(meterValidation -> {
                            meterValidation.setActivationStatus(false);
                            meterValidation.save();
                        }
                );
    }

    @Override
    public void enableValidationOnStorage(Meter meter) {
        getMeterValidation(meter)
                .filter(meterValidation -> !meterValidation.getValidateOnStorage())
                .ifPresent(meterValidation -> {
                    meterValidation.setValidateOnStorage(true);
                    meterValidation.save();
                });
    }

    @Override
    public void disableValidationOnStorage(Meter meter) {
        getMeterValidation(meter)
                .filter(MeterValidationImpl::getValidateOnStorage)
                .ifPresent(meterValidation -> {
                    meterValidation.setValidateOnStorage(false);
                    meterValidation.save();
                });
    }

    @Override
    public boolean validationEnabled(Meter meter) {
        return getMeterValidation(meter).filter(MeterValidationImpl::getActivationStatus).isPresent();
    }

    @Override
    public boolean isValidationActive(ChannelsContainer channelsContainer) {
        return activeChannelsContainerValidationsFor(channelsContainer).isActive();
    }

    @Override
    public List<Meter> validationEnabledMetersIn(List<String> meterNames) {
        Condition isActive = ListOperator.IN.contains("meter.name", meterNames).and(where("isActive").isEqualTo(true));
        QueryExecutor<MeterValidationImpl> query = dataModel.query(MeterValidationImpl.class, EndDevice.class);
        return query.select(isActive).stream()
                .map(MeterValidationImpl::getMeter)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validationOnStorageEnabled(Meter meter) {
        return getMeterValidation(meter).filter(MeterValidationImpl::getValidateOnStorage).isPresent();
    }

    private Optional<MeterValidationImpl> getMeterValidation(Meter meter) {
        return dataModel.mapper(MeterValidationImpl.class).getOptional(meter.getId());
    }

    private void createMeterValidation(Meter meter, boolean active, boolean activeOnStorage) {
        MeterValidationImpl meterValidation = new MeterValidationImpl(dataModel).init(meter);
        meterValidation.setActivationStatus(active);
        meterValidation.setValidateOnStorage(active && activeOnStorage);
        meterValidation.save();
    }

    @Override
    public void updateLastChecked(ChannelsContainer channelsContainer, Instant date) {
        updatedChannelsContainerValidationsFor(new ValidationContextImpl(channelsContainer)).updateLastChecked(Objects.requireNonNull(date));
    }

    /**
     * Please consider {@link #moveLastCheckedBefore(Channel, Instant)} instead
     */
    @Deprecated
    @Override
    public void updateLastChecked(Channel channel, Instant date) {
        activeChannelsContainerValidationsFor(Objects.requireNonNull(channel).getChannelsContainer())
                .updateLastChecked(channel, Objects.requireNonNull(date));
    }

    @Override
    public void moveLastCheckedBefore(Channel channel, Instant date) {
        activeChannelsContainerValidationsFor(Objects.requireNonNull(channel).getChannelsContainer())
                .moveLastCheckedBefore(channel, Objects.requireNonNull(date));
    }

    @Override
    public boolean isValidationActive(Channel channel) {
        return activeChannelsContainerValidationsFor(Objects.requireNonNull(channel).getChannelsContainer()).isValidationActive(channel);
    }

    @Override
    public Optional<Instant> getLastChecked(ChannelsContainer channelsContainer) {
        return activeChannelsContainerValidationsFor(Objects.requireNonNull(channelsContainer)).getLastChecked();
    }

    @Override
    public Optional<Instant> getLastValidationRun(ChannelsContainer channelsContainer) {
        return activeChannelsContainerValidationsFor(Objects.requireNonNull(channelsContainer)).getLastValidationRun();
    }

    @Override
    public Optional<Instant> getLastChecked(Channel channel) {
        return activeChannelsContainerValidationsFor(Objects.requireNonNull(channel).getChannelsContainer()).getLastChecked(channel);
    }

    @Override
    public Optional<? extends ValidationRuleSet> getValidationRuleSet(long id) {
        return dataModel.mapper(IValidationRuleSet.class).getOptional(id);
    }

    @Override
    public Optional<? extends ValidationRuleSet> findAndLockValidationRuleSetByIdAndVersion(long id, long version) {
        return dataModel.mapper(IValidationRuleSet.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(String name) {
        Condition condition = where("name").isEqualTo(name).and(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull());
        return getRuleSetQuery().select(condition).stream().findFirst();
    }

    @Override
    public Query<ValidationRuleSet> getRuleSetQuery() {
        Query<ValidationRuleSet> ruleSetQuery = queryService.wrap(dataModel.query(ValidationRuleSet.class));
        ruleSetQuery.setRestriction(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull());
        return ruleSetQuery;
    }

    @Override
    public List<ValidationRuleSet> getValidationRuleSets() {
        return getRuleSetQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
    }

    @Override
    public void validate(Set<QualityCodeSystem> targetQualityCodeSystems, ChannelsContainer channelsContainer) {
        validate(new ValidationContextImpl(targetQualityCodeSystems, channelsContainer));
    }

    @Override
    public void validate(Set<QualityCodeSystem> targetQualityCodeSystems, ChannelsContainer channelsContainer, ReadingType readingType) {
        validate(new ValidationContextImpl(targetQualityCodeSystems, channelsContainer, readingType));
    }

    @Override
    public void validate(ValidationContext validationContext, Instant date) {
        ChannelsContainerValidationList container = updatedChannelsContainerValidationsFor(validationContext);
        container.moveLastCheckedBefore(date);
        container.validate();
    }

    public void validate(ValidationContext validationContext) {
        Set<QualityCodeSystem> allowedQualityCodeSystems = getQualityCodeSystemsWithAllowedValidation(validationContext);
        if (!allowedQualityCodeSystems.isEmpty()) {
            // Update the list of requested quality systems, because it is possible that some of them were restricted
            validationContext.setQualityCodeSystems(allowedQualityCodeSystems);
            if (validationContext.getReadingType().isPresent()) {
                validationContext.getChannelsContainer().getChannel(validationContext.getReadingType().get()).ifPresent(channel ->
                        updatedChannelsContainerValidationsFor(validationContext).validate(Collections.singleton(channel)));
            } else {
                updatedChannelsContainerValidationsFor(validationContext).validate();
            }
        }
    }

    @Override
    public void validate(Map<Channel, Range<Instant>> rangeByChannelMap) {
        rangeByChannelMap.entrySet()
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().getChannelsContainer(),
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .forEach(this::validate);
    }

    @Override
    public void validate(ChannelsContainer channelsContainer, Map<Channel, Range<Instant>> rangeByChannelMap) {
        if (!rangeByChannelMap.isEmpty()) {
            ChannelsContainerValidationList container = updatedChannelsContainerValidationsFor(new ValidationContextImpl(channelsContainer));
            container.moveLastCheckedBefore(rangeByChannelMap.entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue)));
            if (isValidationActiveOnStorage(channelsContainer)) {
                container.validate(rangeByChannelMap.keySet());
            } else {
                container.update();
                Map<Channel, Range<Instant>> actualScope = rangeByChannelMap.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> makeEndless(entry.getValue())));
                eventService.postEvent(EventType.VALIDATION_RESET.topic(), new ValidationScopeImpl(channelsContainer, actualScope));
            }
        }
    }

    private static Range<Instant> makeEndless(Range<Instant> range) {
        return range.hasLowerBound() ? Range.downTo(range.lowerEndpoint(), range.lowerBoundType()) : Range.all();
    }

    private Set<QualityCodeSystem> getQualityCodeSystemsWithAllowedValidation(ValidationContext validationContext) {
        Set<QualityCodeSystem> toValidate = validationContext.getQualityCodeSystems();
        if (toValidate == null || toValidate.isEmpty()) {
            toValidate = EnumSet.allOf(QualityCodeSystem.class);
        } else {
            toValidate = EnumSet.copyOf(toValidate); // make an editable copy
        }
        if (toValidate.contains(QualityCodeSystem.MDC) && !isValidationActiveOnMeter(validationContext.getMeter())) {
            toValidate.remove(QualityCodeSystem.MDC);
        }
        return toValidate;
    }

    private Boolean isValidationActiveOnMeter(Optional<Meter> meter) {
        return meter
                .flatMap(this::getMeterValidation)
                .map(MeterValidationImpl::getActivationStatus)
                .orElse(!meter.isPresent());
    }

    private boolean isValidationActiveOnStorage(ChannelsContainer channelsContainer) {
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            // no validation on storage for output channels yet;
            // once it is available, there's a need to reconsider case of
            // com.elster.jupiter.validation.impl.ValidationServiceImpl.validate(com.elster.jupiter.metering.ChannelsContainer,
            // java.util.Map<com.elster.jupiter.metering.Channel,com.google.common.collect.Range<java.time.Instant>>),
            // which is called by event of validation performed or reset, so performance problems up to infinite loop are probable.
            return false;
        } else {
            Optional<Meter> meter = channelsContainer.getMeter();
            return meter
                    .flatMap(this::getMeterValidation)
                    .filter(MeterValidationImpl::getActivationStatus) // validation should be active
                    .map(MeterValidationImpl::getValidateOnStorage)
                    .orElse(!meter.isPresent());
        }
    }

    List<ChannelsContainerValidation> getUpdatedChannelsContainerValidations(ValidationContext validationContext) {
        List<ValidationRuleSet> ruleSets = ruleSetResolvers.stream()
                .flatMap(r -> r.resolve(validationContext).stream())
                .filter(ruleSet -> validationContext.getQualityCodeSystems().isEmpty() || validationContext.getQualityCodeSystems().contains(ruleSet.getQualityCodeSystem()))
                .collect(Collectors.toList());
        List<ChannelsContainerValidation> persistedChannelsContainerValidations = getPersistedChannelsContainerValidations(validationContext.getChannelsContainer());
        List<ChannelsContainerValidation> returnList = ruleSets.stream()
                .map(ruleSet -> Pair.of(ruleSet, getForRuleSet(persistedChannelsContainerValidations, ruleSet)))
                .map(validationPair -> validationPair.getLast().orElseGet(() -> applyRuleSet(validationPair.getFirst(), validationContext.getChannelsContainer())))
                .collect(Collectors.toList());
        returnList
                .forEach(channelsContainerValidation -> channelsContainerValidation.getChannelsContainer().getChannels()
                        .stream()
                        .filter(channel -> !channelsContainerValidation.getRuleSet().getRules(channel.getReadingTypes()).isEmpty())
                        .filter(channel -> !channelsContainerValidation.getChannelValidation(channel).isPresent())
                        .forEach(channelsContainerValidation::addChannelValidation));
        persistedChannelsContainerValidations.stream()
                .filter(channelsContainerValidation -> {
                    ValidationRuleSet validationRuleSet = channelsContainerValidation.getRuleSet();
                    return !ruleSets.contains(validationRuleSet)
                            && (validationContext.getQualityCodeSystems().isEmpty() || validationContext.getQualityCodeSystems().contains(validationRuleSet.getQualityCodeSystem()))
                            && (validationRuleSet.getObsoleteDate() != null || !isValidationRuleSetInUse(validationRuleSet));
                })
                .forEach(ChannelsContainerValidation::makeObsolete);
        return returnList;
    }

    ChannelsContainerValidationList activeChannelsContainerValidationsFor(ChannelsContainer channelsContainer) {
        return dataModel.getInstance(ChannelsContainerValidationList.class).ofActivePersistedValidations(channelsContainer);
    }

    ChannelsContainerValidationList updatedChannelsContainerValidationsFor(ValidationContext validationContext) {
        return dataModel.getInstance(ChannelsContainerValidationList.class).ofUpdatedValidations(validationContext);
    }

    private Optional<ChannelsContainerValidation> getForRuleSet(List<ChannelsContainerValidation> channelsContainerValidations, ValidationRuleSet ruleSet) {
        return channelsContainerValidations.stream().filter(channelsContainerValidation -> ruleSet.equals(channelsContainerValidation.getRuleSet())).findFirst();
    }

    List<ChannelsContainerValidation> getPersistedChannelsContainerValidations(ChannelsContainer channelsContainer) {
        Condition condition = where("channelsContainer").isEqualTo(channelsContainer)
                .and(where("obsoleteTime").isNull());
        return dataModel.query(ChannelsContainerValidation.class, ChannelValidation.class).select(condition);
    }

    List<ChannelsContainerValidation> getActivePersistedChannelsContainerValidations(ChannelsContainer channelsContainer) {
        Condition condition = where("channelsContainer").isEqualTo(channelsContainer)
                .and(where(ValidationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull())
                .and(where("active").isEqualTo(true));
        return dataModel.query(ChannelsContainerValidation.class, ChannelValidation.class).select(condition);
    }

    private ChannelsContainerValidation applyRuleSet(ValidationRuleSet ruleSet, ChannelsContainer channelsContainer) {
        ChannelsContainerValidation channelsContainerValidation = dataModel.getInstance(ChannelsContainerValidationImpl.class).init(channelsContainer);
        channelsContainerValidation.setRuleSet(ruleSet);
        channelsContainer.getChannels().stream()
                .filter(c -> !ruleSet.getRules(c.getReadingTypes()).isEmpty())
                .forEach(channelsContainerValidation::addChannelValidation);
        channelsContainerValidation.save();
        return channelsContainerValidation;
    }

    List<? extends ChannelsContainerValidation> getChannelsContainerValidations(ChannelsContainer channelsContainer) {
        return getUpdatedChannelsContainerValidations(new ValidationContextImpl(channelsContainer));
    }

    @Override
    public Validator getValidator(String implementation) {
        ValidatorCreator validatorCreator = new DefaultValidatorCreator();
        return validatorCreator.getTemplateValidator(implementation);
    }

    Query<IValidationRule> getAllValidationRuleQuery() {
        return queryService.wrap(dataModel.query(IValidationRule.class, IValidationRuleSetVersion.class, IValidationRuleSet.class));
    }

    List<? extends ChannelValidation> getChannelValidations(Channel channel) {
        return dataModel.mapper(ChannelValidation.class).find("channel", channel);
    }

    @Override
    public List<Validator> getAvailableValidators() {
        ValidatorCreator validatorCreator = new DefaultValidatorCreator();
        return validatorFactories.stream()
                .map(ValidatorFactory::available)
                .flatMap(List::stream)
                .map(validatorCreator::getTemplateValidator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Validator> getAvailableValidators(QualityCodeSystem qualityCodeSystem) {
        ValidatorCreator validatorCreator = new DefaultValidatorCreator();
        return validatorFactories.stream()
                .map(ValidatorFactory::available)
                .flatMap(List::stream)
                .map(validatorCreator::getTemplateValidator)
                .filter(validator -> validator.getSupportedQualityCodeSystems().contains(qualityCodeSystem))
                .collect(Collectors.toList());
    }

    @Override
    public ValidationEvaluator getEvaluator() {
        return new ValidationEvaluatorImpl(this);
    }

    @Override
    public ValidationEvaluator getEvaluator(Meter meter) {
        return new ValidationEvaluatorForMeter(this, meter);
    }

    @Override
    public void addValidatorFactory(ValidatorFactory validatorfactory) {
        addResource(validatorfactory);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(ValidatorFactory validatorfactory) {
        validatorFactories.add(validatorfactory);
    }

    void removeResource(ValidatorFactory validatorfactory) {
        validatorFactories.remove(validatorfactory);
    }

    @Override
    public String getComponentName() {
        return ValidationService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Collections.addAll(keys, TranslationKeys.values());
        Collections.addAll(keys, DataValidationTaskStatus.values());
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    class DefaultValidatorCreator implements ValidatorCreator {

        @Override
        public Validator getValidator(String implementation, Map<String, Object> props) {
            return validatorFactories.stream()
                    .filter(hasImplementation(implementation))
                    .findFirst()
                    .orElseThrow(() -> new ValidatorNotFoundException(thesaurus, implementation))
                    .create(implementation, props);
        }

        public Validator getTemplateValidator(String implementation) {
            return validatorFactories.stream()
                    .filter(hasImplementation(implementation))
                    .findFirst()
                    .orElseThrow(() -> new ValidatorNotFoundException(thesaurus, implementation))
                    .createTemplate(implementation);
        }

        private Predicate<ValidatorFactory> hasImplementation(String implementation) {
            return f -> f.available().contains(implementation);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addValidationRuleSetResolver(ValidationRuleSetResolver resolver) {
        ruleSetResolvers.add(resolver);
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet validationRuleSet) {
        for (ValidationRuleSetResolver resolver : ruleSetResolvers) {
            if (resolver.isValidationRuleSetInUse(validationRuleSet)) {
                return true;
            }
        }
        return false;
    }

    public void removeValidationRuleSetResolver(ValidationRuleSetResolver resolver) {
        ruleSetResolvers.remove(resolver);
    }

    DataModel getDataModel() {
        return dataModel;
    }

    private Optional<? extends ChannelsContainerValidation> findChannelsContainerValidation(ChannelsContainer channelsContainer, ValidationRuleSet ruleSet) {
        return getChannelsContainerValidations(channelsContainer).stream()
                .filter(channelsContainerValidation -> channelsContainerValidation.getRuleSet().equals(ruleSet))
                .findFirst();
    }

    @Override
    public void activate(ChannelsContainer channelsContainer, ValidationRuleSet ruleSet) {
        findChannelsContainerValidation(channelsContainer, ruleSet).ifPresent(channelsContainerValidation -> {
            channelsContainerValidation.activate();
            channelsContainerValidation.save();
        });
    }

    @Override
    public void deactivate(ChannelsContainer channelsContainer, ValidationRuleSet ruleSet) {
        findChannelsContainerValidation(channelsContainer, ruleSet).ifPresent(channelsContainerValidation -> {
            channelsContainerValidation.deactivate();
            channelsContainerValidation.save();
        });
    }

    @Override
    public List<ValidationRuleSet> activeRuleSets(ChannelsContainer channelsContainer) {
        return getActivePersistedChannelsContainerValidations(channelsContainer).stream()
                .map(ChannelsContainerValidation::getRuleSet)
                .collect(Collectors.toList());
    }

    @Override
    public DataValidationTaskBuilder newTaskBuilder() {
        return new DataValidationTaskBuilderImpl(dataModel, this);
    }

    @Override
    public Query<DataValidationTask> findValidationTasksQuery() {
        return queryService.wrap(dataModel.query(DataValidationTask.class));
    }

    @Override
    public List<DataValidationTask> findValidationTasks() {
        return findValidationTasksQuery().select(Condition.TRUE, Order.descending("lastRun").nullsLast());
    }

    @Override
    public Optional<DataValidationTask> findValidationTask(long id) {
        return dataModel.mapper(DataValidationTask.class).getOptional(id);
    }

    @Override
    public Optional<DataValidationTask> findAndLockValidationTaskByIdAndVersion(long id, long version) {
        return dataModel.mapper(DataValidationTask.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<DataValidationTask> findValidationTaskByName(String name) {
        Query<DataValidationTask> query =
                queryService.wrap(dataModel.query(DataValidationTask.class, RecurrentTask.class));
        Condition condition = where("recurrentTask.name").isEqualTo(name);
        return query.select(condition).stream().findFirst();
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public DataValidationOccurrence createValidationOccurrence(TaskOccurrence taskOccurrence) {
        DataValidationTask task = getDataValidationTaskForRecurrentTask(taskOccurrence.getRecurrentTask()).orElseThrow(IllegalArgumentException::new);
        DataValidationOccurrenceImpl occurrence = DataValidationOccurrenceImpl.from(dataModel, taskOccurrence, task);
        occurrence.persist();
        return occurrence;
    }

    @Override
    public Optional<DataValidationOccurrence> findDataValidationOccurrence(TaskOccurrence occurrence) {
        return dataModel.query(DataValidationOccurrence.class, DataValidationTask.class).select(EQUAL.compare("taskOccurrence", occurrence)).stream().findFirst();
    }

    DataValidationOccurrence findAndLockDataValidationOccurrence(TaskOccurrence occurrence) {
        return dataModel.mapper(DataValidationOccurrence.class).lock(occurrence.getId());
    }

    @Override
    public Optional<? extends ValidationRuleSetVersion> findValidationRuleSetVersion(long id) {
        return dataModel.mapper(IValidationRuleSetVersion.class).getOptional(id);
    }

    @Override
    public Optional<? extends ValidationRuleSetVersion> findAndLockValidationRuleSetVersionByIdAndVersion(long id, long version) {
        return dataModel.mapper(IValidationRuleSetVersion.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<? extends ValidationRule> findValidationRule(long id) {
        return dataModel.mapper(IValidationRule.class).getOptional(id);
    }

    @Override
    public Optional<? extends ValidationRule> findAndLockValidationRuleByIdAndVersion(long id, long version) {
        return dataModel.mapper(IValidationRule.class).lockObjectIfVersion(version, id);
    }

    @Override
    public List<DataValidationTask> findByDeviceGroup(EndDeviceGroup endDeviceGroup, int skip, int limit) {
        return dataModel.stream(DataValidationTask.class)
                .filter(Where.where("endDeviceGroup").isEqualTo(endDeviceGroup))
                .skip(skip)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public DataModel dataModel() {
        return dataModel;
    }

    @Override
    public KpiService kpiService() {
        return kpiService;
    }

    private Optional<DataValidationTask> getDataValidationTaskForRecurrentTask(RecurrentTask recurrentTask) {
        return dataModel.mapper(DataValidationTask.class).getUnique("recurrentTask", recurrentTask);
    }

    private void registerDataValidationKpiService(BundleContext bundleContext) {
        this.serviceRegistrations.add(bundleContext.registerService(DataValidationKpiService.class, this.dataValidationKpiService, null));
    }

}
