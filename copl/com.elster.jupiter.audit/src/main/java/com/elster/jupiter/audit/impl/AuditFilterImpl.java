/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditFilter;
import com.elster.jupiter.util.conditions.Condition;

public class AuditFilterImpl implements AuditFilter {

    @Override
    public Condition toCondition() {
        Condition condition = Condition.TRUE;
        return condition;
    }
}
