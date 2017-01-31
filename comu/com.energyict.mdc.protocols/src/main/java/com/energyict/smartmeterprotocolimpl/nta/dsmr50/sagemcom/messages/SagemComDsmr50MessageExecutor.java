/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.Dsmr50MessageExecutor;

import java.text.ParseException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SagemComDsmr50MessageExecutor extends Dsmr50MessageExecutor {

    public SagemComDsmr50MessageExecutor(AbstractSmartNtaProtocol protocol, Clock clock, TopologyService topologyService, CalendarService calendarService) {
        super(protocol, clock, topologyService, calendarService);
    }

    @Override
    protected ActivityCalendarMessage getActivityCalendarParser(Calendar calendar) {
        return new SagemComDsmr50ActivityCalendarParser(calendar, getMeterConfig());
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
}