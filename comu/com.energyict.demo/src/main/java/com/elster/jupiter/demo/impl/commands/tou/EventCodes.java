/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.tou;

/**
 * Models the event codes of the Belgian electricity market.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-17 (13:18)
 */
enum EventCodes {
    PEAK(11), OFFPEAK(10);

    private final long code;

    EventCodes(long code) {
        this.code = code;
    }

    long getCode() {
        return code;
    }
}