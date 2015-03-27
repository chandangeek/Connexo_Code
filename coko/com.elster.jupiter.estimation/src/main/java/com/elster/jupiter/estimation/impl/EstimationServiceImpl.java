package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskBuilder;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimation.EstimatorNotFoundException;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UpdatableHolder;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

@Component(name = "com.elster.jupiter.estimation", service = {InstallService.class, EstimationService.class}, property = "name=" + EstimationService.COMPONENTNAME, immediate = true)
public class EstimationServiceImpl implements IEstimationService, InstallService {

    static final String DESTINATION_NAME = "EstimationTask";
    static final String SUBSCRIBER_NAME = "EstimationTask";
    private final List<EstimatorFactory> estimatorFactories = new CopyOnWriteArrayList<>();
    private final List<EstimationResolver> resolvers = new CopyOnWriteArrayList<>();

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

    private Optional<DestinationSpec> destinationSpec = Optional.empty();

    public EstimationServiceImpl() {
    }

    @Inject
    EstimationServiceImpl(MeteringService meteringService, OrmService ormService, QueryService queryService, NlsService nlsService, EventService eventService, TaskService taskService, MeteringGroupsService meteringGroupsService, MessageService messageService, TimeService timeService, UserService userService) {
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
        activate();
        install();
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
                }
            });
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

    @Override
    public Query<EstimationRuleSet> getEstimationRuleSetQuery() {
        Query<EstimationRuleSet> ruleSetQuery = queryService.wrap(dataModel.query(EstimationRuleSet.class));
        ruleSetQuery.setRestriction(where(EstimationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull());
        return ruleSetQuery;
    }

    @Override
    public Query<? extends EstimationTask> getEstimationTaskQuery() {
        Query<IEstimationTask> estimationTaskQuery = queryService.wrap(dataModel.query(IEstimationTask.class));
        return estimationTaskQuery;
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public List<Estimator> getAvailableEstimators() {
        return estimatorFactories.stream()
                .flatMap(factory -> factory.available().stream().map(factory::createTemplate))
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
    public EstimationReport estimate(MeterActivation meterActivation) {
        EstimationReportImpl report = new EstimationReportImpl();

        meterActivation.getReadingTypes().forEach(readingType -> {
            EstimationReport subReport = this.estimate(meterActivation, readingType);
            report.add(subReport);
        });

        return report;
    }

    @Override
    public EstimationReport estimate(MeterActivation meterActivation, ReadingType readingType) {
        UpdatableHolder<EstimationResult> result = new UpdatableHolder<>(getInitialBlocksToEstimateAsResult(meterActivation, readingType));

        EstimationReportImpl report = new EstimationReportImpl();

        determineEstimationRules(meterActivation)
                .filter(rule -> rule.getReadingTypes().contains(readingType))
                .map(IEstimationRule::createNewEstimator)
                .forEach(estimator -> {
                    estimator.init();
                    EstimationResult estimationResult = result.get();
                    estimationResult.estimated().stream().forEach(block -> report.reportEstimated(readingType, block));
                    result.update(estimator.estimate(estimationResult.remainingToBeEstimated()));
                });
        result.get().estimated().stream().forEach(block -> report.reportEstimated(readingType, block));
        result.get().remainingToBeEstimated().stream().forEach(block -> report.reportUnableToEstimate(readingType, block));
        return report;
    }

    @Override
    public void estimate(MeterActivation meterActivation, ReadingType readingType, Estimator estimator) {
        estimator.estimate(getBlocksToEstimate(meterActivation, readingType));
    }

    @Override
    public EstimationRuleSet createEstimationRuleSet(String name) {
        return dataModel.getInstance(EstimationRuleSetImpl.class).init(name);
    }

    @Override
    public EstimationRuleSet createEstimationRuleSet(String name, String descrption) {
        EstimationRuleSet set = dataModel.getInstance(EstimationRuleSetImpl.class).init(name, descrption);
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
    public void install() {
        new InstallerImpl(dataModel, messageService, userService).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("MTR");
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
    public Optional<? extends EstimationTask> findEstimationTask(long id) {
        return dataModel.mapper(IEstimationTask.class).getOptional(id);
    }

    @Override
    public Optional<? extends EstimationTask> findEstimationTask(RecurrentTask recurrentTask) {
        return dataModel.stream(IEstimationTask.class)
                .filter(Where.where("recurrentTask").isEqualTo(recurrentTask))
                .findFirst();
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

    private SimpleEstimationResult getInitialBlocksToEstimateAsResult(MeterActivation meterActivation, ReadingType readingType) {
        return asInitialResult(getBlocksToEstimate(meterActivation, readingType));
    }

    private SimpleEstimationResult asInitialResult(List<EstimationBlock> blocksToEstimate) {
        return SimpleEstimationResult.of(blocksToEstimate, Collections.emptyList());
    }

    private Stream<IEstimationRule> determineEstimationRules(MeterActivation meterActivation) {
        return decorate(resolvers.stream())
                    .sorted(Comparator.comparing(EstimationResolver::getPriority).reversed())
                    .flatMap(resolver -> resolver.resolve(meterActivation).stream())
                    .map(IEstimationRuleSet.class::cast)
                    .distinct(EstimationRuleSet::getId)
                    .flatMap(set -> set.getRules().stream());
    }

    private List<EstimationBlock> getBlocksToEstimate(MeterActivation meterActivation, ReadingType readingType) {
        return new EstimationEngine().findBlocksToEstimate(meterActivation, readingType);
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
