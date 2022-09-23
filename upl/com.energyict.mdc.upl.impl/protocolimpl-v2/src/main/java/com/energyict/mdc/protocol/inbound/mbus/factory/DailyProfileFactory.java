package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.SpacingControlByte;
import com.energyict.obis.ObisCode;


/**
 * Weekly RF Frame contains a daily profile
 */
public class DailyProfileFactory extends AbstractProfileFactory{
    public static final String DAILY_PROFILE_OBISCODE = "8.0.99.2.0.255";
    public static final String DAILY_PROFILE_CIM = "11.0.0.1.1.9.58.0.0.0.0.0.0.0.0.0.42.0";    //[60-minutes] Bulk Water volume (m³)

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
