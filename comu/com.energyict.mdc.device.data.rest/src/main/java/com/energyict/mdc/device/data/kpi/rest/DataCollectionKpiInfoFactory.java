package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import java.util.stream.Stream;

/**
 * Created by bvn on 12/12/14.
 */
public class DataCollectionKpiInfoFactory {

    public DataCollectionKpiInfo from(DataCollectionKpi kpi) {
        DataCollectionKpiInfo kpiInfo = new DataCollectionKpiInfo();
        kpiInfo.id = kpi.getId();
        kpiInfo.endDeviceGroup = new LongIdWithNameInfo(kpi.getDeviceGroup().getId(), kpi.getDeviceGroup().getName());
        kpiInfo.displayRange = new TimeDurationInfo(kpi.getDisplayRange());
        Stream.of(kpi.comTaskExecutionKpiCalculationIntervalLength(),kpi.connectionSetupKpiCalculationIntervalLength()).
                flatMap(Functions.asStream()).
                findFirst().
                ifPresent(temporalAmount -> kpiInfo.frequency = TemporalExpressionInfo.from(temporalAmount));
        kpi.getStaticCommunicationKpiTarget().ifPresent(target -> kpiInfo.communicationTarget = target);
        kpi.getStaticConnectionKpiTarget().ifPresent(target -> kpiInfo.connectionTarget = target);
        kpi.getLatestCalculation().ifPresent(target -> kpiInfo.latestCalculationDate = target);
        return kpiInfo;
    }

}
