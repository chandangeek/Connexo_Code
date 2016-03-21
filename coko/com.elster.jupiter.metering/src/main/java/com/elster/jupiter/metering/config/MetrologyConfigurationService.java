package com.elster.jupiter.metering.config;

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

    Optional<MetrologyConfiguration> findAndLockMetrologyConfiguration(long id, long version);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(String name);

    List<MetrologyConfiguration> findAllMetrologyConfigurations();

    boolean isInUse(MetrologyConfiguration metrologyConfiguration);

    FormulaBuilder newFormulaBuilder(Formula.Mode mode);

    /**
     * Good for now but will need to be facaded with another API before final release.
     */
    Optional<Formula> findFormula(long id);

    List<Formula> findFormulas();

    Optional<ReadingTypeDeliverable> findReadingTypeDeliverable(long id);

    Optional<ReadingTypeRequirement> findReadingTypeRequirement(long id);


}