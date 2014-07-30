package com.elster.jupiter.kpi;

import com.google.common.base.Optional;

public interface KpiService {

    String COMPONENT_NAME = "KPI";

    KpiBuilder newKpi();

    Optional<Kpi> getKpi(long id);
}
