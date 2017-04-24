/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import aQute.bnd.annotation.ConsumerType;
import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.Instant;

@ProviderType
public interface Estimatable {

    Instant getTimestamp();

    void setEstimation(BigDecimal value);

    BigDecimal getEstimation();
}
