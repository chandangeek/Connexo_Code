package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.impl.config.ExpressionNode;

import java.util.List;
import java.util.Optional;

/**
 * Provides services to manage {@link MetrologyConfiguration}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-15 (13:09)
 */
public interface MetrologyConfigurationService {

    String COMPONENT_NAME = "MCF";

    MetrologyConfiguration newMetrologyConfiguration(String name);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(long id);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(String name);

    List<MetrologyConfiguration> findAllMetrologyConfigurations();

    /**
     * Good for now but will need to be facaded with another API before final release.
     */
    Formula newFormula(Formula.Mode mode, ExpressionNode node);

    /**
     * Good for now but will need to be facaded with another API before final release.
     */
    Optional<Formula> findFormula(long id);

}