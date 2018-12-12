package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.events;

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
 * Created by cisac on 1/11/2017.
 */
public class T210DLogBookFactory extends AM130LogBookFactory {

    public T210DLogBookFactory(AM130 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(DISCONNECTOR_CONTROL_LOG)) {
            meterEvents = new T210DDisconnectorControlLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new T210DStandardEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MBUS_EVENT_LOG)) {
            meterEvents = new T210DMBusEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
        } else {
            return super.parseEvents(dataContainer, logBookObisCode);
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }
}
