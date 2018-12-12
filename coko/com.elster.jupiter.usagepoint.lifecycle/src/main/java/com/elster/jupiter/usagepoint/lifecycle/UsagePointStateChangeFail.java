/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle;

public interface UsagePointStateChangeFail {
    enum FailSource {
        ACTION,
        CHECK,;
    }

    FailSource getFailSource();

    String getKey();

    String getName();

    String getMessage();
}
