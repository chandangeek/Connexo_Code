package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.NlsKey;

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

    UsagePointMetrologyConfigurationBuilder newUsagePointMetrologyConfiguration(String name, ServiceCategory serviceCategory);

    List<UsagePointMetrologyConfiguration> findLinkableMetrologyConfigurations(UsagePoint usagePoint);

    Optional<MetrologyContract> findMetrologyContract(Object id);

    boolean isInUse(MetrologyConfiguration metrologyConfiguration);

    MeterRole newMeterRole(NlsKey name);

    MeterRole findDefaultMeterRole(DefaultMeterRole defaultMeterRole);

    Optional<MeterRole> findMeterRole(String key);

    ReadingTypeTemplate.ReadingTypeTemplateAttributeSetter createReadingTypeTemplate(String name);

    List<ReadingTypeTemplate> getReadingTypeTemplates();

    Optional<ReadingTypeTemplate> findReadingTypeTemplate(String name);

    MetrologyPurpose createMetrologyPurpose(NlsKey name, NlsKey description);

    Optional<MetrologyPurpose> findMetrologyPurpose(long id);

    List<MetrologyPurpose> getMetrologyPurposes();

}