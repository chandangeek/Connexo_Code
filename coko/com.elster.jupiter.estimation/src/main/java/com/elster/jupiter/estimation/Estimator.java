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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

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

    default void validateProperties(Map<String, Object> properties) {
        // empty by default
    }

    NlsKey getNlsKey();

    List<String> getRequiredProperties();

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
}
