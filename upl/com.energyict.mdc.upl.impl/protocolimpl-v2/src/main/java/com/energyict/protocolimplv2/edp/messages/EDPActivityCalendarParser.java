package com.energyict.protocolimplv2.edp.messages;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.genericprotocolimpl.common.messages.ActivityCalendarMessage;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeDayType;
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

    public EDPActivityCalendarParser(Code ct) {
        super(ct, null);
    }

    @Override
    protected int getDayTypeName(CodeDayType cdt) {
        try {
            return Integer.parseInt(cdt.getName());  //Day type name should be "1", "2", etc.
        } catch (NumberFormatException e) {
            return super.getDayTypeName(cdt);
        }
    }

    /**
     * Find the 0-based index number for a given season
     * This index number is used to create the AXDR arrays representing season profiles
     */
    @Override
    protected Integer getSeasonProfileName(Map.Entry entry) {
        Integer value = (Integer) entry.getValue();
        Map<Integer, Integer> seasonIds = (Map<Integer, Integer>) getSeasonIds();
        return (Integer) seasonIds.get(value);
    }

    @Override
    protected OctetString getOctetStringFromInt(int weekProfileName) {
        byte[] bytes = {(byte) weekProfileName};
        return OctetString.fromByteArray(bytes, bytes.length);
    }

    @Override
    protected int getSeasonIdFromSeasonProfile(SeasonProfiles sp) {
        return ProtocolTools.getIntFromBytes(sp.getSeasonProfileName().toByteArray());
    }
}
