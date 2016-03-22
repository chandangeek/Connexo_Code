package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UPMetrologyConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UPMetrologyConfigurationImpl extends MetrologyConfigurationImpl implements UPMetrologyConfiguration {
    public static final String TYPE_IDENTIFIER = "U";

    private List<MetrologyConfigurationMeterRoleUsageImpl> meterRoles = new ArrayList<>();

    @Inject
    UPMetrologyConfigurationImpl(ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService) {
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
//            if (this.readingTypeRequirements
//                    .stream()
//                    .map(ReadingTypeRequirement::getMeterRole)
//                    .anyMatch(meterRole::equals)) {
//                throw CannotManageMeterRoleOnMetrologyConfigurationException.canNotDeleteMeterRoleFromMetrologyConfiguration(this.thesaurus, meterRole.getDisplayName(), getName());
//            }
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
}
