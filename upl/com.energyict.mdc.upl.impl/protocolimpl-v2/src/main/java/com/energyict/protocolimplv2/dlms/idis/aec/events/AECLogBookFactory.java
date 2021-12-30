package com.energyict.protocolimplv2.dlms.idis.aec.events;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.dlms.DataContainer;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.idis.aec.AEC;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AECLogBookFactory extends IDISLogBookFactory<AEC> {

    public AECLogBookFactory(AEC protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        supportedLogBooks.remove(STANDARD_EVENT_LOG);
        STANDARD_EVENT_LOG = ObisCode.fromString("1.1.99.98.0.255");
        supportedLogBooks.add(STANDARD_EVENT_LOG);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new AECStandardEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        //map the meter events in order to change the device type of the code to the correct device type from protocol
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents).stream().map(item -> {
            item.getEventType().setType(protocol.getTypeMeter());
            return item;
        }).collect(Collectors.toList());
    }

}
