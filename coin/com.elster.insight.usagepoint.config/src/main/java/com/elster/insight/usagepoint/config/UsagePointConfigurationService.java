package com.elster.insight.usagepoint.config;

import java.util.Optional;

import com.elster.jupiter.domain.util.Finder;

public interface UsagePointConfigurationService {
    static String COMPONENTNAME = "UPC";
    MetrologyConfiguration newMetrologyConfiguration(String name);
    Optional<MetrologyConfiguration> findMetrologyConfiguration(long id);
    Finder<MetrologyConfiguration> findAllMetrologyConfigurations();
}
