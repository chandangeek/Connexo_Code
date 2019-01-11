/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import com.elster.jupiter.util.conditions.Condition;

public interface AuditFilter {
    Condition toCondition();
}
