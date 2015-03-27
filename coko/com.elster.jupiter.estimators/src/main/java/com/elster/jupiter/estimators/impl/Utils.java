package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

/**
 * Created by igh on 27/03/2015.
 */
public class Utils {

    public Optional<Range<Instant>> getAllPeriod(Channel channel, ValidationService validationService) {
        Instant from = channel.getMeterActivation().getStart();
        Instant to = validationService.getLastChecked(channel).orElse(null);
        Range<Instant> range = (to == null) ? null : Range.closed(from, to);
        return Optional.ofNullable(range);
    }
}
