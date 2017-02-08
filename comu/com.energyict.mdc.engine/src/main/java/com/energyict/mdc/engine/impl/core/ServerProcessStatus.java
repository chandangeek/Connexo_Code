/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

/**
 * Models the different known and supported statusses of a {@link ServerProcess}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-21 (09:09)
 */
public enum ServerProcessStatus {

    /**
     * Indicates that the {@link ServerProcess} is starting up,
     * i.e. it is already active but not ready to service requests
     * or execute tasks yet.
     */
    STARTING,

    /**
     * Indicates that the {@link ServerProcess} is fully functional
     * and ready to service request or execute actions.
     */
    STARTED,

    /**
     * Indicates that the {@link ServerProcess} is shutting down,
     * i.e. it is releasing resources, cleaning up, completing
     * last service requests or finishing running tasks
     * but will no longer service new requests or start new tasks.
     */
    SHUTTINGDOWN,

    /**
     * Indicates that the {@link ServerProcess} has stopped
     * servicing requests or execute tasks. It is definitely not
     * going to service new requests or start new tasks.
     * It will need to be restarted.
     */
    SHUTDOWN

}