package com.energyict.mdc.protocol.inbound.mbus.factory.profiles;

import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.factory.profiles.AbstractProfileFactory;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.SpacingControlByte;
import com.energyict.obis.ObisCode;


/**
 * Daily RF Frame will contain 24 Hrs of previous day index details
 * and will be supporting dynamic payload according to respective data logger configuration.
 *
 * Daily frame payload will dynamically change according to data logger interval configuration.
 * Daily frame payload will contain 24 different indexes if data logger interval is configured as hourly,
 *                                  48 indexes if data logger interval is configured as half hourly,
 *                                  96 indexes if data logger interval is configured as every 15 minutes.
 */
public class HourlyProfileFactory extends AbstractProfileFactory {


    public HourlyProfileFactory(Telegram telegram, InboundContext inboundContext) {
        super(telegram, inboundContext);
    }


    public ObisCode getObisCode() {
        return ObisCode.fromString(HOURLY_PROFILE_OBISCODE);
    }

    @Override
    public String getReadingTypeMRID() {
        return HOURLY_PROFILE_CIM;
    }


    @Override
    public SpacingControlByte applicableSpacingControlByte() {
        return SpacingControlByte.INDEX_HOUR_SPACING_CONTROL_BYTE;
    }

}
