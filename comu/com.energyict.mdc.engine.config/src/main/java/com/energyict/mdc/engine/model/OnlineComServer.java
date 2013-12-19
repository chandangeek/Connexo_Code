package com.energyict.mdc.engine.model;

import com.energyict.mdc.shadow.servers.OnlineComServerShadow;

/**
 * Models a {@link ComServer} that is connected to the online database.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:43)
 */
public interface OnlineComServer extends ComServer<OnlineComServerShadow>, InboundCapableComServer<OnlineComServerShadow>, OutboundCapableComServer<OnlineComServerShadow> {

    /**
     * Gets the URI on which the event registration mechanism runs.
     *
     * @return The URI or <code>null</code> if this ComServer does not support event registration
     */
    public String getEventRegistrationUri ();

    /**
     * Tests if the URI for the event registration mechanism
     * is the default one or not.
     *
     * @return <code>true</code> iff the URI for the event registration mechanism is the default one
     */
    public boolean usesDefaultEventRegistrationUri ();

    public String getQueryApiPostUri ();

    /**
     * Tests if the URI for the query API post mechanism
     * is the default one or not.
     *
     * @return <code>true</code> iff the URI for the query API post mechanism is the default one
     */
    public boolean usesDefaultQueryApiPostUri ();

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
     *         will be able to handle at one point in time
     */
    public int getStoreTaskQueueSize ();

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
    public int getNumberOfStoreTaskThreads ();

    /**
     * Gets the priority of the thread(s) that will execute store tasks.
     *
     * @return The priority of the thread(s) that will exeucte store tasks.
     */
    public int getStoreTaskThreadPriority ();

}