package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ChannelValidation;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 2/10/2014
 * Time: 15:00
 */
public interface IChannelValidation extends ChannelValidation {
    boolean updateLastChecked(Instant date);
    boolean moveLastCheckedBefore(Instant date);
    void validate();
}
