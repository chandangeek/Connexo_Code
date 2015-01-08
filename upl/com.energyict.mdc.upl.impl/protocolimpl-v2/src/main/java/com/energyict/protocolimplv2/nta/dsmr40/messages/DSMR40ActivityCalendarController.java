package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;

import java.io.IOException;
import java.util.TimeZone;

/**
 * @author sva
 * @since 7/01/2015 - 15:31
 */
public class DSMR40ActivityCalendarController extends DLMSActivityCalendarController {


    public DSMR40ActivityCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone) {
        super(cosemObjectFactory, timeZone);
    }

    public DSMR40ActivityCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone, ObisCode activityCalendarObisCode, ObisCode specialDaysCalendarObisCode) {
        super(cosemObjectFactory, timeZone, activityCalendarObisCode, specialDaysCalendarObisCode);
    }

    @Override
    /**
     * DSMR 4.0 implementation differs from 2.3, override.
     * Order is now: write day profiles, write week profiles, write season profiles.
     */
    public void writeCalendar() throws IOException {
        ActivityCalendar ac = getActivityCalendar();
        ac.writeDayProfileTablePassive(getDayArray());
        ac.writeWeekProfileTablePassive(getWeekArray());
        ac.writeSeasonProfilePassive(getSeasonArray());

        if ("1".equalsIgnoreCase(this.activatePassiveCalendarTime.stringValue())) {
            ac.activateNow();
        } else if (!"0".equalsIgnoreCase(this.activatePassiveCalendarTime.stringValue())) {
            ac.writeActivatePassiveCalendarTime(this.activatePassiveCalendarTime);
        } else {
            logger.trace("No passiveCalendar activation date was given.");
        }
    }
}
