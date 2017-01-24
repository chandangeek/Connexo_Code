package com.energyict.protocolimplv2.edp.messages;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Map;

/**
 * This mostly uses the existing ActivityCalendarMessage functionality but changes some details.
 * The names of the seasons and weeks are fixed to 0-based incrementing indexes (instead of their database IDs).
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/02/14
 * Time: 11:51
 * Author: khe
 */
public class EDPActivityCalendarParser extends ActivityCalendarMessage {

    public EDPActivityCalendarParser(Calendar calendar) {
        super(calendar, null);
    }

    @Override
    protected int getDayTypeName(DayType dayType) {
        try {
            return Integer.parseInt(dayType.getName());  //Day type name should be "1", "2", etc.
        } catch (NumberFormatException e) {
            return super.getDayTypeName(dayType);
        }
    }

    /**
     * Find the 0-based index number for a given season
     * This index number is used to create the AXDR arrays representing season profiles
     */
    @Override
    protected Long getSeasonProfileName(Map.Entry entry) {
        Long value = (Long) entry.getValue();
        Map<Long, Integer> seasonIds = super.periodIds;
        return Long.valueOf(seasonIds.get(value));
    }

    @Override
    protected OctetString getOctetStringFromLong(long weekProfileName) {
        byte[] bytes = {(byte) weekProfileName};
        return OctetString.fromByteArray(bytes, bytes.length);
    }

    @Override
    protected int getSeasonIdFromSeasonProfile(SeasonProfiles sp) {
        return ProtocolTools.getIntFromBytes(sp.getSeasonProfileName().toByteArray());
    }
}
