package com.elster.jupiter.metering.config;

public interface MetrologyConfigurationUpdater {
    MetrologyConfigurationUpdater setName(String name);

    MetrologyConfigurationUpdater setDescription(String description);

    MetrologyConfiguration complete();
}