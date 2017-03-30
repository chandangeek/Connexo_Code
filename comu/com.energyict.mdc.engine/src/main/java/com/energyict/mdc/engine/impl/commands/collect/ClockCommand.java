/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.tasks.ClockTask;

import java.util.Optional;

/**
 * The {@link ComCommand} which can perform the actions necessary for a {@link com.energyict.mdc.tasks.ClockTask}
 *
 * @author gna
 * @since 8/05/12 - 14:35
 */
public interface ClockCommand extends CompositeComCommand {

    /**
     * @return the {@link TimeDifferenceCommand}
     */
    public TimeDifferenceCommand getTimeDifferenceCommand();

    /**
     * Set the given command as the TimeDifferenceCommand
     *
     * @param timeDifferenceCommand the timeDifferenceCommand
     */
    public void setTimeDifferenceCommand(final TimeDifferenceCommand timeDifferenceCommand);

    /**
     * @return the used {@link ClockTask} for this command
     */
    public ClockTaskOptions getClockTaskOptions();

    /**
     * Get the TimeDifference of the ClockCommand. If the timeDifference is not read,
     * then empty will be returned.
     *
     * @return the timeDifference
     */
    public Optional<TimeDuration> getTimeDifference();

    void updateAccordingTo(ClockTask clockTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution);

}
