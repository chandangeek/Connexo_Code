package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.validation.ValidationService;

import java.time.Instant;
import java.util.Objects;

public class UsagePointTimeResolver {


    public static Instant resolveLastCheck(ValidationService validationService, ChannelsContainer channelsContainer, Instant candidate) {
        Instant lastChecked = validationService.getLastChecked(channelsContainer)
                .orElseGet(channelsContainer::getStart);

        // following condition should never happen since ORM will not allow both ValidationService and ChannelContainer to have null as begin of interval
        if (Objects.isNull(lastChecked) && Objects.isNull(candidate)) {
            throw new RuntimeException("Could not determine last check date");
        }

        if (Objects.isNull(lastChecked)) {
            return candidate;
        }
        if (Objects.isNull(candidate)) {
            return lastChecked;
        }

        return (lastChecked.isBefore(candidate) ? lastChecked : candidate)
                .plusMillis(1); // need to exclude lastChecked timestamp
    }

}
