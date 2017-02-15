/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

/**
 * Defines constants that represent the property names of the JSon object
 * that will pass query information back and forth between
 * a remote and online com server.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (14:50)
 */
public final class RemoteComServerQueryJSonPropertyNames {

    /**
     * The name of the property that specifies
     * which remote com server query to execute.
     */
    public static final String METHOD = "method";

    /**
     * The name of the property that uniquely identifies
     * the query in the client context.
     */
    public static final String QUERY_ID = "query-id";

    /**
     * The name of the property that contains the result
     * of a query that returns a single object.
     */
    public static final String SINGLE_OBJECT_RESULT = "single-value";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link com.energyict.mdc.engine.config.ComServer}
     * that is passed as an argument to a remote com server query.
     */
    public static final String COMSERVER = "com-server";

    /**
     * The name of the property that specifies the host name
     * of a {@link com.energyict.mdc.engine.config.RemoteComServer}
     * that is passed as an argument to a remote com server query.
     */
    public static final String HOSTNAME = "host-name";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link com.energyict.mdc.engine.config.ComPort}
     * that is passed as an argument to a remote com server query.
     */
    public static final String COMPORT = "com-port";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}
     * that is passed as an argument to a remote com server query.
     */
    public static final String COMTASKEXECUTION = "com-task-execution";

    /**
     * The name of the property that specifies a collection of unique identifiers
     * of {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
     * that are passed as an argument to a remote com server query.
     */
    public static final String COMTASKEXECUTION_COLLECTION = "com-task-executions";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
     * that is passed as an argument to a remote com server query.
     */
    public static final String CONNECTIONTASK = "connection-task";

    /**
     * The name of the property that specifies the unique identifier
     * of a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}
     * that is passed as an argument to a remote com server query.
     */
    public static final String DEVICE = "device";

    /**
     * The name of the property that specifies the modification date
     * of an object who's unique identifier is passed as an argument
     * to a remote com server query.
     * Note that the value of the property is expected to be
     * the number of milliseconds since Jan 1st, 1970 (UTC).
     */
    public static final String MODIFICATION_DATE = "mod-date";

    /**
     * The name of the property that specifies the maximum number
     * of tries for a {@link com.energyict.mdc.device.data.tasks.ScheduledConnectionTask}.
     */
    public static final String MAX_NR_OF_TRIES = "max-tries";

    /**
     * The name of the property that specifies when we need to reschedule the ComtaskExecution
     */
    public static final String RESCHEDULE_DATE = "reschedule-time";

    // Hide utility class constructor
    private RemoteComServerQueryJSonPropertyNames () {}

}