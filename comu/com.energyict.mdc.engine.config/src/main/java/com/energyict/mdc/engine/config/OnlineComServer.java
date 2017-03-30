/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

/**
 * Models a {@link ComServer} that is connected to the online database.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:43)
 */
public interface OnlineComServer extends ComServer, InboundCapableComServer, OutboundCapableComServer {

    /**
     * Gets the server name of this OnlineComServer. This name will be used
     * for building up the various URIs.
     *
     * @return the server name
     */
    public String getServerName();

    public void setServerName(String serverName);

    /**
     * Gets the URI on which the event registration mechanism runs.
     *
     * @return The URI or <code>null</code> if this ComServer does not support event registration
     */
    public String getEventRegistrationUri();

    /**
     * Gets the port on which the event registration mechanism runs.
     *
     * @return the port on which the event registration mechanism runs
     */
    public int getEventRegistrationPort();

    public void setEventRegistrationPort(int eventRegistrationPort);

    /**
     * Gets the URI that returns the status information of this ComServer.
     *
     * @return The URI
     */
    public String getStatusUri();

    /**
     * Gets the port on which the status information mechanism runs.
     *
     * @return the port on which the status information runs
     */
    public int getStatusPort();

    public void setStatusPort(int statusPort);

    public String getQueryApiPostUri();

    /**
     * Gets the port on which the query API mechanism runs
     *
     * @return the port on which the query API mechanism runs
     */
    public int getQueryApiPort();

    public void setQueryApiPort(int queryApiPort);

    /**
     * Gets the maximum number of store tasks that can be exeucted
     * by this OnlineComServer at one given time.
     * As soon as this number if reached, the OnlineComServer
     * will stop communicating with devices to avoid that the
     * storage mechanism cannot keep up with the communication mechanism.<br>
     * Note that as soon as store tasks complete, the OnlineComServer
     * will continue to communicate with devices and produce new store tasks.
     * <p>
     * Setting this value low will frequently stall the device communication
     * while setting this value high increase the risk that the ComServer
     * will run out of memory and that meter data will not get stored
     * after the communication with the meter completed.
     *
     * @return The maximum number of store tasks that this OnlineComServer
     * will be able to handle at one point in time
     */
    public int getStoreTaskQueueSize();

    public void setStoreTaskQueueSize(int storeTaskQueueSize);

    /**
     * Gets the number of threads that will effectively execute store tasks.
     * <p>
     * Setting this value low will slow down the execution of store tasks
     * while setting this value high will increase the speed at which
     * store tasks are taken from the queue and execute but at the same
     * time will but more strain on the machine resources.
     *
     * @return The number of threads that will execute store tasks
     */
    public int getNumberOfStoreTaskThreads();

    public void setNumberOfStoreTaskThreads(int numberOfStoreTaskThreads);

    /**
     * Gets the priority of the thread(s) that will execute store tasks.
     *
     * @return The priority of the thread(s) that will exeucte store tasks.
     */
    public int getStoreTaskThreadPriority();

    public void setStoreTaskThreadPriority(int storeTaskThreadPriority);

    interface OnlineComServerBuilder<OCS extends OnlineComServer> extends ComServerBuilder<OCS, OnlineComServerBuilder> {
        OnlineComServerBuilder numberOfStoreTaskThreads(int numberOfStoreTaskThreads);

        OnlineComServerBuilder storeTaskQueueSize(int storeTaskQueueSize);

        OnlineComServerBuilder storeTaskThreadPriority(int storeTaskThreadPriority);

        OnlineComServerBuilder serverName(String serverName);

        OnlineComServerBuilder eventRegistrationPort(int eventRegistrationPort);

        OnlineComServerBuilder queryApiPort(int queryApiPostPort);

    }

}