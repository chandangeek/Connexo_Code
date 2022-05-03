package com.energyict.protocolimplv2.dlms.idis.iskra.am550.events;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.idis.iskra.am550.AM550;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.events.Mx382LogBookFactory;

import java.util.List;

/**
 * Created by Dmitry Borisov on 26/10/2021.
 */
public final class AM550LogBookFactory extends Mx382LogBookFactory {

    private static ObisCode COMMUNICATION_DETAIL_EVENT_LOG = ObisCode.fromString("0.0.99.98.6.255");
    private static ObisCode SECURITY_EVENT_LOG             = ObisCode.fromString("0.0.99.98.7.255");

    public AM550LogBookFactory(AM550 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        supportedLogBooks.add(COMMUNICATION_DETAIL_EVENT_LOG);
        supportedLogBooks.add(SECURITY_EVENT_LOG);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(COMMUNICATION_DETAIL_EVENT_LOG)) {
            meterEvents = new AM550CommunicationDetailEventLog(dataContainer, protocol.getTimeZone()).getMeterEvents();
            return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
        } else if(logBookObisCode.equals(SECURITY_EVENT_LOG)) {
            meterEvents = new AM550SecurityEventLog(dataContainer, protocol.getTimeZone()).getMeterEvents();
            return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
        }

        return super.parseEvents(dataContainer, logBookObisCode);

    }

}
