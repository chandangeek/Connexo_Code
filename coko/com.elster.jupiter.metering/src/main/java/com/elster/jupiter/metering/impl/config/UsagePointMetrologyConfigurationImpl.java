package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UnsatisfiedReadingTypeRequirements;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.metering.impl.search.UsagePointRequirementsSearchDomain;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.Pair;

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

    private final UsagePointRequirementsSearchDomain searchDomain;

    private List<MetrologyConfigurationMeterRoleUsageImpl> meterRoles = new ArrayList<>();
    private List<ReadingTypeRequirementMeterRoleUsage> requirementToRoleUsages = new ArrayList<>();
    private List<UsagePointRequirement> usagePointRequirements = new ArrayList<>();

    @Inject
    UsagePointMetrologyConfigurationImpl(ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService, UsagePointRequirementsSearchDomain searchDomain) {
        super(metrologyConfigurationService, eventService);
        this.searchDomain = searchDomain;
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

    @Override
    public void validateMeterCapabilities(List<Pair<MeterRole, Meter>> meters) {
        List<ReadingTypeRequirement> mandatoryReadingTypeRequirements = getMandatoryReadingTypeRequirements();
        boolean hasUnsatisfiedReadingTypeRequirements = false;
        UnsatisfiedReadingTypeRequirements ex = new UnsatisfiedReadingTypeRequirements(getMetrologyConfigurationService().getThesaurus(), this);
        for (Pair<MeterRole, Meter> pair : meters) {
            List<ReadingTypeRequirement> unmatchedRequirements = getUnmatchedMeterReadingTypeRequirements(this, mandatoryReadingTypeRequirements, pair);
            if (!unmatchedRequirements.isEmpty()) {
                hasUnsatisfiedReadingTypeRequirements = true;
                ex.addUnsatisfiedReadingTypeRequirements(pair.getFirst(), unmatchedRequirements);
            }
        }
        if (hasUnsatisfiedReadingTypeRequirements) {
            throw ex;
        }

    }


    private List<ReadingTypeRequirement> getMandatoryReadingTypeRequirements() {
        ReadingTypeRequirementChecker requirementChecker = new ReadingTypeRequirementChecker();
        getContracts()
                .stream()
                .filter(MetrologyContract::isMandatory)
                .flatMap(contract -> contract.getDeliverables().stream())
                .forEach(deliverable -> deliverable.getFormula().getExpressionNode().accept(requirementChecker));
        return requirementChecker.getReadingTypeRequirements();
    }

    private List<ReadingTypeRequirement> getUnmatchedMeterReadingTypeRequirements(UsagePointMetrologyConfiguration metrologyConfiguration, List<ReadingTypeRequirement> mandatoryReadingTypeRequirements, Pair<MeterRole, Meter> pair) {
        List<ReadingType> readingTypesOnMeter = new ArrayList<>();
        pair.getLast().getHeadEndInterface()
                .map(headEndInterface -> headEndInterface.getCapabilities(pair.getLast()))
                .ifPresent(endDeviceCapabilities -> readingTypesOnMeter.addAll(endDeviceCapabilities.getConfiguredReadingTypes()));
        return metrologyConfiguration.getRequirements(pair.getFirst())
                .stream()
                .filter(mandatoryReadingTypeRequirements::contains)
                .filter(requirement -> !readingTypesOnMeter.stream().anyMatch(requirement::matches))
                .collect(Collectors.toList());
    }
}
