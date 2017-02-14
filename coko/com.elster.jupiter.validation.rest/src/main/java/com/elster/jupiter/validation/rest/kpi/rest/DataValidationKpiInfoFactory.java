/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest.kpi.rest;


import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;
import com.elster.jupiter.validation.kpi.EndDeviceDataQuality;
import com.elster.jupiter.validation.kpi.UsagePointDataQuality;
import com.elster.jupiter.validation.rest.impl.MessageSeeds;

import javax.inject.Inject;

public class DataValidationKpiInfoFactory {

    private final MeteringGroupsService meteringGroupsService;
    private final ExceptionFactory exceptionFactory;
    private final DataValidationKpiService dataValidationKpiService;

    @Inject
    public DataValidationKpiInfoFactory(MeteringGroupsService meteringGroupsService, ExceptionFactory exceptionFactory, DataValidationKpiService dataValidationKpiService) {
        this.meteringGroupsService = meteringGroupsService;
        this.exceptionFactory = exceptionFactory;
        this.dataValidationKpiService = dataValidationKpiService;
    }

    public UsagePointDataQuality createNewKpi(DataQualityKpiInfo.UsagePointDataQualityKpiInfo usagePointDataValidationKpi) {
        LongIdWithNameInfo group = usagePointDataValidationKpi.usagePointGroup;
        if (group != null && group.id != null) {
            UsagePointGroup usagePointGroup = meteringGroupsService.findUsagePointGroup(group.id)
                        .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_GROUP, group.id));
            DataValidationKpiService.UsagePointDataValidationKpiBuilder builder = dataValidationKpiService.newUsagePointDataValidationKpi(usagePointGroup);
            TemporalExpressionInfo frequency = usagePointDataValidationKpi.frequency;
            if (frequency != null && frequency.every != null && frequency.every.asTimeDuration() != null) {
                builder.frequency(frequency.every.asTimeDuration().asTemporalAmount());
            }
            if (usagePointDataValidationKpi.purpose != null) {
                builder.purpose(usagePointDataValidationKpi.purpose);
            }

            return builder.build();
        }

        return null;
    }

    public EndDeviceDataQuality createNewKpi(DataQualityKpiInfo.DeviceDataQualityKpiInfo deviceDataQualityKpiInfo) {
        LongIdWithNameInfo group = deviceDataQualityKpiInfo.deviceGroup;
        if (group != null && group.id != null) {
            EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroup(group.id)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_GROUP, group.id));
            DataValidationKpiService.DeviceDataValidationKpiBuilder builder = dataValidationKpiService.newDeviceDataValidationKpi(endDeviceGroup);
            TemporalExpressionInfo frequency = deviceDataQualityKpiInfo.frequency;
            if (frequency != null && frequency.every != null && frequency.every.asTimeDuration() != null) {
                builder.frequency(frequency.every.asTimeDuration().asTemporalAmount());
            }

            return builder.build();
        }

        return null;
    }

    public DataQualityKpiInfo from(DataValidationKpi dataValidationKpi) {
        if (dataValidationKpi instanceof UsagePointDataQuality) {
            return from((UsagePointDataQuality) dataValidationKpi);
        } else if (dataValidationKpi instanceof EndDeviceDataQuality){
            return from((EndDeviceDataQuality) dataValidationKpi);
        }

        return null;
    }

    private DataQualityKpiInfo.DeviceDataQualityKpiInfo from(EndDeviceDataQuality kpi) {
        DataQualityKpiInfo.DeviceDataQualityKpiInfo kpiInfo = new DataQualityKpiInfo.DeviceDataQualityKpiInfo();
        kpiInfo.id = kpi.getId();
        kpiInfo.deviceGroup = new LongIdWithNameInfo(kpi.getDeviceGroup().getId(), kpi.getDeviceGroup().getName());
        kpiInfo.frequency = kpi.getFrequency() != null ? TemporalExpressionInfo.from(kpi.getFrequency()) : null;
        kpiInfo.version = kpi.getVersion();
        kpi.getLatestCalculation().ifPresent(target -> kpiInfo.latestCalculationDate = target);
        return kpiInfo;
    }

    private DataQualityKpiInfo.UsagePointDataQualityKpiInfo from(UsagePointDataQuality kpi) {
        DataQualityKpiInfo.UsagePointDataQualityKpiInfo kpiInfo = new DataQualityKpiInfo.UsagePointDataQualityKpiInfo();
        kpiInfo.id = kpi.getId();
        kpiInfo.usagePointGroup = new LongIdWithNameInfo(kpi.getUsagePointGroup().getId(), kpi.getUsagePointGroup()
                .getName());
        kpiInfo.frequency = kpi.getFrequency() != null ? TemporalExpressionInfo.from(kpi.getFrequency()) : null;
        kpiInfo.version = kpi.getVersion();
        kpi.getLatestCalculation().ifPresent(target -> kpiInfo.latestCalculationDate = target);
        return kpiInfo;
    }
}
