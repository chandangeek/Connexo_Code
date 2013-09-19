package com.elster.jupiter.rest.whiteboard;

import com.elster.jupiter.util.time.StopWatch;

import java.net.URL;

public final class RestCallExecutedEvent {

    private final StopWatch stopWatch;
    private final URL url;

    public RestCallExecutedEvent(URL url, StopWatch stopWatch) {
        this.stopWatch = stopWatch;
        this.url = url;
    }

    public StopWatch getStopWatch() {
        return stopWatch;
    }

    public URL getUrl() {
        return url;
    }
    
    @Override
    public String toString() {
    	return "Rest call to " + url + " executed in " + (stopWatch.getElapsed() / 1000L) + " \u00b5s";
    }
}
