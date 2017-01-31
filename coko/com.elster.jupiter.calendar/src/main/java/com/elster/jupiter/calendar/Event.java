/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Models a thing that happens or takes place
 * and takes some time to complete or finish.
 * When an Event occurs, the associated timeline
 * will produce this Event's code.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-07 (12:02)
 */
@ProviderType
public interface Event extends HasId, HasName {
    /**
     * The value that will be produced by the timeline
     * on which this Event occurs.
     *
     * @return The value
     */
    long getCode();

    Instant getCreateTime();

    long getVersion();

    Instant getModTime();

    String getUserName();

}