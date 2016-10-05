package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;

import javax.inject.Inject;

public class ResourceHelper {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public ResourceHelper(MeteringService meteringService,
                          ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          MetrologyConfigurationService metrologyConfigurationService) {
        super();
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
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
        return conflictFactory.conflict().withMessageBody(MessageSeeds.USAGE_POINT_LINKED_EXCEPTION_MSG, name).withMessageTitle(MessageSeeds.USAGE_POINT_LINKED_EXCEPTION, name).build();
    }

    public MetrologyContract findMetrologyContractOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, long contractId) {
        return effectiveMC.getMetrologyConfiguration().getContracts().stream()
                .filter(contract -> contract.getId() == contractId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.METROLOGYCONTRACT_IS_NOT_LINKED_TO_USAGEPOINT, contractId, effectiveMC.getUsagePoint().getName()));
    }

    public ReadingTypeDeliverable findReadingTypeDeliverableOrThrowException(MetrologyContract metrologyContract, long outputId, String usagePointName) {
        return metrologyContract.getDeliverables().stream()
                .filter(deliverable -> deliverable.getId() == outputId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_OUTPUT_FOR_USAGEPOINT, usagePointName, outputId));
    }
}
