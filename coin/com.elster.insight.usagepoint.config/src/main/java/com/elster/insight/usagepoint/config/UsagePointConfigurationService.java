package com.elster.insight.usagepoint.config;

import java.util.List;
import java.util.Optional;

public interface UsagePointConfigurationService {
    static String COMPONENTNAME = "UPC";
    MetrologyConfiguration newMetrologyConfiguration(String name);
    Optional<MetrologyConfiguration> findMetrologyConfiguration(long id);
    List<MetrologyConfiguration> findAllMetrologyConfigurations();
}
