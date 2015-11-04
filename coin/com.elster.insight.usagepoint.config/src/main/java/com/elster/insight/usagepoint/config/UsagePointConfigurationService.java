package com.elster.insight.usagepoint.config;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.validation.ValidationRuleSet;

@ProviderType
public interface UsagePointConfigurationService {
    static String COMPONENTNAME = "UPC";

    MetrologyConfiguration newMetrologyConfiguration(String name);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(long id);
    Optional<MetrologyConfiguration> findMetrologyConfiguration(String name);

    List<MetrologyConfiguration> findAllMetrologyConfigurations();

    UsagePointMetrologyConfiguration link(UsagePoint up, MetrologyConfiguration mc);

    Optional<MetrologyConfiguration> findMetrologyConfigurationForUsagePoint(UsagePoint up);

    List<UsagePoint> findUsagePointsForMetrologyConfiguration(MetrologyConfiguration mc);

    List<MetrologyConfiguration> findMetrologyConfigurationsForValidationRuleSet(ValidationRuleSet rs);

    Boolean unlink(UsagePoint up, MetrologyConfiguration mc);

    Optional<MetrologyConfiguration> findAndLockMetrologyConfiguration(long id, long version);

}
