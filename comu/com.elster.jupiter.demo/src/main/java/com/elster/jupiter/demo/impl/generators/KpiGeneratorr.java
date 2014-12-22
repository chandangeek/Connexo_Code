package com.elster.jupiter.demo.impl.generators;

import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiBuilder;
import com.elster.jupiter.kpi.KpiService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class KpiGeneratorr {

    private final KpiService kpiService;
    private final Store store;

    private String name;

    @Inject
    public KpiGeneratorr(KpiService kpiService, Store store) {
        this.kpiService = kpiService;
        this.store = store;
    }

    public KpiGeneratorr withName(String name){
        this.name = name;
        return this;
    }

    public void create(){
        System.out.println("==> Creating KPI '" + name + "'... ");
        KpiBuilder builder = kpiService.newKpi();
        builder.named(name);
        builder.member().withTargetSetAt(new BigDecimal(80));
        builder.interval(Duration.of(15, ChronoUnit.MINUTES));
        final Kpi kpi = builder.build();
        kpi.save();
        store.add(Kpi.class, kpi);
    }
}
