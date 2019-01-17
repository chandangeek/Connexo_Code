/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import com.elster.jupiter.util.conditions.Condition;

import java.time.Instant;
import java.util.List;

public interface AuditTrailFilter {
    Condition toCondition();

    void setChangedOnFrom(Instant changedOnFrom);

    void setChangedOnTo(Instant changedOnTo);

    void setCategories(List<String> categories);

    void setChangedBy(List<String> users);
}
