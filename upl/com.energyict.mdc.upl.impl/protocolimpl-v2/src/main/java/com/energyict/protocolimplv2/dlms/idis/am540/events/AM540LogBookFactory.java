package com.energyict.protocolimplv2.dlms.idis.am540.events;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130LogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks.Beacon3100LogBookFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cisac on 10/31/2016.
 */
public final class AM540LogBookFactory extends AM130LogBookFactory<AM540> {

    private static ObisCode SECURITY_LOG = ObisCode.fromString("0.0.99.98.9.255");
    private static ObisCode ALTERNATE_PANID_LOG = ObisCode.fromString("0.0.94.33.9.255");


    public AM540LogBookFactory(AM540 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        supportedLogBooks.add(SECURITY_LOG);
        supportedLogBooks.add(ALTERNATE_PANID_LOG);

        supportedLogBooks.add(Beacon3100LogBookFactory.PROTOCOL_LOGBOOK); // this is an virtual logbook, populated while reading the beacon
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(SECURITY_LOG)) {
            meterEvents = new AM540SecurityEventLog(dataContainer, protocol.getTimeZone()).getMeterEvents();
        } else {
            //map the meter events in order to change the device type of the code to the correct device type from protocol
            return super.parseEvents(dataContainer, logBookObisCode).stream().map(item -> {item.getEventType().setType(protocol.getTypeMeter()); return item;}).collect(Collectors.toList());
        }
        //map the meter events in order to change the device type of the code to the correct device type from protocol
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents).stream().map(item -> {item.getEventType().setType(protocol.getTypeMeter()); return item;}).collect(Collectors.toList());
    }

}