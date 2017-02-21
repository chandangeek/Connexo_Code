/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationBlockFormatter;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskBuilder;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimation.EstimatorNotFoundException;
import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
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
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.spi.RelativePeriodCategoryTranslationProvider;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UpdatableHolder;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

@Component(name = "com.elster.jupiter.estimation",
        service = {EstimationService.class, TranslationKeyProvider.class, MessageSeedProvider.class, RelativePeriodCategoryTranslationProvider.class},
        property = "name=" + EstimationService.COMPONENTNAME,
        immediate = true)
public class EstimationServiceImpl implements IEstimationService, TranslationKeyProvider, MessageSeedProvider, RelativePeriodCategoryTranslationProvider {

    static final String ESTIMATION_TASKS_USER = "estimation";
    static final String DESTINATION_NAME = "EstimationTask";
    static final String SUBSCRIBER_NAME = "EstimationTask";
    static final String SUBSCRIBER_DISPLAYNAME = "Handle data estimation";
    public static final Logger LOGGER = Logger.getLogger(EstimationService.class.getName());
    private final List<EstimatorFactory> estimatorFactories = new CopyOnWriteArrayList<>();
    private final List<EstimationResolver> resolvers = new CopyOnWriteArrayList<>();
    private final EstimationEngine estimationEngine = new EstimationEngine();

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile TaskService taskService;
    private volatile MessageService messageService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile UserService userService;
    private volatile TimeService timeService;
    private volatile UpgradeService upgradeService;
    private volatile AppService appService;
    private volatile Clock clock;

    private Optional<DestinationSpec> destinationSpec = Optional.empty();

    public EstimationServiceImpl() {
    }

    @Inject
    EstimationServiceImpl(MeteringService meteringService, OrmService ormService, QueryService queryService,
                          NlsService nlsService, EventService eventService, TaskService taskService,
                          MeteringGroupsService meteringGroupsService, MessageService messageService,
                          TimeService timeService, UserService userService, UpgradeService upgradeService, Clock clock) {
        this();
        setMeteringService(meteringService);
        setOrmService(ormService);
        setQueryService(queryService);
        setNlsService(nlsService);
        setEventService(eventService);
        setTaskService(taskService);
        setMeteringGroupsService(meteringGroupsService);
        setMessageService(messageService);
        setTimeService(timeService);
        setUserService(userService);
        setUpgradeService(upgradeService);
        setClock(clock);
        activate();
    }

    @Activate
    public void activate() {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(DataModel.class).toInstance(dataModel);
                    bind(MeteringService.class).toInstance(meteringService);
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(EventService.class).toInstance(eventService);
                    bind(EstimatorCreator.class).toInstance(new DefaultEstimatorCreator());
                    bind(TaskService.class).toInstance(taskService);
                    bind(IEstimationService.class).toInstance(EstimationServiceImpl.this);
                    bind(EstimationService.class).toInstance(EstimationServiceImpl.this);
                    bind(MessageService.class).toInstance(messageService);
                    bind(TimeService.class).toInstance(timeService);
                    bind(UserService.class).toInstance(userService);
                    bind(Clock.class).toInstance(clock);
                }
            });
            upgradeService.register(
                    InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                    dataModel,
                    InstallerImpl.class,
                    ImmutableMap.of(
                            version(10, 2), UpgraderV10_2.class,
                            version(10, 3), UpgraderV10_3.class
                    ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deactivate
    public void deactivate() {

    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(EstimationService.COMPONENTNAME, "Validation");
        Stream.of(TableSpecs.values()).forEach(spec -> spec.addTo(dataModel));
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(EstimationService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Query<EstimationRuleSet> getEstimationRuleSetQuery() {
        Query<EstimationRuleSet> ruleSetQuery = queryService.wrap(dataModel.query(EstimationRuleSet.class));
        ruleSetQuery.setRestriction(where(EstimationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull());
        return ruleSetQuery;
    }

    @Override
    public Query<? extends EstimationTask> getEstimationTaskQuery() {
        return queryService.wrap(dataModel.query(IEstimationTask.class));
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public List<String> getAvailableEstimatorImplementations(QualityCodeSystem qualityCodeSystem) {
        return estimatorFactories.stream()
                .flatMap(factory -> factory.available().stream()
                        .filter(implementation -> factory.createTemplate(implementation).getSupportedQualityCodeSystems().contains(qualityCodeSystem)))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Estimator> getAvailableEstimators(QualityCodeSystem qualityCodeSystem) {
        return estimatorFactories.stream()
                .flatMap(factory -> factory.available().stream()
                        .map(factory::createTemplate)
                        .filter(estimator -> estimator.getSupportedQualityCodeSystems().contains(qualityCodeSystem)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Estimator> getEstimator(String implementation) {
        return estimatorFactories.stream()
                .filter(factory -> factory.available().contains(implementation))
                .map(factory -> factory.createTemplate(implementation))
                .findFirst();
    }

    @Override
    public Optional<Estimator> getEstimator(String implementation, Map<String, Object> props) {
        return estimatorFactories.stream()
                .filter(factory -> factory.available().contains(implementation))
                .map(factory -> factory.create(implementation, props))
                .findFirst();
    }

    @Override
    public EstimationReport estimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period) {
        return estimate(system, channelsContainer, period, LOGGER);
    }

    @Override
    public EstimationReport estimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period, Logger logger) {
        EstimationReportImpl report;
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            report = new EstimationReportImpl();
            if (channelsContainer.getRange().isConnected(period)) {
                ((MetrologyContractChannelsContainer) channelsContainer).getMetrologyContract().sortReadingTypesByDependencyLevel().stream()
                        .map(readingTypes -> previewEstimate(system, channelsContainer, period, readingTypes, logger))
                        .peek(subReport -> estimationEngine.applyEstimations(system, subReport))
                        .forEach(report::add);
            }
        } else {
            report = previewEstimate(system, channelsContainer, period, logger);
            estimationEngine.applyEstimations(system, report);
        }
        logEstimationReport(channelsContainer, period, logger, report);
        postEvents(report);
        return report;
    }

    private void logEstimationReport(ChannelsContainer channelsContainer, Range<Instant> period, Logger logger, EstimationReportImpl report) {
        long notEstimated = report.getResults().values().stream()
                .map(EstimationResult::remainingToBeEstimated)
                .mapToLong(Collection::size)
                .sum();
        long estimated = report.getResults().values().stream()
                .map(EstimationResult::estimated)
                .mapToLong(Collection::size)
                .sum();
        DateTimeFormatter formatter = DefaultDateTimeFormatters.mediumDate()
                .withLongTime()
                .build()
                .withZone(channelsContainer.getZoneId())
                .withLocale(Locale.ENGLISH);
        String from = formatter.format(period.hasLowerBound() ? period.lowerEndpoint() : channelsContainer.getStart());
        String to = period.hasUpperBound() ? formatter.format(period.upperEndpoint()) : "now";
        String message = "{0} blocks estimated.\nSuccessful estimations {1}, failed estimations {2}\nPeriod of estimation from {3} until {4}";
        LoggingContext.get().info(logger, message, estimated + notEstimated, estimated, notEstimated, from, to);
    }

    private void postEvents(EstimationReportImpl report) {
        report.getResults().values().stream()
                .map(EstimationResult::remainingToBeEstimated)
                .flatMap(Collection::stream)
                .forEach(estimationBlock -> eventService.postEvent(EventType.ESTIMATIONBLOCK_FAILURE.topic(),
                        EstimationBlockEventInfo.forFailure(estimationBlock)));
    }

    @Override
    public EstimationReportImpl previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period) {
        return previewEstimate(system, channelsContainer, period, LOGGER);
    }

    @Override
    public EstimationReportImpl previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period, Logger logger) {
        return previewEstimate(system, channelsContainer, period, channelsContainer.getReadingTypes(period), logger);
    }

    private EstimationReportImpl previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer,
                                                 Range<Instant> period, Set<ReadingType> readingTypes, Logger logger) {
        EstimationReportImpl report = new EstimationReportImpl();
        readingTypes.forEach(readingType -> report.add(previewEstimate(system, channelsContainer, period, readingType, logger)));

        report.getResults().values().stream()
                .map(EstimationResult::remainingToBeEstimated)
                .flatMap(Collection::stream)
                .forEach(estimationBlock -> {
                    String message = "Block {0} could not be estimated.";
                    LoggingContext.get().warning(logger, message, EstimationBlockFormatter.getInstance().format(estimationBlock));
                });

        return report;
    }

    @Override
    public EstimationReport previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer,
                                            Range<Instant> period, ReadingType readingType) {
        return previewEstimate(system, channelsContainer, period, readingType, LOGGER);
    }

    @Override
    public EstimationReportImpl previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer,
                                                Range<Instant> period, ReadingType readingType, Logger logger) {
        UpdatableHolder<EstimationResult> result = new UpdatableHolder<>(
                getInitialBlocksToEstimateAsResult(system, channelsContainer, period, readingType));

        EstimationReportImpl report = new EstimationReportImpl();

        determineEstimationRules(system, channelsContainer)
                .filter(EstimationRule::isActive)
                .filter(rule -> rule.getReadingTypes().contains(readingType))
                .forEach(rule -> {
                    try (LoggingContext parentContext = LoggingContext.getCloseableContext();
                         LoggingContext loggingContext = parentContext.with("rule", rule.getName())) {
                        loggingContext.info(logger, "Attempting rule {rule}");
                        Estimator estimator = rule.createNewEstimator();
                        estimator.init(logger);
                        EstimationResult estimationResult = result.get();
                        estimationResult.estimated().forEach(block -> report.reportEstimated(readingType, block));
                        EstimationResult newResult = estimator.estimate(estimationResult.remainingToBeEstimated(), system);
                        newResult.estimated().forEach(block ->
                                loggingContext.info(logger, "Successful estimation with {rule}: block {0}",
                                        EstimationBlockFormatter.getInstance().format(block)));
                        result.update(newResult);
                    }
                });
        result.get().estimated().forEach(block -> report.reportEstimated(readingType, block));
        result.get().remainingToBeEstimated().forEach(block -> report.reportUnableToEstimate(readingType, block));
        return report;
    }

    @Override
    public EstimationResult previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period,
                                            ReadingType readingType, Estimator estimator) {
        try (LoggingContext parentContext = LoggingContext.getCloseableContext();
             LoggingContext loggingContext = parentContext.with("rule", estimator.getDisplayName())) {
            return estimator.estimate(getBlocksToEstimate(system, channelsContainer, period, readingType), system);
        }
    }

    @Override
    public EstimationRuleSet createEstimationRuleSet(String name, QualityCodeSystem qualityCodeSystem) {
        EstimationRuleSetImpl set = dataModel.getInstance(EstimationRuleSetImpl.class).init(name, qualityCodeSystem);
        set.save();
        return set;
    }

    @Override
    public EstimationRuleSet createEstimationRuleSet(String name, QualityCodeSystem qualityCodeSystem, String description) {
        EstimationRuleSet set = dataModel.getInstance(EstimationRuleSetImpl.class).init(name, qualityCodeSystem, description);
        set.save();
        return set;
    }

    @Override
    public List<IEstimationRuleSet> getEstimationRuleSets() {
        return getRuleSetQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
    }

    private Query<IEstimationRuleSet> getRuleSetQuery() {
        Query<IEstimationRuleSet> ruleSetQuery = queryService.wrap(dataModel.query(IEstimationRuleSet.class));
        ruleSetQuery.setRestriction(isNotObsolete());
        return ruleSetQuery;
    }

    @Override
    public Optional<? extends EstimationRuleSet> getEstimationRuleSet(long id) {
        return dataModel.mapper(IEstimationRuleSet.class).getOptional(id);
    }

    @Override
    public Optional<? extends EstimationRuleSet> findAndLockEstimationRuleSet(long id, long version) {
        return dataModel.mapper(IEstimationRuleSet.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<IEstimationRuleSet> getEstimationRuleSet(String name) {
        return getRuleSetQuery().select(hasName(name).and(isNotObsolete())).stream()
                .findFirst();
    }

    private Condition hasName(String name) {
        return where("name").isEqualTo(name);
    }

    private Condition isNotObsolete() {
        return where(EstimationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull();
    }

    @Override
    public boolean isEstimationRuleSetInUse(EstimationRuleSet estimationRuleSet) {
        return resolvers.stream()
                .anyMatch(resolver -> resolver.isInUse(estimationRuleSet));
    }

    @Override
    public Optional<IEstimationRule> getEstimationRule(long id) {
        return dataModel.mapper(IEstimationRule.class).getOptional(id);
    }

    @Override
    public Optional<? extends EstimationRule> findAndLockEstimationRule(long id, long version) {
        return dataModel.mapper(IEstimationRule.class).lockObjectIfVersion(version, id);
    }

    @Override
    public DestinationSpec getDestination() {
        if (!destinationSpec.isPresent()) {
            destinationSpec = messageService.getDestinationSpec(DESTINATION_NAME);
        }
        return destinationSpec.orElse(null);
    }

    @Override
    public EstimationTaskBuilder newBuilder() {
        return new EstimationTaskBuilderImpl(dataModel);
    }

    @Override
    public List<? extends EstimationTask> findEstimationTasks(QualityCodeSystem qualityCodeSystem) {
        return getEstimationTaskQuery().select(Condition.TRUE, Order.descending("lastRun").nullsLast())
                .stream()
                .filter(task -> task.getQualityCodeSystem().equals(qualityCodeSystem))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<? extends EstimationTask> findEstimationTask(long id) {
        return dataModel.mapper(IEstimationTask.class).getOptional(id);
    }

    @Override
    public Optional<? extends EstimationTask> findAndLockEstimationTask(long id, long version) {
        return dataModel.mapper(IEstimationTask.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<? extends EstimationTask> findEstimationTask(RecurrentTask recurrentTask) {
        return dataModel.stream(IEstimationTask.class)
                .filter(Where.where("recurrentTask").isEqualTo(recurrentTask))
                .findFirst();
    }

    @Override
    public List<EstimationTask> findByDeviceGroup(EndDeviceGroup endDeviceGroup, int skip, int limit) {
        return dataModel.stream(IEstimationTask.class)
                .filter(Where.where("endDeviceGroup").isEqualTo(endDeviceGroup))
                .skip(skip)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEstimatorFactory(EstimatorFactory estimatorFactory) {
        estimatorFactories.add(estimatorFactory);
    }

    public void removeEstimatorFactory(EstimatorFactory estimatorFactory) {
        estimatorFactories.remove(estimatorFactory);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEstimationResolver(EstimationResolver estimationResolver) {
        resolvers.add(estimationResolver);
    }

    public void removeEstimationResolver(EstimationResolver estimationResolver) {
        resolvers.remove(estimationResolver);
    }

    @Override
    public List<EstimationResolver> getEstimationResolvers() {
        return Collections.unmodifiableList(resolvers);
    }

    private EstimationResult getInitialBlocksToEstimateAsResult(QualityCodeSystem system, ChannelsContainer channelsContainer,
                                                                Range<Instant> period, ReadingType readingType) {
        return asInitialResult(getBlocksToEstimate(system, channelsContainer, period, readingType));
    }

    private EstimationResult asInitialResult(List<EstimationBlock> blocksToEstimate) {
        return new InitialEstimationResult(blocksToEstimate);
    }

    private Stream<IEstimationRule> determineEstimationRules(QualityCodeSystem system, ChannelsContainer channelsContainer) {
        return decorate(resolvers.stream())
                .sorted(Comparator.comparing(EstimationResolver::getPriority).reversed())
                .flatMap(resolver -> resolver.resolve(channelsContainer).stream())
                .map(IEstimationRuleSet.class::cast)
                .filter(set -> set.getQualityCodeSystem() == system)
                .distinct(EstimationRuleSet::getId)
                .flatMap(set -> set.getRules().stream());
    }

    private List<EstimationBlock> getBlocksToEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer,
                                                      Range<Instant> period, ReadingType readingType) {
        return estimationEngine.findBlocksToEstimate(system, channelsContainer, period, readingType);
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
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public String getComponentName() {
        return COMPONENTNAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(Privileges.values()),
                Arrays.stream(TranslationKeys.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public Optional<? extends EstimationRule> findEstimationRuleByQualityType(ReadingQualityType readingQualityType) {
        return getEstimationRule(readingQualityType.getIndexCode());
    }

    class DefaultEstimatorCreator implements EstimatorCreator {

        @Override
        public Estimator getEstimator(String implementation, Map<String, Object> props) {
            return estimatorFactories.stream()
                    .filter(hasImplementation(implementation))
                    .findFirst()
                    .orElseThrow(() -> new EstimatorNotFoundException(thesaurus, implementation))
                    .create(implementation, props);
        }

        public Estimator getTemplateEstimator(String implementation) {
            return estimatorFactories.stream()
                    .filter(hasImplementation(implementation))
                    .findFirst()
                    .orElseThrow(() -> new EstimatorNotFoundException(thesaurus, implementation))
                    .createTemplate(implementation);
        }

        private Predicate<EstimatorFactory> hasImplementation(String implementation) {
            return f -> f.available().contains(implementation);
        }
    }

}
