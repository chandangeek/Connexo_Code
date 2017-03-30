/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.tasks.history.ComSession;

/**
 * Add-on for ConnectionTask, extending its behavior that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-17 (11:05)
 */
public interface HasLastComSession {

    /**
     * Notifies that a new {@link ComSession} was created
     * and may update the last ComSession if the newly created on
     * was more recent then the current las ComSession.
     *
     * @param session The newly created ComSession
     */
    public void sessionCreated(ComSession session);

}