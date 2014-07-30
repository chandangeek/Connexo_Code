package com.elster.jupiter.kpi;

import com.elster.jupiter.ids.IntervalLength;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.TimeZone;

public interface Kpi extends HasName {

    long getId();

    List<? extends KpiMember> getMembers();

    TimeZone getTimeZone();

    IntervalLength getIntervalLength();

    void save();
}
