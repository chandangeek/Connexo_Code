package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when
 * an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction}
 * is executed by the user and the {@link DeviceLifeCycleService.MicroActionPropertyName#EFFECTIVE_TIMESTAMP}
 * is not within the range defined by the max future and past time shift configured on the {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (16:41)
 */
public class EffectiveTimestampNotInRangeException extends DeviceLifeCycleActionViolationException {

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final Instant lowerBound;
    private final Instant upperBound;

    public EffectiveTimestampNotInRangeException(Thesaurus thesaurus, MessageSeed messageSeed, DeviceLifeCycle deviceLifeCycle) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.lowerBound = deviceLifeCycle.getMaximumPastEffectiveTimestamp();
        this.upperBound = deviceLifeCycle.getMaximumFutureEffectiveTimestamp();
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format(this.lowerBound, this.upperBound);
    }

}