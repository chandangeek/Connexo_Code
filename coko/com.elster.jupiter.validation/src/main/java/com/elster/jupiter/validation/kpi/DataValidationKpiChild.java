package com.elster.jupiter.validation.kpi;

import com.elster.jupiter.kpi.Kpi;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DataValidationKpiChild {

    Kpi getChildKpi();

    DataValidationKpi getDataValidationKpi();

    void remove();

}