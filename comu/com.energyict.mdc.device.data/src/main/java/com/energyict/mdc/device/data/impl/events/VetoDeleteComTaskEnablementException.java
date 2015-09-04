package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when a {@link ComTaskEnablement}
 * is being deleted while it is still being used by one or more
 * {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-29 (08:35)
 */
public class VetoDeleteComTaskEnablementException extends LocalizedException {

    protected VetoDeleteComTaskEnablementException(Thesaurus thesaurus, ComTaskEnablement comTaskEnablement) {
        super(thesaurus, MessageSeeds.VETO_COM_TASK_ENABLEMENT_DELETION, comTaskEnablement.getComTask().getName(), comTaskEnablement.getDeviceConfiguration().getName());
        this.set("comTaskName", comTaskEnablement.getComTask().getName());
        this.set("deviceConfigurationName", comTaskEnablement.getDeviceConfiguration().getName());
    }

}