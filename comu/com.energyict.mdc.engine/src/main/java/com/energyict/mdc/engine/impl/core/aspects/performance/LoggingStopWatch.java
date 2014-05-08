package com.energyict.mdc.engine.impl.core.aspects.performance;

import com.elster.jupiter.util.time.StopWatch;

import java.util.logging.Logger;

/**
 * Wrapper for {@link com.elster.jupiter.util.time.StopWatch} that
 * will log the duration of the execution with a tag name
 * that is specified @ construction time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-12 (15:19)
 */
public class LoggingStopWatch {

    private com.elster.jupiter.util.time.StopWatch actualStopWatch;
    private long startTime;
    private String tag;
    private Logger logger;

    public LoggingStopWatch (String tag, Logger logger) {
        super();
        this.startTime = System.currentTimeMillis();
        this.actualStopWatch = new StopWatch();
        this.tag = tag;
        this.logger = logger;
    }

    public void stop () {
        this.actualStopWatch.stop();
        logger.info(this.perf4jMessage());
    }

    private String perf4jMessage () {
        StringBuilder builder = new StringBuilder();
        builder.
            append("start[").append(this.startTime).append("] ").
            append("time[").append(this.actualStopWatch.getElapsed()).append("] ").
            append("tag[").append(this.tag).append("]");
        return builder.toString();
    }

}
