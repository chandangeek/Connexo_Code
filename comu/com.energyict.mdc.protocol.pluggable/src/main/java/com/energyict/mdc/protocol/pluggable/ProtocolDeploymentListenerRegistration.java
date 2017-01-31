/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

/**
 * Models the registration of a {@link ProtocolDeploymentListener}
 * at the {@link ProtocolPluggableService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-29 (10:04)
 */
public interface ProtocolDeploymentListenerRegistration {

    /**
     * Undoes the registration such that the {@link ProtocolDeploymentListener}
     * will no longer receive notifications.
     */
    public void unregister();

}