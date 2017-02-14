/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.metering.EndDevice;

interface DataManagementKpiCalculator {

    void calculate();

    void store(EndDevice endDevice);

}