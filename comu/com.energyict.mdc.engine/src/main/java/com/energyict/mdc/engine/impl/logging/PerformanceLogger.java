package com.energyict.mdc.engine.impl.logging;

import java.util.logging.Logger;

/**
 * Holds onto the {@link Logger} that should be used by all
 * components that will measure and log their own performance
 * with the <a href="http://perf4j.codehaus.org">perf4J framework</a>.
 * Typical usage pattern for those components will be:
 * <pre><code>
 * LoggingStopWatch stopWatch = new LoggingStopWatch("name of code block that needs performance monitoring");
 * ...  // Execute the statements that need performance monitoring
 * stopWatch.stop();
 * </code></pre>
 * The logging properties of the ComServer platform will ensure that all the logging
 * will go to a separate log file that can then be analyzed (according to the perf4j manuals)
 * with the following script:
 * <pre><code>
 * java -jar perf4j-0.9.16.jar ComServer-performance.log
 * </code></pre>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-08-13 (13:53)
 */
public final class PerformanceLogger {

    public static final Logger INSTANCE = Logger.getLogger("com.energyict.comserver.logging.performance");

    // Hide utility class constructor
    private PerformanceLogger () {}

}