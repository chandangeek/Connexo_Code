/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard;

import com.elster.jupiter.util.time.StopWatch;

import java.net.URL;

import org.osgi.service.event.Event;

public interface RestCallExecutedEvent {
    StopWatch getStopWatch();
    URL getUrl();
    int getSqlCount();
    int getTransactionCount();
    int getFailedCount();
    int getFetchCount();
    Event toOsgiEvent();
}
