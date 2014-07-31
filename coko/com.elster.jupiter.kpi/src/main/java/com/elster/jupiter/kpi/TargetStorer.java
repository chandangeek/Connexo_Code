package com.elster.jupiter.kpi;

import java.math.BigDecimal;
import java.util.Date;

public interface TargetStorer {

    TargetStorer add(Date timestamp, BigDecimal target);

    void execute();
}
