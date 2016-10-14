package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.metering.EndDevice;

interface DataManagementKpiCalculator {

    void calculate();

    void store(EndDevice endDevice);

}