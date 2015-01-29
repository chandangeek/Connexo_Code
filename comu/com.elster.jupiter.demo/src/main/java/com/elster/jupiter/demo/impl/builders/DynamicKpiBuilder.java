package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

public class DynamicKpiBuilder implements Builder<DataCollectionKpi> {
    private final DataCollectionKpiService dataCollectionKpiService;

    private EndDeviceGroup group;

    @Inject
    public DynamicKpiBuilder(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    public DynamicKpiBuilder withGroup(EndDeviceGroup group){
        this.group = group;
        return this;
    }

    @Override
    public Optional<DataCollectionKpi> find() {
        return dataCollectionKpiService.findDataCollectionKpi(this.group);
    }

    @Override
    public DataCollectionKpi create() {
        Log.write(this);
        DataCollectionKpiService.DataCollectionKpiBuilder kpiBuilder = dataCollectionKpiService.newDataCollectionKpi(group);
        kpiBuilder.calculateComTaskExecutionKpi(Duration.ofMinutes(5)).expectingAsMinimum(new BigDecimal(95));
        kpiBuilder.calculateConnectionSetupKpi(Duration.ofMinutes(5)).expectingAsMinimum(new BigDecimal(95));
        DataCollectionKpi kpi = kpiBuilder.save();
        return kpi;
    }
}
