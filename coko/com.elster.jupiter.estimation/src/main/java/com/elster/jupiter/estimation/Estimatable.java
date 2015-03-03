package com.elster.jupiter.estimation;

import java.math.BigDecimal;
import java.time.Instant;

public interface Estimatable {

    Instant getTimestamp();

    void setEstimation(BigDecimal value);

    BigDecimal getEstimation();
}
