package com.elster.jupiter.validation.kpi;


import com.elster.jupiter.kpi.Kpi;

public interface DataValidationKpiChild {

    Kpi getChildKpi();

    DataValidationKpi getDataValidationKpi();

    void remove();
}
