package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.tasks.BasicCheckTask;

/**
 * The {@link ComCommand} which can perform the actions necessary for a {@link com.energyict.mdc.tasks.BasicCheckTask}
 *
 * @author gna
 * @since 31/05/12 - 13:12
 */
public interface BasicCheckCommand extends CompositeComCommand {

    /**
     * @return the {@link BasicCheckTask}
     */
    public BasicCheckTask getBasicCheckTask();

    /**
     * @return the {@link TimeDifferenceCommand}
     */
    public TimeDifferenceCommand getTimeDifferenceCommand();

    /**
     * @return the {@link VerifyTimeDifferenceCommand}
     */
    public VerifyTimeDifferenceCommand getVerifyTimeDifferenceCommand();

    /**
     * @return the {@link VerifySerialNumberCommand}
     */
    public VerifySerialNumberCommand getVerifySerialNumberCommand();

    /**
     * Get the TimeDifference of the BasicCheckCommand. If the timeDifference is not read,
     * then {@link TimeDifferenceCommand#DID_NOT_READ_TIME_DIFFERENCE} will be returned.
     *
     * @return the timeDifference
     */
    public TimeDuration getTimeDifference();
}
