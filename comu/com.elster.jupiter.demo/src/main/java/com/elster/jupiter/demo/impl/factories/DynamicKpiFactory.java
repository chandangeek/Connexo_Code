package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Duration;

public class DynamicKpiFactory implements Factory<DataCollectionKpi>{
    private final DataCollectionKpiService dataCollectionKpiService;
    private final KpiService kpiService;
    private final Store store;

    private EndDeviceGroup group;

    @Inject
    public DynamicKpiFactory(KpiService kpiService, DataCollectionKpiService dataCollectionKpiService, Store store) {
        this.dataCollectionKpiService = dataCollectionKpiService;
        this.kpiService = kpiService;
        this.store = store;
    }

    public DynamicKpiFactory withGroup(EndDeviceGroup group){
        this.group = group;
        return this;
    }

    public DataCollectionKpi get(){
        Log.write(this);
        DataCollectionKpiService.DataCollectionKpiBuilder kpiBuilder = dataCollectionKpiService.newDataCollectionKpi(group);
        kpiBuilder.calculateComTaskExecutionKpi(Duration.ofMinutes(15)).expectingAsMinimum(new BigDecimal(80));
        kpiBuilder.calculateConnectionSetupKpi(Duration.ofMinutes(15)).expectingAsMinimum(new BigDecimal(80));
        DataCollectionKpi kpi = kpiBuilder.save();
        store.add(DataCollectionKpi.class, kpi);
        return kpi;
    }
}
