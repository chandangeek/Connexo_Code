/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface Entity {

    long getId();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();

    // Operational methods
    void update();

    void delete();
}
