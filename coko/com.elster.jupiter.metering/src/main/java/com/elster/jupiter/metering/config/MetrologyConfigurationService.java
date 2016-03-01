package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ServiceCategory;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Provides services to manage {@link MetrologyConfiguration}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-15 (13:09)
 */
@ProviderType
public interface MetrologyConfigurationService {

    String COMPONENT_NAME = "MCF";

    MetrologyConfigurationBuilder newMetrologyConfiguration(String name, ServiceCategory serviceCategory);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(long id);

    Optional<MetrologyConfiguration> findAndLockMetrologyConfiguration(long id, long version);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(String name);

    List<MetrologyConfiguration> findAllMetrologyConfigurations();

    boolean isInUse(MetrologyConfiguration metrologyConfiguration);

}