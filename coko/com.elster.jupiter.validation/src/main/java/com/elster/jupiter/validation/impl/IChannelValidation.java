package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 2/10/2014
 * Time: 15:00
 */
public interface IChannelValidation {
	long getId();
    IMeterActivationValidation getMeterActivationValidation();
    Instant getLastChecked();
    Channel getChannel();
    boolean hasActiveRules();
    boolean updateLastChecked(Instant date);
    boolean moveLastCheckedBefore(Instant date);
    void validate();
}
