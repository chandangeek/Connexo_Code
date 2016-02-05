package com.elster.insight.usagepoint.data.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsagePointCustomPropertySetExtensionImpl implements UsagePointCustomPropertySetExtension {
    private final Clock clock;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final CustomPropertySetService customPropertySetService;
    private final Thesaurus thesaurus;

    private UsagePoint usagePoint;

    @Inject
    public UsagePointCustomPropertySetExtensionImpl(Clock clock,
                                                    UsagePointConfigurationService usagePointConfigurationService,
                                                    CustomPropertySetService customPropertySetService,
                                                    Thesaurus thesaurus) {
        this.clock = clock;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.customPropertySetService = customPropertySetService;
        this.thesaurus = thesaurus;
    }

    public UsagePointCustomPropertySetExtensionImpl init(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint;
    }

    private Optional<MetrologyConfiguration> getMetrologyConfiguration() {
        return this.usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(getUsagePoint());
    }

    private CustomPropertySetValues getCustomPropertySetValuesWithoutChecks(RegisteredCustomPropertySet rcps, Instant effectiveTimeStamp) {
        if (rcps.getCustomPropertySet().isVersioned()) {
            return this.customPropertySetService.getUniqueValuesFor(rcps.getCustomPropertySet(), getUsagePoint(), effectiveTimeStamp);
        }
        return this.customPropertySetService.getUniqueValuesFor(rcps.getCustomPropertySet(), getUsagePoint());
    }

    private void setCustomPropertySetValuesWithoutChecks(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue) {
        if (customPropertySet.isVersioned()) {
            throw new UnsupportedOperationException();
        } else {
            this.customPropertySetService.setValuesFor(customPropertySet, getUsagePoint(), customPropertySetValue);
        }
        getUsagePoint().touch();
    }

    @Override
    public List<RegisteredCustomPropertySet> getMetrologyCustomPropertySets() {
        Optional<MetrologyConfiguration> metrologyConfiguration = getMetrologyConfiguration();
        if (metrologyConfiguration.isPresent()) {
            return metrologyConfiguration.get().getCustomPropertySets();
        }
        return Collections.emptyList();
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyConfigurationCustomPropertySetValues() {
        return this.getMetrologyConfigurationCustomPropertySetValues(this.clock.instant());
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getMetrologyConfigurationCustomPropertySetValues(Instant effectiveTimeStamp) {
        return getMetrologyCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .collect(Collectors.toMap(Function.identity(), rcps -> getCustomPropertySetValuesWithoutChecks(rcps, effectiveTimeStamp)));
    }

    @Override
    public void setMetrologyConfigurationCustomPropertySetValue(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue) {
        Optional<MetrologyConfiguration> metrologyConfiguration = getMetrologyConfiguration();
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
            setCustomPropertySetValuesWithoutChecks(customPropertySet, customPropertySetValue);
        } else {
            throw UsagePointCustomPropertySetValuesManageException
                    .noLinkedMetrologyConfiguration(this.thesaurus, getUsagePoint().getName());
        }
    }
}
