/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.factory.profiles;

import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.SpacingControlByte;
import com.energyict.obis.ObisCode;


/**
 * Weekly RF Frame contains a daily profile
 */
public class DailyProfileFactory extends AbstractProfileFactory {


    public DailyProfileFactory(Telegram telegram, InboundContext inboundContext) {
        super(telegram, inboundContext);
    }

    public ObisCode getObisCode() {
        return ObisCode.fromString(DAILY_PROFILE_OBISCODE);
    }

    @Override
    public String getReadingTypeMRID() {
        return DAILY_PROFILE_CIM;
    }


    @Override
    public SpacingControlByte applicableSpacingControlByte() {
        return SpacingControlByte.WEEKLY_LOG_SPACING_CONTROL_BYTE;
    }

}
