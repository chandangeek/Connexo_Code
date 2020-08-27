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
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.*;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.upgrade.V10_5SimpleUpgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.EventType;
import com.elster.jupiter.validation.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.*;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    static final String SUBSCRIBER_NAME = "DataValidation";
    public static final String VALIDATION_USER = "validation";
    public static final Logger LOGGER = Logger.getLogger(ValidationService.class.getName());

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
    private final List<ValidationPropertyResolver> validationPropertyResolvers = new CopyOnWriteArrayList<>();
    private DestinationSpec destinationSpec;
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    public ValidationServiceImpl() {
        // for OSGi
    }

    @Inject
    ValidationServiceImpl(BundleContext bundleContext,
                          Clock clock,
                          MessageService messageService,
                          EventService eventService,
                          TaskService taskService,
                          MeteringService meteringService,
                          MeteringGroupsService meteringGroupsService,
                          OrmService ormService,
                          QueryService queryService,
                          NlsService nlsService,
                          UserService userService,
                          Publisher publisher,
                          UpgradeService upgradeService,
                          KpiService kpiService,
                          MetrologyConfigurationService metrologyConfigurationService,
                          SearchService searchService) {
        this();
        setClock(clock);
        setMetrologyConfigurationService(metrologyConfigurationService);
        setSearchService(searchService);
        setMessageService(messageService);
        setEventService(eventService);
        setMeteringService(meteringService);
        setMeteringGroupsService(meteringGroupsService);
        setTaskService(taskService);
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        setKpiService(kpiService);
        setUpgradeService(upgradeService);
        activate(bundleContext);

        // subscribe manually when not using OSGi
        ValidationEventHandler handler = new ValidationEventHandler();
        handler.setValidationService(this);
        publisher.addSubscriber(handler);
    }

    @Activate
    public final void activate(BundleContext context) {
        LOGGER.log(Level.INFO, "Validation service activated");
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
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(DestinationSpec.class).toProvider(ValidationServiceImpl.this::getDestination);
                bind(SearchService.class).toInstance(searchService);
                bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
            }
        });
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                dataModel,
                InstallerImpl.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(Version.version(10, 2), UpgraderV10_2.class)
                        .put(Version.version(10, 3), UpgraderV10_3.class)
                        .put(Version.version(10, 5), V10_5SimpleUpgrader.class)
                        .put(Version.version(10, 7), UpgraderV10_7.class)
                        .put(Version.version(10, 7, 2), UpgraderV10_7_2.class)
                        .put(Version.version(10, 8, 1), UpgraderV10_8_1.class)
                        .build());
    }

    @Deactivate
    public void deactivate() {
        LOGGER.log(Level.INFO, "Validation service deactivated");
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
                        .ifPresent(channelsContainer -> updatedChannelsContainerValidationsFor(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDC), channelsContainer)));
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
        this.checkChannelsContainer(channelsContainer);
        return getPurposeValidation(channelsContainer)
                .map(PurposeValidationImpl::getActivationStatus)
                .orElse(false);
    }

    @Override
    public void activateValidation(ChannelsContainer channelsContainer) {
        this.checkChannelsContainer(channelsContainer);
        PurposeValidationImpl purposeValidation = getPurposeValidation(channelsContainer)
                .orElse(createPurposeValidation(channelsContainer));
        if (!purposeValidation.getActivationStatus()) {
            purposeValidation.setActivationStatus(true);
            purposeValidation.save();
        }
    }

    @Override
    public void deactivateValidation(ChannelsContainer channelsContainer) {
        this.checkChannelsContainer(channelsContainer);
        this.getPurposeValidation(channelsContainer)
                .filter(PurposeValidationImpl::getActivationStatus)
                .ifPresent(purposeValidation -> {
                            purposeValidation.setActivationStatus(false);
                            purposeValidation.save();
                        }
                );
    }

    private Optional<PurposeValidationImpl> getPurposeValidation(ChannelsContainer channelsContainer) {
        return dataModel.mapper(PurposeValidationImpl.class).getOptional(channelsContainer.getId());
    }

    private PurposeValidationImpl createPurposeValidation(ChannelsContainer channelsContainer) {
        return new PurposeValidationImpl(dataModel).init(channelsContainer);
    }

    private void checkChannelsContainer(ChannelsContainer channelsContainer) {
        if (channelsContainer == null) {
            throw new IllegalArgumentException("Channels container cannot be null");
        }
    }

    @Override
    public void forceUpdateValidationStatus(ChannelsContainer channelsContainer) {
        getUpdatedChannelsContainerValidations(new ValidationContextImpl(channelsContainer));
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

    /**
     * Please consider {@link #moveLastCheckedBefore(ChannelsContainer, Instant)} instead.
     */
    @Deprecated
    @Override
    public void updateLastChecked(ChannelsContainer channelsContainer, Instant date) {
        updatedChannelsContainerValidationsFor(new ValidationContextImpl(channelsContainer)).updateLastChecked(Objects.requireNonNull(date));
    }

    @Override
    public void moveLastCheckedBefore(ChannelsContainer channelsContainer, Instant date) {
        updatedChannelsContainerValidationsFor(new ValidationContextImpl(channelsContainer)).moveLastCheckedBefore(date);
    }

    /**
     * Please consider {@link #moveLastCheckedBefore(Channel, Instant)} instead.
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
        validate(new ValidationContextImpl(targetQualityCodeSystems, channelsContainer, LOGGER));
    }

    @Override
    public void validate(Set<QualityCodeSystem> targetQualityCodeSystems, ChannelsContainer channelsContainer, ReadingType readingType) {
        validate(new ValidationContextImpl(targetQualityCodeSystems, channelsContainer, readingType, LOGGER));
    }

    @Override
    public void validate(ValidationContext validationContext, Instant validateAtMostFrom) {
        ChannelsContainerValidationList container = updatedChannelsContainerValidationsFor(validationContext);
        container.moveLastCheckedBefore(validateAtMostFrom);
        container.validate();
    }

    @Override
    public void validate(ValidationContext validationContext, Range<Instant> interval) {
        if (!interval.hasLowerBound()) {
            throw new IllegalArgumentException("Interval must have lower bound");
        }
        ChannelsContainerValidationList container = updatedChannelsContainerValidationsFor(validationContext);
        container.moveLastCheckedBefore(interval.lowerEndpoint());
        if (interval.hasUpperBound()) {
            container.validate(interval.upperEndpoint());
        } else {
            container.validate();
        }
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
        return Ranges.copy(range).withoutUpperBound();
    }


    private Set<QualityCodeSystem> getQualityCodeSystemsWithAllowedValidation(ValidationContext validationContext) {
        Set<QualityCodeSystem> toValidate = this.getEditableCopy(validationContext.getQualityCodeSystems());

        if (toValidate.contains(QualityCodeSystem.MDC) && !isValidationActiveOnMeter(validationContext)) {
            toValidate.remove(QualityCodeSystem.MDC);
        }

        if (toValidate.contains(QualityCodeSystem.MDM) && !isValidationActiveOnPurpose(validationContext)) {
            toValidate.remove(QualityCodeSystem.MDM);
        }

        return toValidate;
    }

    private Set<QualityCodeSystem> getEditableCopy(Set<QualityCodeSystem> qualityCodeSystems) {
        if (qualityCodeSystems == null || qualityCodeSystems.isEmpty()) {
            return EnumSet.allOf(QualityCodeSystem.class);
        }
        return EnumSet.copyOf(qualityCodeSystems);
    }

    private boolean isValidationActiveOnMeter(ValidationContext validationContext) {
        return validationContext.getMeter()
                .flatMap(this::getMeterValidation)
                .map(MeterValidationImpl::getActivationStatus)
                .orElse(false);
    }

    private boolean isValidationActiveOnPurpose(ValidationContext validationContext) {
        ChannelsContainer channelsContainer = validationContext.getChannelsContainer();
        return this.getPurposeValidation(channelsContainer)
                .map(PurposeValidationImpl::getActivationStatus)
                .orElse(false);
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

    List<EffectiveChannelsContainerValidation> getUpdatedChannelsContainerValidations(ValidationContext validationContext) {
        Map<ValidationRuleSet, RangeSet<Instant>> ruleSets = ruleSetResolvers.stream()
                .flatMap(r -> r.resolve(validationContext).entrySet().stream())
                .filter(ruleSet -> validationContext.getQualityCodeSystems().isEmpty() || validationContext.getQualityCodeSystems().contains(ruleSet.getKey().getQualityCodeSystem()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
        List<ChannelsContainerValidation> persistedChannelsContainerValidations = getPersistedChannelsContainerValidations(validationContext.getChannelsContainer());
        Map<ValidationRuleSet, ChannelsContainerValidation> returnMap = ruleSets.entrySet().stream()
                .map(ruleSet -> Pair.of(ruleSet, getForRuleSet(persistedChannelsContainerValidations, ruleSet.getKey())))
                .map(validationPair -> validationPair.getLast().orElseGet(() -> applyRuleSet(validationPair.getFirst().getKey(), validationContext)))
                .collect(Collectors.toMap(ChannelsContainerValidation::getRuleSet, Function.identity(), (a, b) -> a));
        returnMap.values()
                .forEach(channelsContainerValidation -> channelsContainerValidation.getChannelsContainer().getChannels()
                        .stream()
                        .filter(channel -> !channelsContainerValidation.getRuleSet().getRules(channel.getReadingTypes()).isEmpty())
                        .filter(channel -> !channelsContainerValidation.getChannelValidation(channel).isPresent())
                        .forEach(channelsContainerValidation::addChannelValidation));
        persistedChannelsContainerValidations.stream()
                .filter(channelsContainerValidation -> {
                    ValidationRuleSet validationRuleSet = channelsContainerValidation.getRuleSet();
                    return ruleSets.entrySet().stream().noneMatch(e -> e.getKey().equals(validationRuleSet))
                            && (validationContext.getQualityCodeSystems().isEmpty() || validationContext.getQualityCodeSystems().contains(validationRuleSet.getQualityCodeSystem()))
                            && (validationRuleSet.getObsoleteDate() != null || !isValidationRuleSetInUse(validationRuleSet));
                })
                .forEach(ChannelsContainerValidation::makeObsolete);
        return ruleSets.entrySet().stream()
                .filter(entry -> returnMap.containsKey(entry.getKey()))
                .map(entry -> new EffectiveChannelsContainerValidation(returnMap.get(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
    }

    ChannelsContainerValidationList activeChannelsContainerValidationsFor(ChannelsContainer channelsContainer) {
        return dataModel.getInstance(ChannelsContainerValidationList.class).ofActivePersistedValidations(channelsContainer, LOGGER);
    }

    ChannelsContainerValidationList updatedChannelsContainerValidationsFor(ValidationContext validationContext) {
        return dataModel.getInstance(ChannelsContainerValidationList.class).ofUpdatedValidations(validationContext, LOGGER);
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

    private ChannelsContainerValidation applyRuleSet(ValidationRuleSet ruleSet, ValidationContext validationContext) {
        ChannelsContainer channelsContainer = validationContext.getChannelsContainer();
        ChannelsContainerValidation channelsContainerValidation = dataModel.getInstance(ChannelsContainerValidationImpl.class).init(channelsContainer);
        channelsContainerValidation.setRuleSet(ruleSet);
        channelsContainer.getChannels().stream()
                .filter(c -> !ruleSet.getRules(c.getReadingTypes()).isEmpty())
                .forEach(channelsContainerValidation::addChannelValidation);
        if (validationContext.getMeter().isPresent()) {
            channelsContainerValidation.setInitialActivationStatus(isValidationRuleSetActiveOnDeviceConfig(ruleSet.getId(), Long.parseLong(validationContext.getMeter().get().getAmrId())));
        }
        try {
            channelsContainerValidation.save();
        } catch (UnderlyingSQLFailedException ex) {
            return getExistingChannelsContainerValidation(ruleSet, channelsContainer, ex);
        }
        return channelsContainerValidation;
    }

    private ChannelsContainerValidation getExistingChannelsContainerValidation(ValidationRuleSet ruleSet, ChannelsContainer channelsContainer, UnderlyingSQLFailedException ex) {
        if (ex.getCause() instanceof SQLIntegrityConstraintViolationException) {
            Condition condition = where("channelsContainer").isEqualTo(channelsContainer).and(where("ruleSet").isEqualTo(ruleSet))
                    .and(where("obsoleteTime").isNull());
            List<ChannelsContainerValidation> result = dataModel.query(ChannelsContainerValidation.class, ChannelValidation.class).select(condition);
            if (result.isEmpty()) {
                LOGGER.severe("Database reported IntegrityConstraintViolation in " + TableSpecs.VAL_MA_VALIDATION.name() +
                        " for ruleSet=" + ruleSet.getId() +
                        ", channelContainer=" + channelsContainer.getId() + "obsoleteTime=null, still can't find the record");
                throw ex;
            }
            return result.get(0);
        }
        throw ex;
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
    public ValidationEvaluator getEvaluator(ChannelsContainer container) {
        return new ValidationEvaluatorForChannelContainer(this, container);
    }

    @Override
    public void addValidatorFactory(ValidatorFactory validatorfactory) {
        addResource(validatorfactory);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(ValidatorFactory validatorfactory) {
        LOGGER.log(Level.INFO, "Validation service: add factory " + String.valueOf(validatorfactory));
        validatorFactories.add(validatorfactory);
    }

    void removeResource(ValidatorFactory validatorfactory) {
        LOGGER.log(Level.INFO, "Validation service: remove factory " + String.valueOf(validatorfactory));
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

    @Override
    public boolean isValidationRuleSetActiveOnDeviceConfig(long validationRuleSetId, long deviceId) {
        return ruleSetResolvers
                .stream()
                .filter(ValidationRuleSetResolver::canHandleRuleSetStatus)
                .findFirst()
                .filter(resolver -> resolver.isValidationRuleSetActiveOnDeviceConfig(validationRuleSetId, deviceId)).isPresent();
    }

    public void removeValidationRuleSetResolver(ValidationRuleSetResolver resolver) {
        ruleSetResolvers.remove(resolver);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addValidationPropertyResolver(ValidationPropertyResolver resolver) {
        validationPropertyResolvers.add(resolver);
    }

    public void removeValidationPropertyResolver(ValidationPropertyResolver resolver) {
        validationPropertyResolvers.remove(resolver);
    }

    @Override
    public List<ValidationPropertyResolver> getValidationPropertyResolvers() {
        return Collections.unmodifiableList(validationPropertyResolvers);
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
    public Optional<DataValidationTask> findValidationTaskByRecurrentTaskId(long id) {
        Query<DataValidationTask> query =
                queryService.wrap(dataModel.query(DataValidationTask.class, RecurrentTask.class));
        Condition condition = where("recurrentTask.id").isEqualTo(id);
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

    @Override
    public List<ValidationRule> findValidationRules(Collection<Long> ids) {
        return dataModel.mapper(ValidationRule.class).select(Where.where("id").in(new ArrayList<>(ids)));
    }
}
