/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import java.time.Instant;

public interface Entity {
    long getId();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();

    void delete();
}
