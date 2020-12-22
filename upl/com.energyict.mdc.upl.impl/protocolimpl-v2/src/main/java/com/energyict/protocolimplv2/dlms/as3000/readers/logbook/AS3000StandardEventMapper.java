package com.energyict.protocolimplv2.dlms.as3000.readers.logbook;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.mapper.EventMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AS3000StandardEventMapper implements EventMapper {

    @Override
    public List<MeterProtocolEvent> map(DataContainer dcEvents) {
        List<MeterEvent> meterEvents = new ArrayList<>();
        int size = dcEvents.getRoot().getNrOfElements();
        for (int i = 0; i <= (size - 1); i++) {
            Date eventTimeStamp = dcEvents.getRoot().getStructure(i).getOctetString(0).toDate();
            int eventId = dcEvents.getRoot().getStructure(i).getInteger(1);
            meterEvents.addAll(AS3000Event.buildMeterEvents(eventTimeStamp, eventId));
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }



}
