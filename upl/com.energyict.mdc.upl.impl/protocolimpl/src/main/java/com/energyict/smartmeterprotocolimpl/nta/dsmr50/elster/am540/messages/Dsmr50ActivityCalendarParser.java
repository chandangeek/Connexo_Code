package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.dlms.DLMSMeterConfig;
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
public class Dsmr50ActivityCalendarParser extends ActivityCalendarMessage {

    public Dsmr50ActivityCalendarParser(TariffCalendar calendar, Extractor extractor, DLMSMeterConfig meterConfig) {
        super(calendar, extractor, meterConfig);
    }

    /**
     * Find the 0-based index number for a given season
     * This index number is used to create the AXDR arrays representing season profiles
     */
    @Override
    protected String getSeasonProfileName(Map.Entry<OctetString, String> entry) {
        return Integer.toString(seasonIds.get(entry.getValue()));
    }

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