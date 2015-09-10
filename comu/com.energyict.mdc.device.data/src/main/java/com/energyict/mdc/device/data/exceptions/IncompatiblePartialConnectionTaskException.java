package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to create a {@link ConnectionTask}
 * against a Device for a {@link PartialConnectionTask}
 * that is not compatible with the type of ConnectionTask.
 * Remember that ConnectionTasks and PartialConnectionTasks
 * are of one of the following types:
 * <ul>
 * <li>Inbound</li>
 * <li>Outbound</li>
 * <li>Initiator</li>
 * </ul>
 * Both types must be compatible, i.e. the type of the PartialConnectionTask
 * must be the same as the type of the ConnectionTask.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (11:04)
 */
public class IncompatiblePartialConnectionTaskException extends LocalizedException {

    public <T extends PartialConnectionTask> IncompatiblePartialConnectionTaskException(PartialConnectionTask partialConnectionTask, Class<T> expectedPartialConnectionTaskType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, partialConnectionTask.getClass().getName(), expectedPartialConnectionTaskType.getName());
        this.set("partialConnectionTaskType", partialConnectionTask.getClass().getName());
        this.set("expectedPartialConnectionTaskType", expectedPartialConnectionTaskType.getName());
    }

}