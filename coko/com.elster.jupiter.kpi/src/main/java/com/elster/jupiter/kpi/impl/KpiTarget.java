package com.elster.jupiter.kpi.impl;

import java.math.BigDecimal;
import java.util.Date;

interface KpiTarget {

    BigDecimal get(Date date);
}
