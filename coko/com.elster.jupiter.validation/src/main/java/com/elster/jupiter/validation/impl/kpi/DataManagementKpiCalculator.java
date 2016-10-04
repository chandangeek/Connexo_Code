package com.elster.jupiter.validation.impl.kpi;

interface DataManagementKpiCalculator {

    void calculate();

    void store(long endDeviceId);

}