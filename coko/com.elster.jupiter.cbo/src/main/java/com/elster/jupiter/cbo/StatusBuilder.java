/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import java.time.Instant;

public interface StatusBuilder {

    StatusBuilder value(String value);

    StatusBuilder reason(String reason);

    StatusBuilder remark(String remark);

    StatusBuilder at(Instant dateTime);

    Status build();
}
