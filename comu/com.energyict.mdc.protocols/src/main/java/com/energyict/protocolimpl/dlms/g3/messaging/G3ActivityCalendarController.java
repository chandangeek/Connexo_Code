package com.energyict.protocolimpl.dlms.g3.messaging;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.attributeobjects.SeasonProfiles;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXml;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 21/08/12
 * Time: 14:00
 * Author: khe
 */
public class G3ActivityCalendarController extends DLMSActivityCalendarController {

    public G3ActivityCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone) {
        super(cosemObjectFactory, timeZone);
    }

    public G3ActivityCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone, ObisCode activityCalendarObisCode, ObisCode specialDaysCalendarObisCode) {
        super(cosemObjectFactory, timeZone, activityCalendarObisCode, specialDaysCalendarObisCode);
    }

    /**
     * Converts an epoch (long) into an OctetString describing the activation time
     * This is slightly different for the G3 meter (no DOW, timezone unspecified)
     */
    @Override
    protected OctetString getActivatePassiveCalendarTime(Long activationDate) throws IOException {
        OctetString defaultActivationDate = super.getActivatePassiveCalendarTime(activationDate);
        byte[] dateBytes = defaultActivationDate.getOctetStr();
        dateBytes[4] = (byte) 0xFF;               //no DOW
        dateBytes[9] = (byte) 128;      //Timezone unspecified
        dateBytes[10] = (byte) 0;
        dateBytes[11] = (byte) 255;     //DST unspecified
        return OctetString.fromByteArray(dateBytes);
    }

    @Override
    protected List<SeasonProfiles> sortSeasonProfiles(List<SeasonProfiles> seasonProfiles) throws IOException {
        Collections.sort(seasonProfiles);    //Sort the season profiles in chronological order
        int i = 0;
        for (SeasonProfiles profile : seasonProfiles) {
            profile.setSeasonProfileName(createSeasonName(String.valueOf(i)));
            profile.setWeekName(createSeasonName(String.valueOf(i)));
            i++;
        }
        return seasonProfiles;
    }

    /**
     * Pad the name (with spaces: 0x20) to 16 characters, add 5 bytes of 0xFF
     */
    protected byte[] constructEightByteCalendarName(String name) {
        byte[] calName = new byte[]{0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
        System.arraycopy(name.getBytes(), 0, calName, 0, (name.getBytes().length > 16) ? 16 : name.getBytes().length);
        return ProtocolTools.concatByteArrays(calName, new byte[]{(byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
    }

    /**
     * Days must start with ID 0
     *
     * @param dayProfileList the given list of dayProfiles
     */
    protected void createShiftedDayIdMap(final NodeList dayProfileList) {
        Node dayProfile;
        int dayIdCounter = 0;
        for (int i = 0; i < dayProfileList.getLength(); i++) {
            dayProfile = dayProfileList.item(i);
            for (int j = 0; j < dayProfile.getChildNodes().getLength(); j++) {
                if (dayProfile.getChildNodes().item(j).getNodeName().equalsIgnoreCase(CodeTableXml.dayId)) {
                    this.tempShiftedDayIdMap.put(dayProfile.getChildNodes().item(j).getTextContent(), dayIdCounter++);
                }
            }
        }
    }

    /**
     * Do not sort the special days
     *
     * @throws java.io.IOException
     */
    protected void sortSpecialDays() throws IOException {
        for (Node node : tempSpecialDayMap.values()) {
            sortedSpecialDayNodes.add(node);
        }
    }

}