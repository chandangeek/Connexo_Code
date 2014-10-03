package com.energyict.smartmeterprotocolimpl.nta.dsmr50.sagemcom.messages;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.mdw.core.Code;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.Dsmr50MessageExecutor;

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

    public SagemComDsmr50MessageExecutor(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    protected ActivityCalendarMessage getActivityCalendarParser(Code ct) {
        return new SagemComDsmr50ActivityCalendarParser(ct, getMeterConfig());
    }

    /**
     * Sort special days chronologically
     */
    protected Array sort(Array specialDays) {

        List<Date> startDates = new ArrayList<Date>();
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