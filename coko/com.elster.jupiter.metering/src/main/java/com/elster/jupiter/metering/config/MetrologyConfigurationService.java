package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.nls.TranslationKey;

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

    UsagePointMetrologyConfigurationBuilder newUsagePointMetrologyConfiguration(String name, ServiceCategory serviceCategory);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(long id);

    Optional<MetrologyConfiguration> findAndLockMetrologyConfiguration(long id, long version);

    Optional<MetrologyConfiguration> findMetrologyConfiguration(String name);

    List<MetrologyConfiguration> findAllMetrologyConfigurations();

    boolean isInUse(MetrologyConfiguration metrologyConfiguration);

    MeterRole newMeterRole(TranslationKey key);

    Optional<MeterRole> findMeterRole(String key);

    FormulaBuilder newFormulaBuilder(Formula.Mode mode);

    /**
     * Good for now but will need to be facaded with another API before final release.
     */
    Optional<Formula> findFormula(long id);

    List<Formula> findFormulas();

    ReadingTypeTemplate createReadingTypeTemplate(String name);

    Optional<ReadingTypeTemplate> findReadingTypeTemplate(long id);

    Optional<ReadingTypeTemplate> findAndLockReadingTypeTemplateByIdAndVersion(long id, long version);

    MetrologyPurpose.MetrologyPurposeBuilder createMetrologyPurpose();

    Optional<MetrologyPurpose> findMetrologyPurpose(long id);

    Optional<MetrologyPurpose> findMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose);

    List<MetrologyPurpose> getMetrologyPurposes();

    ReadingTypeDeliverable createReadingTypeDeliverable(MetrologyConfiguration metrologyConfiguration, String name, ReadingType readingType, Formula formula);

    List<ReadingTypeDeliverable> findReadingTypeDeliverable(ReadingTypeDeliverableFilter filter);

    Optional<ReadingTypeDeliverable> findReadingTypeDeliverable(long id);

    Optional<ReadingTypeRequirement> findReadingTypeRequirement(long id);
}