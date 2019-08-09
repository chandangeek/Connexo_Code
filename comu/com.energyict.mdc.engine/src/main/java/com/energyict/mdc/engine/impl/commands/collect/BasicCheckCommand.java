/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.tasks.BasicCheckTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;

import java.util.Optional;

/**
 * The {@link ComCommand} which can perform the actions necessary for a {@link BasicCheckTask}
 *
 * @author gna
 * @since 31/05/12 - 13:12
 */
public interface BasicCheckCommand extends CompositeComCommand {

    /**
     * @return the {@link BasicCheckTask}
     */
    public Optional<TimeDuration> getMaximumClockDifference();

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
     * then empty will be returned.
     *
     * @return the timeDifference
     */
    public Optional<TimeDuration> getTimeDifference();

    public void updateAccordingTo(BasicCheckTask basicCheckTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution);

}
