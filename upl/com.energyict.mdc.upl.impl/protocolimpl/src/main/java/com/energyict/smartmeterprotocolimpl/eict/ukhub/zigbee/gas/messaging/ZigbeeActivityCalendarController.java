package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging;

import com.energyict.dlms.cosem.*;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300ActivityCalendarController;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2011
 * Time: 15:07:36
 */
public class ZigbeeActivityCalendarController extends AS300ActivityCalendarController {

    private final AbstractSmartDlmsProtocol protocol;
    private ActivityCalendar activityCalendar;
    private SpecialDaysTable specialDayTable;

    public ZigbeeActivityCalendarController(AbstractSmartDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Getter for the LOCAL {@link com.energyict.dlms.cosem.ActivityCalendar}
     *
     * @return the current local {@link com.energyict.dlms.cosem.ActivityCalendar}
     */
    @Override
    protected ActivityCalendar getActivityCalendar() throws IOException {
        if (this.activityCalendar == null) {
            this.activityCalendar = new ActivityCalendar(this.protocol.getDlmsSession(), new ObjectReference(ObisCodeProvider.ACTIVITY_CALENDER.getLN()));
        }
        return this.activityCalendar;
    }

    @Override
    protected SpecialDaysTable getSpecialDayTable() throws IOException {
        if (this.specialDayTable == null) {
            this.specialDayTable = new SpecialDaysTable(this.protocol.getDlmsSession(), new ObjectReference(ObisCodeProvider.SPECIAL_DAY_TABLE.getLN()));
        }
        return this.specialDayTable;
    }

}
