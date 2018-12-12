/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
    private final Instant lowerBound;
    private final Instant upperBound;
    private final DateTimeFormatter formatter;

    public EffectiveTimestampNotInRangeException(Thesaurus thesaurus, MessageSeed messageSeed, Instant lowerBound, Instant upperBound, DateTimeFormatter formatter) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.formatter = formatter;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus
                .getFormat(this.messageSeed)
                .format(
                    getFormattedInstant(this.formatter, this.lowerBound),
                    getFormattedInstant(this.formatter, this.upperBound));
    }

    private String getFormattedInstant(DateTimeFormatter formatter, Instant time){
        return formatter.format(LocalDateTime.ofInstant(time, ZoneId.systemDefault()));
    }
}