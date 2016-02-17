package com.elster.insight.usagepoint.data.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
import com.elster.insight.usagepoint.data.impl.exceptions.UsagePointCustomPropertySetValuesManageException;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
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

    private Map<RegisteredCustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues(List<RegisteredCustomPropertySet> customPropertySets, Instant effectiveTimeStamp) {
        return customPropertySets
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .collect(Collectors.toMap(Function.identity(), rcps -> getCustomPropertySetValuesWithoutChecks(rcps, effectiveTimeStamp), (k1, k2) -> k1));
    }

    private CustomPropertySetValues getCustomPropertySetValuesWithoutChecks(RegisteredCustomPropertySet rcps, Instant effectiveTimeStamp) {
        if (rcps.getCustomPropertySet().isVersioned()) {
            return this.customPropertySetService.getUniqueValuesFor(rcps.getCustomPropertySet(), getUsagePoint(), effectiveTimeStamp);
        }
        return this.customPropertySetService.getUniqueValuesFor(rcps.getCustomPropertySet(), getUsagePoint());
    }

    private void setCustomPropertySetValuesWithoutChecks(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue) {
        if (customPropertySet.isVersioned()) {
            this.customPropertySetService.setValuesFor(customPropertySet, getUsagePoint(), customPropertySetValue, this.clock.instant());
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
        return getCustomPropertySetValues(getMetrologyCustomPropertySets(), effectiveTimeStamp);
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
                            .noLinkedCustomPropertySetOnMetrologyConfiguration(this.thesaurus, customPropertySet.getName(),
                                    metrologyConfiguration.get().getName()));
            if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
                throw UsagePointCustomPropertySetValuesManageException
                        .customPropertySetIsNotEditableByUser(this.thesaurus, customPropertySet.getName());
            }
            setCustomPropertySetValuesWithoutChecks(customPropertySet, customPropertySetValue);
        } else {
            throw UsagePointCustomPropertySetValuesManageException
                    .noLinkedMetrologyConfiguration(this.thesaurus, getUsagePoint().getName());
        }
    }

    @Override
    public List<RegisteredCustomPropertySet> getServiceCategoryPropertySets() {
        return getUsagePoint().getServiceCategory().getCustomPropertySets();
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getServiceCategoryCustomPropertySetValues() {
        return getServiceCategoryCustomPropertySetValues(this.clock.instant());
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getServiceCategoryCustomPropertySetValues(Instant effectiveTimeStamp) {
        return getCustomPropertySetValues(getServiceCategoryPropertySets(), effectiveTimeStamp);
    }

    @Override
    public void setServiceCategoryCustomPropertySetValue(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getServiceCategoryPropertySets()
                .stream()
                .filter(rcps -> rcps.getCustomPropertySet().getId().equals(customPropertySet.getId()))
                .findAny()
                .orElseThrow(() -> UsagePointCustomPropertySetValuesManageException
                        .noLinkedCustomPropertySetOnServiceCategory(this.thesaurus, customPropertySet.getName(),
                                getUsagePoint().getServiceCategory().getName()));
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw UsagePointCustomPropertySetValuesManageException
                    .customPropertySetIsNotEditableByUser(this.thesaurus, customPropertySet.getName());
        }
        setCustomPropertySetValuesWithoutChecks(customPropertySet, customPropertySetValue);
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues() {
        return getCustomPropertySetValues(this.clock.instant());
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues(Instant effectiveTimeStamp) {
        List<RegisteredCustomPropertySet> allCustomPropertySets = new ArrayList<>(getServiceCategoryPropertySets());
        allCustomPropertySets.addAll(getMetrologyCustomPropertySets());
        return getCustomPropertySetValues(allCustomPropertySets, effectiveTimeStamp);
    }
}
