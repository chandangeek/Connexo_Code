/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.elster.jupiter.time.TimeDuration;

import java.util.Optional;

/**
 * The {@link ComCommand} which can perform the necessary actions to collect the TimeDifference
 *
 * @author gna
 * @since 10/05/12 - 13:18
 */
public interface TimeDifferenceCommand extends ComCommand {

    /**
     * Get the TimeDifference of the ClockCommand. If the timeDifference is not read,
     * then Optional.empty() will be returned.
     *
     * @return the timeDifference
     */
    public Optional<TimeDuration> getTimeDifference();

}
