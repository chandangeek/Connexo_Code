package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimation.EstimatorNotFoundException;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.UpdatableHolder;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.streams.Functions;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.estimation", service = {InstallService.class, EstimationService.class}, property = "name=" + EstimationService.COMPONENTNAME, immediate = true)
public class EstimationServiceImpl implements EstimationService, InstallService {

    private final List<EstimatorFactory> estimatorFactories = new CopyOnWriteArrayList<>();
    private final List<EstimationResolver> resolvers = new CopyOnWriteArrayList<>();

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;

    public EstimationServiceImpl() {
    }

    @Inject
    EstimationServiceImpl(MeteringService meteringService, OrmService ormService, QueryService queryService, NlsService nlsService, EventService eventService) {
        setMeteringService(meteringService);
        setOrmService(ormService);
        setQueryService(queryService);
        setNlsService(nlsService);
        setEventService(eventService);
        activate();
        install();
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(MeteringService.class).toInstance(meteringService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(EventService.class).toInstance(eventService);
                bind(EstimatorCreator.class).toInstance(new DefaultEstimatorCreator());
            }
        });
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
    public void estimate(MeterActivation meterActivation) {
        meterActivation.getReadingTypes().forEach(readingType -> this.estimate(meterActivation, readingType));
    }

    @Override
    public void estimate(MeterActivation meterActivation, ReadingType readingType) {
        UpdatableHolder<EstimationResult> result = new UpdatableHolder<>(getInitialBlocksToEstimateAsResult(meterActivation, readingType));
        
        determineEstimators(meterActivation).forEach(estimator -> result.update(estimator.estimate(result.get().remainingToBeEstimated())));
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
        new InstallerImpl(dataModel).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("MTR");
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

    private Stream<Estimator> determineEstimators(MeterActivation meterActivation) {
        return resolvers.stream()
                    .flatMap(resolver -> resolver.resolve(meterActivation).stream())
                    .distinct()
                    .map(this::getEstimator)
                    .flatMap(Functions.asStream());
    }

    private List<EstimationBlock> getBlocksToEstimate(MeterActivation meterActivation, ReadingType readingType) {
        return new EstimationEngine().findBlocksToEstimate(meterActivation, readingType);
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
