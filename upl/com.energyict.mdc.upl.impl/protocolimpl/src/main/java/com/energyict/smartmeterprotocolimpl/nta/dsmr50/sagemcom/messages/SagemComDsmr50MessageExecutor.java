package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.Dsmr50MessageExecutor;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 3/10/2014 - 9:15
 */
public class SagemComDsmr50MessageExecutor extends Dsmr50MessageExecutor {

    public SagemComDsmr50MessageExecutor(AbstractSmartNtaProtocol protocol, TariffCalendarFinder calendarFinder, TariffCalendarExtractor extractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor) {
        super(protocol, calendarFinder, extractor, messageFileFinder, messageFileExtractor);
    }

    @Override
    protected ActivityCalendarMessage getActivityCalendarParser(TariffCalendar calendar) {
        return new SagemComDsmr50ActivityCalendarParser(calendar, this.getCalendarExtractor(), getMeterConfig());
    }

    /**
     * Sort special days chronologically
     */
    protected Array sort(Array specialDays) {
        List<Date> startDates = new ArrayList<>();
        for (AbstractDataType specialDay : specialDays) {
            Structure structure = (Structure) specialDay;
            startDates.add(parseDate(structure.getDataType(1).getOctetString()));
        }
        Collections.sort(startDates);

        Array sortedSpecialDays = new Array();
        for (int index = 0; index < specialDays.nrOfDataTypes(); index++) {
            for (AbstractDataType specialDay : specialDays) {
                Structure structure = (Structure) specialDay;
                if (parseDate(structure.getDataType(1).getOctetString()).equals(startDates.get(index))) {
                    sortedSpecialDays.addDataType(specialDay);
                }
            }
        }
        return sortedSpecialDays;
    }

    private Date parseDate(OctetString octetString) {
        try {
            return AXDRDate.toDate((octetString));
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    /**
     * Convert the given unix activation date to a proper DateTimeArray
     */
    @Override
    protected Array convertActivationDateUnixToDateTimeArray(String strDate) throws IOException {
        return super.convertUnixToDateTimeArray(strDate); // Reuse the standard conversion instead of the special DSMR5.0 one
    }
}