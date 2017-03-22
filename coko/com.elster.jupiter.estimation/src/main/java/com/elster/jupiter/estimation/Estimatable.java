/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import aQute.bnd.annotation.ConsumerType;

import java.math.BigDecimal;
import java.time.Instant;

@ConsumerType
public interface Estimatable {

    Instant getTimestamp();

    void setEstimation(BigDecimal value);

    BigDecimal getEstimation();
}
