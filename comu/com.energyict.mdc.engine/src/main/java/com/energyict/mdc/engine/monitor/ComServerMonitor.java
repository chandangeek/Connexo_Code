/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor;

import aQute.bnd.annotation.ProviderType;

/**
 * Exposes the information that is gathered by the process
 * that monitors a RunningComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (09:26)
 */
@ProviderType
public interface ComServerMonitor {

    ComServerOperationalStatistics getOperationalStatistics ();

    EventAPIStatistics getEventApiStatistics ();

    QueryAPIStatistics getQueryApiStatistics ();

    CollectedDataStorageStatistics getCollectedDataStorageStatistics ();

}