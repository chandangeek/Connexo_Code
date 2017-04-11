package com.energyict.protocolimplv2.dlms.idis.am540.events;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130LogBookFactory;

import java.util.List;

/**
 * Created by cisac on 10/31/2016.
 */
public class AM540LogBookFactory extends AM130LogBookFactory {

    private static ObisCode SECURITY_LOG = ObisCode.fromString("0.0.99.98.9.255");
    private static ObisCode ALTERNATE_PANID_LOG = ObisCode.fromString("0.0.94.33.9.255");


    public AM540LogBookFactory(AM130 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
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
            return super.parseEvents(dataContainer, logBookObisCode);
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }

}