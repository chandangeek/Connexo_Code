/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ConsumerType
public interface Estimator extends HasDynamicProperties {

    @Override
    List<PropertySpec> getPropertySpecs();

    @Override
    default Optional<PropertySpec> getPropertySpec(String name) {
        return getPropertySpecs()
                .stream()
                .filter(spec -> name.equals(spec.getName()))
                .findAny();
    }

    default void init(Logger logger) {
        // empty by default
    }

    /**
     * The method to perform data estimation
     *
     * @param estimationBlocks a list of {@link EstimationBlock}s to estimate
     * @param system {@link QualityCodeSystem} that performs estimation
     * @return {@link EstimationResult}
     */
    EstimationResult estimate(List<EstimationBlock> estimationBlocks, QualityCodeSystem system);

    String getDisplayName();

    String getDefaultFormat();

    /**
     * Validates values of estimator's properties according to business constraints.
     * Note: the method should not try to validate presence of required properties, because this will be done automatically.
     *
     * @param properties the values to validate
     */
    default void validateProperties(Map<String, Object> properties) {
        // nothing to do by default
    }

    NlsKey getNlsKey();

    default List<String> getRequiredProperties() {
        return getPropertySpecs()
                .stream()
                .filter(PropertySpec::isRequired)
                .map(PropertySpec::getName)
                .collect(Collectors.toList());
    }

    /**
     * Returns the set of target quality code systems supported by this estimator.
     *
     * @return the set of target applications supported by this estimator.
     * @see EstimationService#getAvailableEstimatorImplementations(QualityCodeSystem)
     * @see EstimationService#getAvailableEstimators(QualityCodeSystem)
     */
    Set<QualityCodeSystem> getSupportedQualityCodeSystems();

    /**
     * Returns {@link QualityCodeSystem}s whose reading qualities to take into account during estimation
     *
     * @param currentSystem {@link QualityCodeSystem} that performs estimation
     * @return {@link QualityCodeSystem}s whose reading qualities to take into account during estimation
     */
    static Set<QualityCodeSystem> qualityCodeSystemsToTakeIntoAccount(QualityCodeSystem currentSystem) {
        return ImmutableSet.of(QualityCodeSystem.ENDDEVICE, currentSystem);
    }

    /**
     * Returns the list of {@link PropertySpec}s for which the values can be set on the specified {@link EstimationPropertyDefinitionLevel}.
     * <p>Default implementation assumes that the values for all the {@link PropertySpec}s returned by {@link Estimator#getPropertySpecs()}
     * can be set only on {@link EstimationPropertyDefinitionLevel#ESTIMATION_RULE}
     *
     * @return The List of PropertySpec
     */
    default List<PropertySpec> getPropertySpecs(EstimationPropertyDefinitionLevel level) {
        return EstimationPropertyDefinitionLevel.ESTIMATION_RULE == level ? getPropertySpecs() : Collections.emptyList();
    }
}
