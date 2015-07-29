package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;

/**
 * Adds behavior to {@link ConnectionTask} that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-16 (13:38)
 */
public interface ServerConnectionTask<CPPT extends ComPortPool, PCTT extends PartialConnectionTask> extends ConnectionTask<CPPT, PCTT> {

    /**
     * Revalidates the properties of this ConnectionTask and will
     * set the {@link ConnectionTaskLifecycleStatus} to
     * {@link ConnectionTaskLifecycleStatus#INCOMPLETE}
     * if at least one required property is missing.
     * Assumes that no changes have been applied to
     * the ConnectionTask and intended to be called
     * when required properties were removed
     * on the {@link PartialConnectionTask}.
     */
    public void revalidatePropertiesAndAdjustStatus();

}