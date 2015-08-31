package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Instant;
import java.util.Date;

/**
 * Models the exceptional situation that occurs when
 * an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction}
 * is executed by the user and the effective timestamp is not within the range
 * defined by the max future and past time shift configured on the {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (16:41)
 */
public class EffectiveTimestampNotInRangeException extends DeviceLifeCycleActionViolationException {

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final Date lowerBound;
    private final Date upperBound;

    public EffectiveTimestampNotInRangeException(Thesaurus thesaurus, MessageSeed messageSeed, Instant lowerBound, Instant upperBound) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.lowerBound = Date.from(lowerBound);
        this.upperBound = Date.from(upperBound);
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus
                .getFormat(this.messageSeed)
                .format(
                    this.lowerBound,
                    this.upperBound);
    }

}