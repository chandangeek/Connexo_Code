package com.elster.insight.usagepoint.data.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.data.UsagePointPropertySetValuesExtension;
import com.elster.insight.usagepoint.data.impl.exceptions.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UsagePointPropertySetValuesExtensionImpl implements UsagePointPropertySetValuesExtension {
    private final Clock clock;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final CustomPropertySetService customPropertySetService;
    private final Thesaurus thesaurus;

    private UsagePoint usagePoint;

    @Inject
    public UsagePointPropertySetValuesExtensionImpl(Clock clock,
                                                    UsagePointConfigurationService usagePointConfigurationService,
                                                    CustomPropertySetService customPropertySetService,
                                                    Thesaurus thesaurus) {
        this.clock = clock;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.customPropertySetService = customPropertySetService;
        this.thesaurus = thesaurus;
    }

    public UsagePointPropertySetValuesExtensionImpl init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint;
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyCustomPropertySetValues() {
        return this.getMetrologyCustomPropertySetValues(this.clock.instant());
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyCustomPropertySetValues(Instant effectiveTimeStamp) {
        Optional<MetrologyConfiguration> metrologyConfiguration = this.usagePointConfigurationService
                .findMetrologyConfigurationForUsagePoint(getUsagePoint());
        if (metrologyConfiguration.isPresent()) {
            List<RegisteredCustomPropertySet> customPropertySets = metrologyConfiguration.get().getCustomPropertySets();
            Map<RegisteredCustomPropertySet, CustomPropertySetValues> values = new HashMap<>(customPropertySets.size());
            for (RegisteredCustomPropertySet rcps : customPropertySets) {
                if (!rcps.isViewableByCurrentUser()) {
                    continue;
                }
                if (rcps.getCustomPropertySet().isVersioned()) {
                    values.put(rcps, this.customPropertySetService
                            .getUniqueValuesFor(rcps.getCustomPropertySet(), getUsagePoint(), effectiveTimeStamp));
                } else {
                    values.put(rcps, this.customPropertySetService
                            .getUniqueValuesFor(rcps.getCustomPropertySet(), getUsagePoint()));
                }
            }
            return values;
        }
        return Collections.emptyMap();
    }

    @Override
    public void setMetrologyCustomPropertySetValue(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue) {
        Optional<MetrologyConfiguration> metrologyConfiguration = this.usagePointConfigurationService
                .findMetrologyConfigurationForUsagePoint(getUsagePoint());
        if (metrologyConfiguration.isPresent()) {
            RegisteredCustomPropertySet registeredCustomPropertySet = metrologyConfiguration.get().getCustomPropertySets()
                    .stream()
                    .filter(rcps -> rcps.getCustomPropertySet().getId().equals(customPropertySet.getId()))
                    .findAny()
                    .orElseThrow(() -> UsagePointCustomPropertySetValuesManageException
                            .noLinkedCustomPropertySetOnMetrologyConfiguration(this.thesaurus, customPropertySet.getName()));
            if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
                throw UsagePointCustomPropertySetValuesManageException
                        .customPropertySetIsNotEditableByUser(this.thesaurus, customPropertySet.getName(), metrologyConfiguration.get().getName());
            }
            if (customPropertySet.isVersioned()) {
                throw new UnsupportedOperationException();
            } else {
                this.customPropertySetService.setValuesFor(customPropertySet, getUsagePoint(), customPropertySetValue);
            }
        } else {
            throw UsagePointCustomPropertySetValuesManageException
                    .noLinkedMetrologyConfiguration(this.thesaurus, getUsagePoint().getName());
        }
    }
}
