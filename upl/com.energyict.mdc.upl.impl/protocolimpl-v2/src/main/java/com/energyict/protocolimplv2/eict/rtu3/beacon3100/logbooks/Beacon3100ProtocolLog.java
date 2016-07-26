package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by iulian on 7/26/2016.
 */
public class Beacon3100ProtocolLog extends Beacon3100AbstractEventLog {
    public Beacon3100ProtocolLog(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    @Override
    protected String getLogBookName() {
        return "Protocol log";
    }

    /** https://confluence.eict.vpdc/display/G3IntBeacon3100/Protocol+execution
     *
     * Event structure
             0: unsigned64: timestamp
             1: octet-string: meter serial
             2: unsigned64: device identifier
             3: enum: task result (0=success, 1=failure, 2=unknown)
             4: array of journal entries.  A journal entry consist of an
                                                entry type (enum),
                                                timestamp (unsigned64),
                                                message (octet-string) and
                                                debug information (octet-string)

     * @return
     */
    @Override
    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        long deviceId;
        Date eventTimeStamp;
        String serialNumber;
        int taskResult;
        for (int i = 0; i <= (size - 1); i++) {
            DataStructure eventStructure = this.dcEvents.getRoot().getStructure(i);

            eventTimeStamp = eventStructure.getOctetString(0).toDate(timeZone);
            serialNumber = eventStructure.getOctetString(1).toString();
            deviceId = (int) eventStructure.getValue(2) & 0xFFFFFFFF;
            taskResult = (int) eventStructure.getValue(3) & 0xFFFFFFFF;

            //TODO: parse array and build events

            buildMeterEvent(meterEvents, eventTimeStamp, taskResult, (int) deviceId, "Child device ["+serialNumber+"]("+deviceId+") - task result:"+taskResult);
        }
        return meterEvents;
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int dlmsCode, int deviceCode, String message) {
        int eiCode = MeterEvent.OTHER;
        String eventDescription = getDefaultEventDescription(dlmsCode, deviceCode, message);

        switch (deviceCode) {
            case 0x0036: eiCode = MeterEvent.OTHER; eventDescription =  " PROTOCOL_PRELIMINARY_TASK_COMPLETED"; break;
            case 0x0037: eiCode = MeterEvent.OTHER; eventDescription =  " PROTOCOL_PRELIMINARY_TASK_FAILED"; break;
            case 0x0038: eiCode = MeterEvent.OTHER; eventDescription =  " PROTOCOL_CONSECUTIVE_FAILURE"; break;


            case 255:
                eiCode = MeterEvent.EVENT_LOG_CLEARED;
                eventDescription = getLogBookName() + " cleared";
                break;
            default:
                // just the defaults

        }

        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), eiCode, deviceCode, eventDescription));
    }
}
