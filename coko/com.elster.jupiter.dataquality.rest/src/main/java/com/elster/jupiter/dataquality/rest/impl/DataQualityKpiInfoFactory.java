/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;

import javax.inject.Inject;
import java.time.temporal.TemporalAmount;

import static com.elster.jupiter.dataquality.rest.impl.DataQualityKpiInfo.DeviceDataQualityKpiInfo;
import static com.elster.jupiter.dataquality.rest.impl.DataQualityKpiInfo.UsagePointDataQualityKpiInfo;

public class DataQualityKpiInfoFactory {

    private final MeteringGroupsService meteringGroupsService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final DataQualityKpiService dataQualityKpiService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DataQualityKpiInfoFactory(MeteringGroupsService meteringGroupsService, MetrologyConfigurationService metrologyConfigurationService,
                                     ExceptionFactory exceptionFactory, DataQualityKpiService dataQualityKpiService) {
        this.meteringGroupsService = meteringGroupsService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.exceptionFactory = exceptionFactory;
        this.dataQualityKpiService = dataQualityKpiService;
    }

    public DeviceDataQualityKpi createNewKpi(DeviceDataQualityKpiInfo info) {
        EndDeviceGroup endDeviceGroup = findEndDeviceGroupOrThrowException(info.deviceGroup);
        TemporalAmount calculationFrequency = extractFromInfo(info.frequency);
        return dataQualityKpiService.newDataQualityKpi(endDeviceGroup, calculationFrequency);
    }

    private EndDeviceGroup findEndDeviceGroupOrThrowException(LongIdWithNameInfo group) {
        if (group != null && group.id != null) {
            return meteringGroupsService.findEndDeviceGroup(group.id)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_ENDDEVICE_GROUP, group.id));
        }
        return null;
    }

    public UsagePointDataQualityKpi createNewKpi(UsagePointDataQualityKpiInfo info) {
        UsagePointGroup usagePointGroup = findUsagePointGroupOrThrowException(info.usagePointGroup);
        MetrologyPurpose metrologyPurpose = findMetrologyPurposeOrThrowException(info.metrologyPurpose);
        TemporalAmount calculationFrequency = extractFromInfo(info.frequency);
        return dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, calculationFrequency);
    }

    private TemporalAmount extractFromInfo(TemporalExpressionInfo frequency) {
        if (frequency != null && frequency.every != null && frequency.every.asTimeDuration() != null) {
            return frequency.every.asTimeDuration().asTemporalAmount();
        }
        return null;
    }

    private UsagePointGroup findUsagePointGroupOrThrowException(LongIdWithNameInfo group) {
        if (group != null && group.id != null) {
            return meteringGroupsService.findUsagePointGroup(group.id)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_USAGEPOINT_GROUP, group.id));
        }
        return null;
    }

    private MetrologyPurpose findMetrologyPurposeOrThrowException(LongIdWithNameInfo metrologyPurpose) {
        if (metrologyPurpose != null && metrologyPurpose.id != null) {
            return metrologyConfigurationService.findMetrologyPurpose(metrologyPurpose.id)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_METROLOGY_PURPOSE, metrologyPurpose.id));
        }
        return null;
    }

    public DeviceDataQualityKpiInfo from(DeviceDataQualityKpi kpi) {
        DeviceDataQualityKpiInfo kpiInfo = new DeviceDataQualityKpiInfo();
        setCommonFields(kpi, kpiInfo);
        kpiInfo.deviceGroup = new LongIdWithNameInfo(kpi.getDeviceGroup());
        return kpiInfo;
    }

    public UsagePointDataQualityKpiInfo from(UsagePointDataQualityKpi kpi) {
        UsagePointDataQualityKpiInfo kpiInfo = new UsagePointDataQualityKpiInfo();
        setCommonFields(kpi, kpiInfo);
        kpiInfo.usagePointGroup = new LongIdWithNameInfo(kpi.getUsagePointGroup());
        kpiInfo.metrologyPurpose = new LongIdWithNameInfo(kpi.getMetrologyPurpose());
        return kpiInfo;
    }

    private void setCommonFields(DataQualityKpi kpi, DataQualityKpiInfo kpiInfo) {
        kpiInfo.id = kpi.getId();
        kpiInfo.frequency = kpi.getFrequency() != null ? TemporalExpressionInfo.from(kpi.getFrequency()) : null;
        kpiInfo.version = kpi.getVersion();
        kpi.getLatestCalculation().ifPresent(target -> kpiInfo.latestCalculationDate = target);
    }
}
