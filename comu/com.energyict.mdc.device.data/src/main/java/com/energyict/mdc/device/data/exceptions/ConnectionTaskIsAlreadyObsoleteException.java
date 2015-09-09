package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.sql.Date;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to obsolete a {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * that is in fact already obsolete.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (11:55)
 */
public class ConnectionTaskIsAlreadyObsoleteException extends LocalizedException {

    public ConnectionTaskIsAlreadyObsoleteException(ConnectionTask<?, ?> connectionTask, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, connectionTask.getName(), connectionTask.getDevice().getId(), Date.from(connectionTask.getObsoleteDate()));
        this.set("connectionTaskName", connectionTask.getName());
        this.set("deviceId", connectionTask.getDevice().getId());
        this.set("obsoleteDate", connectionTask.getObsoleteDate());
    }

}