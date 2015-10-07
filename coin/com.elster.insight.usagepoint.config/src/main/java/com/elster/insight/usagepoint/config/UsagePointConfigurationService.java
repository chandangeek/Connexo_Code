package com.elster.insight.usagepoint.config;

import java.util.Optional;

import com.elster.jupiter.domain.util.Finder;

public interface UsagePointConfigurationService {
    static String COMPONENTNAME = "UPC";
    Optional<MetrologyConfiguration> findMetrologyConfiguration(String id);
    Finder<MetrologyConfiguration> findAllMetrologyConfigurations();
}
