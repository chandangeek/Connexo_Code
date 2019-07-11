/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

public final class ModuleConstants {
    private ModuleConstants() {
    }

    public static final String REASON_FAILED = "reason.servicecall.failed";
    public static final String REASON_PARTIAL_SUCCEED = "reason.servicecall.partialsucceed";
    public static final String ACTION_CLASS_RETRY_COMMUNICATION = "com.elster.jupiter.servicecall.issue.impl.action.RetryServiceCallAction";
}
