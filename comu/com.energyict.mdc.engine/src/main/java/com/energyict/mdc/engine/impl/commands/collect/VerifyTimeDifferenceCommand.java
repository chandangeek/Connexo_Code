/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.elster.jupiter.time.TimeDuration;

import java.util.Optional;

/**
 * The {@link ComCommand} which can perform the necessary actions to verify the TimeDifference
 *
 * @author sva
 * @since 18/04/13 - 9:59
 */
public interface VerifyTimeDifferenceCommand extends ComCommand {

    /**
     * Get the TimeDifference of the ClockCommand. If the timeDifference is not read,
     * then Optional.empty() will be returned.
     *
     * @return the timeDifference
     */
    Optional<TimeDuration> getTimeDifference();

}
