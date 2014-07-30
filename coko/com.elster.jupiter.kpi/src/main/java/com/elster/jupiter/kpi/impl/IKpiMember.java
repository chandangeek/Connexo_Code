package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.kpi.KpiMember;

interface IKpiMember extends KpiMember {

    TimeSeries getTimeSeries();

    void setTimeSeries(TimeSeries timeSeries);
}
