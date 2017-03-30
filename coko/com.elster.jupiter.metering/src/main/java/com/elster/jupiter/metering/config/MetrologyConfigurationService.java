/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidator;
import com.elster.jupiter.metering.Meter;
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

    MetrologyConfigurationBuilder newMetrologyConfiguration(String name, ServiceCategory serviceCategory);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(long id);

    Optional<MetrologyConfiguration> findAndLockMetrologyConfiguration(long id, long version);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(String name);

    List<MetrologyConfiguration> findAllMetrologyConfigurations();

    UsagePointMetrologyConfigurationBuilder newUsagePointMetrologyConfiguration(String name, ServiceCategory serviceCategory);

    List<UsagePointMetrologyConfiguration> findLinkableMetrologyConfigurations(UsagePoint usagePoint);

    boolean isInUse(MetrologyConfiguration metrologyConfiguration);

    MeterRole newMeterRole(NlsKey name);

    MeterRole findDefaultMeterRole(DefaultMeterRole defaultMeterRole);

    Optional<MeterRole> findMeterRole(String key);

    ReadingTypeTemplate.ReadingTypeTemplateAttributeSetter createReadingTypeTemplate(String name);

    List<ReadingTypeTemplate> getReadingTypeTemplates();

    Optional<ReadingTypeTemplate> findReadingTypeTemplate(String name);

    Optional<ReadingTypeTemplate> findReadingTypeTemplate(long id);

    MetrologyPurpose createMetrologyPurpose(NlsKey name, NlsKey description);

    Optional<MetrologyPurpose> findMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose);

    Optional<MetrologyPurpose> findMetrologyPurpose(long id);

    List<MetrologyPurpose> getMetrologyPurposes();

    void addCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator);

    void removeCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator);

    void validateUsagePointMeterActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint) throws
            CustomUsagePointMeterActivationValidationException;

    Optional<MetrologyContract> findMetrologyContract(long id);

    Optional<MetrologyContract> findAndLockMetrologyContract(long id, long version);

    Finder<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurationFinderFor(MetrologyContract contract);
}