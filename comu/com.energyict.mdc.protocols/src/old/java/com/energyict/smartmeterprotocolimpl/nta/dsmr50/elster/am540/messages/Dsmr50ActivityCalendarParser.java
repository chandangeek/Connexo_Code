/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Map;

public class Dsmr50ActivityCalendarParser extends ActivityCalendarMessage {

    public Dsmr50ActivityCalendarParser(Calendar calendar, DLMSMeterConfig meterConfig) {
        super(calendar, meterConfig);
    }

    @Override
    protected int getDayTypeName(DayType dayType) {
        return dayTypeIds.get(dayType.getId());     //Return an incremental 0-based ID
    }

    /**
     * Find the 0-based index number for a given season
     * This index number is used to create the AXDR arrays representing season profiles
     */
    @Override
    protected Long getSeasonProfileName(Map.Entry<OctetString, Long> entry) {
        return Long.valueOf(periodIds.get(entry.getValue()));
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