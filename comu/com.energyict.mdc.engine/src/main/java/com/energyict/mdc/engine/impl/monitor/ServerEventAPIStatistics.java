/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.monitor.EventAPIStatistics;

public interface ServerEventAPIStatistics extends EventAPIStatistics {

    /**
     * Resets all counters that make sense to reset.
     * The number of clients will not be reset as that
     * can only be affected by clients that connect/disconnect.
     */
     void reset ();

    /**
     * Increases the number of events that has been collected by the
     * RunningComServer.
     */
     void eventWasPublished ();
}
