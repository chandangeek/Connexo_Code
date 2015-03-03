package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.estimation", service = {InstallService.class, EstimationService.class}, property = "name=" + EstimationService.COMPONENTNAME, immediate = true)
public class EstimationServiceImpl implements EstimationService, InstallService {

    private final List<EstimatorFactory> estimatorFactories = new CopyOnWriteArrayList<>();

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
    public EstimationResult estimate(MeterActivation meterActivation) {
        List<ReadingType> readingTypes = meterActivation.getReadingTypes();
        for (ReadingType readingType : readingTypes) {
            if (estimate(meterActivation, readingType).equals(EstimationResult.COULD_NOT_ESTIMATE)) {
                return EstimationResult.COULD_NOT_ESTIMATE;
            }
        }
        return EstimationResult.ESTIMATED;
    }

    @Override
    public EstimationResult estimate(MeterActivation meterActivation, ReadingType readingType) {
        List<Estimator> availableEstimators = this.getAvailableEstimators();
        for (Estimator estimator : availableEstimators) {
            EstimationResult estimationResult = estimate(meterActivation, readingType, estimator);
            if (estimationResult.equals(EstimationResult.ESTIMATED)) {
                return estimationResult;
            }
        }
        return EstimationResult.COULD_NOT_ESTIMATE;
    }

    @Override
    public EstimationResult estimate(MeterActivation meterActivation, ReadingType readingType, Estimator estimator) {
        List<EstimationBlock> blocksToEstimate =
                new EstimationEngine().findBlocksToEstimate(meterActivation, readingType);
        for (EstimationBlock estimationBlock : blocksToEstimate) {
            EstimationResult estimationResult = estimator.estimate(estimationBlock);
            if (estimationResult.equals(EstimationResult.COULD_NOT_ESTIMATE)) {
                return EstimationResult.COULD_NOT_ESTIMATE;
            }
        }
        return EstimationResult.ESTIMATED;
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
}
