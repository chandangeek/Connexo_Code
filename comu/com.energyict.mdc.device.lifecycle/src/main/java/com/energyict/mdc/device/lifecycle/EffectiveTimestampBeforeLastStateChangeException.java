package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Instant;
import java.util.Date;

/**
 * Models the exceptional situation that occurs when
 * an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction}
 * is executed by the user and the effective timestamp
 * is before the last state change on that same {@link Device}.
 * Say the expected states of a device are A, B and C and the device
 * is in state B since e.g. May 2nd 2015. When the effective date would
 * be allowed to be before May 2nd 2015, then the state history would
 * (in that order) be A, C, B and that would be confusing to the user.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (16:41)
 */
public class EffectiveTimestampBeforeLastStateChangeException extends DeviceLifeCycleActionViolationException {

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final String mRID;
    private final Instant effectiveTimestamp;
    private final Instant lastStateChange;

    public EffectiveTimestampBeforeLastStateChangeException(Thesaurus thesaurus, MessageSeed messageSeed, Device device, Instant effectiveTimestamp, Instant lastStateChange) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.mRID = device.getmRID();
        this.effectiveTimestamp = effectiveTimestamp;
        this.lastStateChange = lastStateChange;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format(this.mRID, Date.from(this.effectiveTimestamp)  , Date.from(this.lastStateChange));
    }

}