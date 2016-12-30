package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Map;

/**
 * This mostly uses the existing ActivityCalendarMessage functionality but changes some details.<br></br><br></br>
 * The names of the seasons and weeks are fixed to 0-based incrementing indexes (instead of their database IDs).<br></br>
 * The names of the day types are fixed to 1-based incrementing indexes (instead of their database IDs).<br></br>
 * @author sva
 * @since 24/03/2014 - 9:01
 */
public class XemexActivityCalendarParser extends ActivityCalendarMessage {

    public XemexActivityCalendarParser(TariffCalendar calendar, Extractor extractor) {
        super(calendar, extractor, null);
    }

    /**
     * Find the 0-based index number for a given season
     * This index number is used to create the AXDR arrays representing season profiles
     */
    @Override
    protected String getSeasonProfileName(Map.Entry<OctetString, String> entry) {
        return Integer.toString(seasonIds.get(entry.getValue()));
    }

    /**
     * Create an OctetString from the given weekProfileName. <br></br>
     * The octetString will *NOT* contain the ASCII representation of the weekProfileName, but will contain the weekProfileNames bytes as content.
     *
     * @param weekProfileName
     * @return
     */
    @Override
    protected OctetString getOctetStringFromInt(int weekProfileName) {
        byte[] bytes = {(byte) weekProfileName};
        return OctetString.fromByteArray(bytes, bytes.length);
    }

    @Override
    protected String getSeasonIdFromSeasonProfile(SeasonProfiles sp) {
        return Integer.toString(ProtocolTools.getIntFromBytes(sp.getSeasonProfileName().toByteArray()));
    }

}