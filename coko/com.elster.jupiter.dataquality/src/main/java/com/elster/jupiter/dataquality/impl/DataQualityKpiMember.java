/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.kpi.Kpi;

public interface DataQualityKpiMember {

    String KPIMEMBERNAME_SEPARATOR = "_";

    String getTargetIdentifier();

    Kpi getChildKpi();

    void remove();
}