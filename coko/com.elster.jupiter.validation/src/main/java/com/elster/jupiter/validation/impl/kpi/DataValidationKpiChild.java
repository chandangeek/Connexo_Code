/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.kpi.Kpi;

public interface DataValidationKpiChild {

    Kpi getChildKpi();

    void remove();

}