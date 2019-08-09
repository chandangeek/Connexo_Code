/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;

/**
 * Add-on for ComTaskExecution, extending its behavior that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-17 (15:17)
 */
public interface HasLastComTaskExecutionSession {

    /**
     * Notifies that a new {@link ComTaskExecutionSession} was created
     * and may update the last ComSession if the newly created on
     * was more recent then the current las ComSession.
     *
     * @param session The newly created ComTaskExecutionSession
     */
    public void sessionCreated(ComTaskExecutionSession session);

}