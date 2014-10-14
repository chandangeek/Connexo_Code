package com.elster.jupiter.kpi.impl;

import java.math.BigDecimal;
import java.time.Instant;

interface KpiTarget {

    BigDecimal get(Instant date);
}
