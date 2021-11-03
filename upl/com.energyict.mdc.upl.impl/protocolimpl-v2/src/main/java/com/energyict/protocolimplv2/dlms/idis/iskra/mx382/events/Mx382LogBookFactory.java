package com.energyict.protocolimplv2.dlms.idis.iskra.mx382.events;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130LogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.Mx382;

import java.util.List;

/**
 * Created by cisac on 1/15/2016.
 */
public class Mx382LogBookFactory<T extends Mx382> extends AM130LogBookFactory<Mx382> {

    private static ObisCode CERTIFICATION_LOG = ObisCode.fromString("1.0.99.99.0.255");

    public Mx382LogBookFactory(T  protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        supportedLogBooks.add(CERTIFICATION_LOG);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(CERTIFICATION_LOG)) {
            meterEvents = new Mx382CertificationLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
            return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
        } else if(logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new Mx382StandardEventLog(protocol.getTimeZone(), dataContainer).getMeterEvents();
            return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
        }

        return super.parseEvents(dataContainer, logBookObisCode);

    }

}
