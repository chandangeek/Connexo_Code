package com.elster.jupiter.estimation;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ConsumerType;

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

    EstimationResult estimate(List<EstimationBlock> estimationBlock);

    String getDisplayName();

    String getDefaultFormat();

    default void validateProperties(Map<String, Object> properties) {

    }

    NlsKey getNlsKey();

    List<String> getRequiredProperties();

    /**
     * Returns the set of target applications supported by this estimator.
     *
     * @return the set of target applications supported by this estimator.
     * @see EstimationService#getAvailableEstimatorImplementations(String)
     * @see EstimationService#getAvailableEstimators(String)
     */
    Set<String> getSupportedApplications();
}
