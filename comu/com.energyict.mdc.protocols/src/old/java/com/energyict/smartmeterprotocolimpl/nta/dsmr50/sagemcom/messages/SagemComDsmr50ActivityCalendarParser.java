package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages;

import com.elster.jupiter.calendar.Calendar;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.Dsmr50ActivityCalendarParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/10/2014 - 9:14
 */
public class SagemComDsmr50ActivityCalendarParser extends Dsmr50ActivityCalendarParser {

    public SagemComDsmr50ActivityCalendarParser(Calendar calendar, DLMSMeterConfig meterConfig) {
        super(calendar, meterConfig);
    }

    /**
     * Sort seasons chronologically
     */
    @Override
    protected Array sort(Array seasonArray) {

        List<Date> startDates = new ArrayList<Date>();
        for (AbstractDataType season : seasonArray) {
            SeasonProfiles seasonProfile = (SeasonProfiles) season;
            startDates.add(seasonProfile.getSeasonStartDate());
        }
        Collections.sort(startDates);

        Array sortedSeasonArray = new Array();
        for (int index = 0; index < seasonArray.nrOfDataTypes(); index++) {
            for (AbstractDataType season : seasonArray) {
                SeasonProfiles seasonProfile = (SeasonProfiles) season;
                if (seasonProfile.getSeasonStartDate().equals(startDates.get(index))) {
                    sortedSeasonArray.addDataType(season);
                }
            }
        }
        return sortedSeasonArray;
    }
}