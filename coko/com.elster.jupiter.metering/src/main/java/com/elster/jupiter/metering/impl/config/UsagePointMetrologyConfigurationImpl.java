package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.metering.impl.search.UsagePointRequirementsSearchDomain;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyValue;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsagePointMetrologyConfigurationImpl extends MetrologyConfigurationImpl implements UsagePointMetrologyConfiguration {
    public static final String TYPE_IDENTIFIER = "U";

    private final SearchDomain searchDomain;

    private List<MetrologyConfigurationMeterRoleUsageImpl> meterRoles = new ArrayList<>();
    private List<ReadingTypeRequirementMeterRoleUsage> requirementToRoleUsages = new ArrayList<>();
    private List<UsagePointRequirement> usagePointRequirements = new ArrayList<>();

    @Inject
    UsagePointMetrologyConfigurationImpl(ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService, CustomPropertySetService customPropertySetService, UsagePointRequirementsSearchDomain searchDomain, SearchService searchService) {
        super(metrologyConfigurationService, eventService, customPropertySetService);
        this.searchDomain = searchService.findDomain(searchDomain.getId()).get();
    }

    @Override
    public void addMeterRole(MeterRole meterRole) {
        if (!getMeterRoles().contains(meterRole)) {
            if (!getServiceCategory().getMeterRoles().contains(meterRole)
                    && !meterRole.equals(super.getMetrologyConfigurationService().findMeterRole(DefaultMeterRole.DEFAULT.getKey()).orElse(null))) {
                throw CannotManageMeterRoleOnMetrologyConfigurationException
                        .canNotAddMeterRoleWhichIsNotAssignedToServiceCategory(getMetrologyConfigurationService().getThesaurus(),
                                meterRole.getDisplayName(), getServiceCategory().getDisplayName());
            }
            MetrologyConfigurationMeterRoleUsageImpl usage = getMetrologyConfigurationService().getDataModel()
                    .getInstance(MetrologyConfigurationMeterRoleUsageImpl.class)
                    .init(this, meterRole);
            this.meterRoles.add(usage);
            touch();
        }
    }

    @Override
    public void removeMeterRole(MeterRole meterRole) {
        Optional<MetrologyConfigurationMeterRoleUsageImpl> meterRoleUsage = this.meterRoles
                .stream()
                .filter(usage -> usage.getMeterRole().equals(meterRole))
                .findAny();
        if (meterRoleUsage.isPresent()) {
            if (this.requirementToRoleUsages
                    .stream()
                    .map(ReadingTypeRequirementMeterRoleUsage::getMeterRole)
                    .anyMatch(meterRole::equals)) {
                throw CannotManageMeterRoleOnMetrologyConfigurationException.canNotDeleteMeterRoleFromMetrologyConfiguration(
                        getMetrologyConfigurationService().getThesaurus(), meterRole.getDisplayName(), getName());
            }
            this.meterRoles.remove(meterRoleUsage.get());
            touch();
        }
    }

    @Override
    public List<MeterRole> getMeterRoles() {
        return this.meterRoles
                .stream()
                .map(MetrologyConfigurationMeterRoleUsageImpl::getMeterRole)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadingTypeRequirement> getRequirements(MeterRole meterRole) {
        return this.requirementToRoleUsages
                .stream()
                .filter(usage -> usage.getMeterRole().equals(meterRole))
                .map(ReadingTypeRequirementMeterRoleUsage::getReadingTypeRequirement)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MeterRole> getMeterRoleFor(ReadingTypeRequirement readingTypeRequirement) {
        return this.requirementToRoleUsages
                .stream()
                .filter(usage -> usage.getReadingTypeRequirement().equals(readingTypeRequirement))
                .findAny()
                .map(ReadingTypeRequirementMeterRoleUsage::getMeterRole);
    }

    void addReadingTypeRequirementMeterRoleUsage(ReadingTypeRequirementMeterRoleUsage usage) {
        Save.CREATE.validate(getMetrologyConfigurationService().getDataModel(), usage);
        this.requirementToRoleUsages.add(usage);
    }

    @Override
    public UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder newReadingTypeRequirement(String name) {
        return new UsagePointMetrologyConfigurationReadingTypeRequirementBuilderImpl(getMetrologyConfigurationService(), this, name);
    }

    @Override
    public UsagePointRequirement addUsagePointRequirement(SearchablePropertyValue.ValueBean valueBean) {
        Optional<UsagePointRequirementImpl> existedUsagePointRequirement = getUsagePointRequirements()
                .stream()
                .filter(requirement -> requirement.getSearchableProperty().getName().equals(valueBean.propertyName))
                .findAny()
                .map(UsagePointRequirementImpl.class::cast);
        UsagePointRequirementImpl usagePointRequirement = existedUsagePointRequirement
                .orElseGet(() -> getMetrologyConfigurationService().getDataModel().getInstance(UsagePointRequirementImpl.class))
                .init(this, valueBean);
        Save.CREATE.validate(getMetrologyConfigurationService().getDataModel(), usagePointRequirement);
        if (!existedUsagePointRequirement.isPresent()) {
            this.usagePointRequirements.add(usagePointRequirement);
        } else {
            getMetrologyConfigurationService().getDataModel().update(usagePointRequirement);
        }
        return usagePointRequirement;
    }

    @Override
    public void removeUsagePointRequirement(UsagePointRequirement requirement) {
        if (this.usagePointRequirements.remove(requirement)) {
            touch();
        }
    }

    @Override
    public List<UsagePointRequirement> getUsagePointRequirements() {
        return Collections.unmodifiableList(this.usagePointRequirements);
    }

    public List<SearchablePropertyValue> getUsagePointRequirementSearchableProperties() {
        Map<String, SearchablePropertyValue.ValueBean> searchableProperties = getUsagePointRequirements().stream()
                .map(UsagePointRequirement::toValueBean)
                .collect(Collectors.toMap(req -> req.propertyName, Function.identity()));
        return this.searchDomain
                .getPropertiesValues(property -> new SearchablePropertyValue(property, searchableProperties.get(property.getName())));
    }
}
