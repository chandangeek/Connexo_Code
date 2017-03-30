/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.kpi.Kpi;

public interface DataValidationKpiChild {

    Kpi getChildKpi();

    void remove();

    long getDeviceId();
}