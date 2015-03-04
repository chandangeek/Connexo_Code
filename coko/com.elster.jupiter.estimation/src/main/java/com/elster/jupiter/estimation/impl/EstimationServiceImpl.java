package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.UpdatableHolder;
import com.elster.jupiter.util.streams.Functions;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.estimation", service = {InstallService.class, EstimationService.class}, property = "name=" + EstimationService.COMPONENTNAME, immediate = true)
public class EstimationServiceImpl implements EstimationService, InstallService {

    private final List<EstimatorFactory> estimatorFactories = new CopyOnWriteArrayList<>();
    private final List<EstimationResolver> resolvers = new CopyOnWriteArrayList<>();

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
    public void install() {
        //TODO automatically generated method body, provide implementation.

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
}
