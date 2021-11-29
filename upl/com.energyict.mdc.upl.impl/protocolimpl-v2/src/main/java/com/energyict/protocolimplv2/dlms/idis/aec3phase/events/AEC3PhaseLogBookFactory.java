package com.energyict.protocolimplv2.dlms.idis.aec3phase.events;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.dlms.DataContainer;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.idis.aec3phase.AEC3Phase;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AEC3PhaseLogBookFactory extends IDISLogBookFactory<AEC3Phase> {
    public AEC3PhaseLogBookFactory(AEC3Phase protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new AEC3PhaseStandardEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        //map the meter events in order to change the device type of the code to the correct device type from protocol
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents)
                .stream()
                .map(item -> {
                    item.getEventType().setType(protocol.getTypeMeter());
                    return item;
                }).collect(Collectors.toList());
    }
}
