package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXml;
import com.energyict.protocolimplv2.nta.dsmr40.messages.DSMR40ActivityCalendarController;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.TimeZone;

/**
 * The DSMR50 controller is almost equal to the DSMR40 variant;
 * the only difference is that the dayProfiles must start with ID 0 (instead of ID 1)
 *
 * @author sva
 * @since 25/02/2015 - 11:48
 */
public class DSMR50ActivitiyCalendarController extends DSMR40ActivityCalendarController {

    public DSMR50ActivitiyCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone) {
        super(cosemObjectFactory, timeZone);
    }

    public DSMR50ActivitiyCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone, ObisCode activityCalendarObisCode, ObisCode specialDaysCalendarObisCode) {
        super(cosemObjectFactory, timeZone, activityCalendarObisCode, specialDaysCalendarObisCode);
    }

    public DSMR50ActivitiyCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone, boolean contentBase64Encoded) {
        super(cosemObjectFactory, timeZone, contentBase64Encoded);
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
}