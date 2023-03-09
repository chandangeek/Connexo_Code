/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.factory.profiles;

import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.SpacingControlByte;
import com.energyict.obis.ObisCode;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class NightlineProfileFactory extends AbstractProfileFactory {

    /* for now use 4am in the morning as default, but this can be configured */
    /* FW R&D will implement a parameter */
    public static final int NIGHTLINE_HOUR_START = 4;

    public NightlineProfileFactory(Telegram telegram, InboundContext inboundContext) {
        super(telegram, inboundContext);
    }

    @Override
    public ObisCode getObisCode() {
        return ObisCode.fromString(NIGHTLINE_PROFILE_OBISCODE);
    }

    @Override
    public SpacingControlByte applicableSpacingControlByte() {
        return SpacingControlByte.NIGHTLINE_15_MINUTES_SPACING_CON_BYTE;
    }

    @Override
    public String getReadingTypeMRID() {
        return NIGHTLINE_PROFILE_CIM;
    }

    @Override
    protected Instant getStartReferenceTimeStamp() {
        Instant nightLineStart = getMidnight().plus(NIGHTLINE_HOUR_START, ChronoUnit.HOURS);
        getInboundContext().getLogger().info("Nightline starting from " + nightLineStart);
        return nightLineStart;
    }
}
