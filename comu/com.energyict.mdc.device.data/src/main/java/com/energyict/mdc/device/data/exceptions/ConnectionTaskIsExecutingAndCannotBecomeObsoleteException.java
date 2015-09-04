package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComServer;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to obsolete a {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * that is currently executing and can therefore not be made obsolete.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (13:06)
 */
public class ConnectionTaskIsExecutingAndCannotBecomeObsoleteException extends LocalizedException {

    public ConnectionTaskIsExecutingAndCannotBecomeObsoleteException(ConnectionTask<?, ?> connectionTask, ComServer comServer, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, connectionTask.getName(), connectionTask.getDevice().getId(), comServer.getName());
        this.set("connectionTaskName", connectionTask.getName());
        this.set("deviceId", connectionTask.getDevice().getId());
        this.set("comServerName", comServer.getName());
    }

}