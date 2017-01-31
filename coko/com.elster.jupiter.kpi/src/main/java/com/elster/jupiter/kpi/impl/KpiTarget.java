/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import java.math.BigDecimal;
import java.time.Instant;

interface KpiTarget {

    BigDecimal get(Instant date);
}
