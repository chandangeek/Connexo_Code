package com.elster.insight.usagepoint.config;

import java.util.List;
import java.util.Optional;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.validation.ValidationRuleSet;

public interface UsagePointConfigurationService {
    static String COMPONENTNAME = "UPC";

    MetrologyConfiguration newMetrologyConfiguration(String name);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(long id);

    List<MetrologyConfiguration> findAllMetrologyConfigurations();

    UsagePointMetrologyConfiguration link(UsagePoint up, MetrologyConfiguration mc);

    Optional<MetrologyConfiguration> findMetrologyConfigurationForUsagePoint(UsagePoint up);

    List<UsagePoint> findUsagePointsForMetrologyConfiguration(MetrologyConfiguration mc);

    List<MetrologyConfiguration> findMetrologyConfigurationsForValidationRuleSet(long id);

}
