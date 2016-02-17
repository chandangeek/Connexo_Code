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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private CustomPropertySetValues getCustomPropertySetValuesWithoutChecks(RegisteredCustomPropertySet rcps,
                                                                            Instant effectiveTimeStamp) {
        CustomPropertySet<UsagePoint, ?> customPropertySet = rcps.getCustomPropertySet();
        CustomPropertySetValues values;
        if (customPropertySet.isVersioned()) {
            values = this.customPropertySetService.getUniqueValuesFor(customPropertySet, getUsagePoint(), effectiveTimeStamp);
            if (values.isEmpty() && !this.customPropertySetService
                    .getUniqueValuesEntityFor(customPropertySet, getUsagePoint(), effectiveTimeStamp).isPresent()) {
                values = null;
            }
        } else {
            values = this.customPropertySetService.getUniqueValuesFor(customPropertySet, getUsagePoint());
            if (values.isEmpty() && !this.customPropertySetService.getUniqueValuesEntityFor(customPropertySet, getUsagePoint()).isPresent()) {
                values = null;
            }
        }
        return values;
    }

    private void setCustomPropertySetValuesWithoutChecks(CustomPropertySet<UsagePoint, ?> customPropertySet,
                                                         CustomPropertySetValues customPropertySetValue) {
        if (customPropertySet.isVersioned()) {
            this.customPropertySetService.setValuesFor(customPropertySet, getUsagePoint(), customPropertySetValue, this.clock.instant());
        } else {
            this.customPropertySetService.setValuesFor(customPropertySet, getUsagePoint(), customPropertySetValue);
        }
        getUsagePoint().touch();
    }

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySetsOnMetrologyConfiguration() {
        Optional<MetrologyConfiguration> metrologyConfiguration = getMetrologyConfiguration();
        if (metrologyConfiguration.isPresent()) {
            return metrologyConfiguration.get().getCustomPropertySets();
        }
        return Collections.emptyList();
    }

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySetsOnServiceCategory() {
        return getUsagePoint().getServiceCategory().getCustomPropertySets();
    }

    @Override
    public List<RegisteredCustomPropertySet> getAllCustomPropertySets() {
        List<RegisteredCustomPropertySet> mcCPS = getCustomPropertySetsOnMetrologyConfiguration();
        List<RegisteredCustomPropertySet> scCPS = getCustomPropertySetsOnServiceCategory();
        List<RegisteredCustomPropertySet> all = new ArrayList<>(mcCPS.size() + scCPS.size());
        all.addAll(mcCPS);
        all.addAll(scCPS);
        return all;
    }

    @Override
    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues(
            List<RegisteredCustomPropertySet> registeredCustomPropertySets) {
        return getCustomPropertySetValues(registeredCustomPropertySets, this.clock.instant());
    }

    public Map<RegisteredCustomPropertySet, CustomPropertySetValues> getCustomPropertySetValues(
            List<RegisteredCustomPropertySet> registeredCustomPropertySets, Instant effectiveTimeStamp) {
        if (registeredCustomPropertySets == null || registeredCustomPropertySets.isEmpty()) {
            return Collections.emptyMap();
        }
        if (effectiveTimeStamp == null) {
            effectiveTimeStamp = this.clock.instant();
        }
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> valueMap = new HashMap<>();
        for (RegisteredCustomPropertySet registeredCustomPropertySet : registeredCustomPropertySets) {
            if (!UsagePoint.class.isAssignableFrom(registeredCustomPropertySet.getCustomPropertySet().getDomainClass())) {
                throw UsagePointCustomPropertySetValuesManageException
                        .badDomainType(this.thesaurus, registeredCustomPropertySet.getCustomPropertySet().getName());
            }
            if (!registeredCustomPropertySet.isViewableByCurrentUser()) {
                continue;
            }
            valueMap.put(registeredCustomPropertySet,
                    getCustomPropertySetValuesWithoutChecks(registeredCustomPropertySet, effectiveTimeStamp));
        }
        return valueMap;
    }

    @Override
    public void setCustomPropertySetValue(CustomPropertySet customPropertySet, CustomPropertySetValues customPropertySetValue) {
        // TODO maybe it will be better to cache all sets here
        RegisteredCustomPropertySet registeredCustomPropertySet = getAllCustomPropertySets()
                .stream()
                .filter(rcps -> rcps.getCustomPropertySet().getId().equals(customPropertySet.getId()))
                .findAny()
                .orElseThrow(() -> UsagePointCustomPropertySetValuesManageException
                        .noLinkedCustomPropertySet(this.thesaurus, customPropertySet.getName()));
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw UsagePointCustomPropertySetValuesManageException
                    .customPropertySetIsNotEditableByUser(this.thesaurus, customPropertySet.getName());
        }
        setCustomPropertySetValuesWithoutChecks(customPropertySet, customPropertySetValue);
    }
}
