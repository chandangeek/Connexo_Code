package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.util.Pair;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceHelper {

    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Clock clock;

    @Inject
    public ResourceHelper(MeteringService meteringService,
                          MeteringGroupsService meteringGroupsService,
                          ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          MetrologyConfigurationService metrologyConfigurationService, Clock clock) {
        super();
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.clock = clock;
    }

    public MeterRole findMeterRoleOrThrowException(String key) {
        return metrologyConfigurationService.findMeterRole(key)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METER_ROLE_FOR_KEY, key));
    }

    public Meter findMeterByNameOrThrowException(String name) {
        return meteringService.findMeterByName(name)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_DEVICE_WITH_NAME, name));
    }

    public UsagePoint findUsagePointByNameOrThrowException(String name) {
        return meteringService.findUsagePointByName(name)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_USAGE_POINT_WITH_NAME, name));
    }

    public UsagePoint findUsagePointByIdOrThrowException(long id) {
        return meteringService.findUsagePointById(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_USAGE_POINT_FOR_ID, id));
    }

    public UsagePoint findAndLockUsagePointByNameOrThrowException(String name, long version) {
        UsagePoint up = findUsagePointByNameOrThrowException(name);
        return lockUsagePointOrThrowException(up.getId(), version, up.getName());
    }

    public EffectiveMetrologyConfigurationOnUsagePoint findEffectiveMetrologyConfigurationByUsagePointOrThrowException(UsagePoint usagePoint) {
        return usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT, usagePoint.getName()));
    }

    public UsagePointMetrologyConfiguration findAndLockActiveUsagePointMetrologyConfigurationOrThrowException(long id, long version) {
        return metrologyConfigurationService
                .findAndLockMetrologyConfiguration(id, version)
                .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(this::isActiveMetrologyConfigurationOrThrowException)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_ID, id));
    }

    private MetrologyConfiguration isActiveMetrologyConfigurationOrThrowException(MetrologyConfiguration metrologyConfiguration) {
        if (metrologyConfiguration.isActive()) {
            return metrologyConfiguration;
        }
        throw exceptionFactory.newException(MessageSeeds.NOT_POSSIBLE_TO_LINK_INACTIVE_METROLOGY_CONFIGURATION_TO_USAGE_POINT, metrologyConfiguration.getName());
    }

    public UsagePoint lockUsagePointOrThrowException(UsagePointInfo info) {
        return lockUsagePointOrThrowException(info.id, info.version, info.name);
    }

    public UsagePoint lockUsagePointOrThrowException(long id, long version, String name) {
        return meteringService.findAndLockUsagePointByIdAndVersion(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(name)
                        .withActualVersion(() -> meteringService.findUsagePointById(id).map(UsagePoint::getVersion).orElse(null))
                        .supplier());
    }

    public ConcurrentModificationException usagePointAlreadyLinkedException(String name) {
        return conflictFactory.conflict()
                .withMessageBody(MessageSeeds.USAGE_POINT_LINKED_EXCEPTION_MSG, name)
                .withMessageTitle(MessageSeeds.USAGE_POINT_LINKED_EXCEPTION, name)
                .build();
    }

    public Optional<MetrologyPurpose> findMetrologyPurpose(long id) {
        return metrologyConfigurationService.findMetrologyPurpose(id);
    }

    public MetrologyPurpose findMetrologyPurposeOrThrowException(long id) {
        return metrologyConfigurationService.findMetrologyPurpose(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_METROLOGY_PURPOSE, id));
    }

    public MetrologyContract findMetrologyContractOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, long contractId) {
        return effectiveMC.getMetrologyConfiguration().getContracts().stream()
                .filter(contract -> contract.getId() == contractId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.METROLOGYCONTRACT_IS_NOT_LINKED_TO_USAGEPOINT, contractId, effectiveMC.getUsagePoint().getName()));
    }

    public MetrologyContract findMetrologyContractOrThrowException(UsagePoint usagePoint, long contractId) {
        return metrologyConfigurationService.findMetrologyContract(contractId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.METROLOGYCONTRACT_IS_NOT_LINKED_TO_USAGEPOINT, contractId, usagePoint.getName()));
    }

    public ReadingTypeDeliverable findReadingTypeDeliverableOrThrowException(MetrologyContract metrologyContract, long outputId, String usagePointName) {
        return metrologyContract.getDeliverables().stream()
                .filter(deliverable -> deliverable.getId() == outputId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_OUTPUT_FOR_USAGEPOINT, usagePointName, outputId));
    }

    public UsagePointGroup findUsagePointGroupOrThrowException(long id) {
        return meteringGroupsService.findUsagePointGroup(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    public UsagePointGroup lockUsagePointGroupOrThrowException(UsagePointGroupInfo info) {
        return meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> meteringGroupsService.findUsagePointGroup(info.id)
                                .map(UsagePointGroup::getVersion)
                                .orElse(null))
                        .supplier());
    }

    public void checkMeterRequirements(UsagePoint usagePoint, MetrologyContract metrologyContract) {
        List<MeterActivation> meterActivations = usagePoint.getMeterActivations(clock.instant());
        EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationOnUsagePoint = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT, usagePoint.getName()));
        List<MeterRole> meterRolesOfMetrologyConfiguration = metrologyConfigurationOnUsagePoint.getMetrologyConfiguration().getMeterRoles();
        List<Pair<MeterRole, Meter>> metersInRoles = meterActivations.stream()
                .filter(meterActivation -> meterActivation.getMeterRole().map(meterRolesOfMetrologyConfiguration::contains).orElse(false))
                .map(meterActivation -> Pair.of(meterActivation.getMeterRole().get(), meterActivation.getMeter().get()))
                .collect(Collectors.toList());
        for (Pair<MeterRole, Meter> pair : metersInRoles) {
            MeterRole meterRole = pair.getFirst();
            Meter meter = pair.getLast();
            Set<ReadingTypeRequirement> requirements = metrologyContract.getRequirements().stream()
                    .filter(readingTypeRequirement -> meterRole.equals(metrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                            .getMeterRoleFor(readingTypeRequirement)
                            .orElse(null)))
                    .collect(Collectors.toSet());
            Set<ReadingTypeRequirement> unsatisfiedRequirements = getUnsatisfiedReadingTypeRequirementsOfMeter(requirements, meter);
            if (!unsatisfiedRequirements.isEmpty()) {
                throw exceptionFactory.newException(MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS);
            }
        }
    }

    private Set<ReadingTypeRequirement> getUnsatisfiedReadingTypeRequirementsOfMeter(Set<ReadingTypeRequirement> requirements, Meter meter) {
        List<ReadingType> meterProvidedReadingTypes = meter.getHeadEndInterface()
                .map(headEndInterface -> headEndInterface.getCapabilities(meter))
                .map(EndDeviceCapabilities::getConfiguredReadingTypes)
                .orElse(Collections.emptyList());
        return requirements.stream()
                .filter(requirement -> !meterProvidedReadingTypes.stream().anyMatch(requirement::matches))
                .collect(Collectors.toSet());
    }

    public IdWithNameInfo getApplicationInfo(QualityCodeSystem system) {
        switch (system) {
            case MDC:
                return new IdWithNameInfo(system.name(), "MultiSense");
            case MDM:
                return new IdWithNameInfo(system.name(), "Insight");
            default:
                return new IdWithNameInfo(system.name(), system.name());
        }
    }
}
