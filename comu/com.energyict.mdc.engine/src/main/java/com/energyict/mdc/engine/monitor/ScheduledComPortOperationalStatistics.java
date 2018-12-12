/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor;

import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ProviderType;

import java.util.Date;
import java.util.Optional;

/**
 * Models the operational statistics that are gathered by the process
 * that monitors a ScheduledComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (10:50)
 */
@ProviderType
public interface ScheduledComPortOperationalStatistics extends OperationalStatistics {

    /**
     * Gets the {@link TimeDuration} between each poll for communication
     * work that needs to be done.
     *
     * @return The TimeDuration between polls to find communication tasks
     */
     TimeDuration getSchedulingInterPollDelay ();

    /**
     * Gets the timestamp on which the ScheduledComPort last checked for work.
     *
     * @return The timestamp on which the ScheduledComPort last checked for work
     */
     Optional<Date> getLastCheckForWorkTimestamp ();

}