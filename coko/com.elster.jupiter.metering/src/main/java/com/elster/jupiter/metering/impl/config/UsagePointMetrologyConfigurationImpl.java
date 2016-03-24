package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsagePointMetrologyConfigurationImpl extends MetrologyConfigurationImpl implements UsagePointMetrologyConfiguration {
    public static final String TYPE_IDENTIFIER = "U";

    private List<MetrologyConfigurationMeterRoleUsageImpl> meterRoles = new ArrayList<>();
    private List<UsagePointMetrologyConfigurationRequirementRoleReference> requirementToRoleReferences = new ArrayList<>();

    @Inject
    UsagePointMetrologyConfigurationImpl(ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService) {
        super(metrologyConfigurationService, eventService);
    }

    @Override
    public void addMeterRole(MeterRole meterRole) {
        if (!getMeterRoles().contains(meterRole)) {
            if (!getServiceCategory().getMeterRoles().contains(meterRole)) {
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
            if (this.requirementToRoleReferences
                    .stream()
                    .map(UsagePointMetrologyConfigurationRequirementRoleReference::getMeterRole)
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

    void addReadingTypeRequirementToMeterRoleReference(UsagePointMetrologyConfigurationRequirementRoleReference reference) {
        Save.CREATE.validate(getMetrologyConfigurationService().getDataModel(), reference);
        this.requirementToRoleReferences.add(reference);
    }

    @Override
    public UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder addReadingTypeRequirement(String name) {
        return new UsagePointMetrologyConfigurationReadingTypeRequirementBuilderImpl(getMetrologyConfigurationService(), this).withName(name);
    }

}
