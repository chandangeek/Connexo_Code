package com.elster.jupiter.kpi;

import com.elster.jupiter.ids.IntervalLength;

import java.math.BigDecimal;
import java.util.TimeZone;

public interface KpiBuilder {

    Kpi build();

    KpiBuilder named(String name);

    KpiMemberBuilder member();

    KpiBuilder timeZone(TimeZone zone);

    KpiBuilder interval(IntervalLength interval);

    interface KpiMemberBuilder {

        KpiMemberBuilder withTargetSetAt(BigDecimal target);

        KpiMemberBuilder withDynamicTarget();

        KpiMemberBuilder asMinimum();

        KpiMemberBuilder asMaximum();

        KpiMemberBuilder named(String name);

        KpiBuilder add();
    }
}
