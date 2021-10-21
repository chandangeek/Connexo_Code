package com.energyict.protocolimplv2.dlms.idis.as3000g.events;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.idis.as3000g.AS3000G;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130LogBookFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cisac on 10/31/2016.
 */
public final class AS3000GLogBookFactory extends AM130LogBookFactory<AS3000G> {

    private static ObisCode STANDARD_EVENT_LOG       = ObisCode.fromString("0.0.99.98.0.255");
    private static ObisCode FRAUD_DETECTION_LOG      = ObisCode.fromString("0.0.99.98.1.255");
    private static ObisCode DISCONNECTOR_CONTROL_LOG = ObisCode.fromString("0.0.99.98.2.255");
    private static ObisCode POWER_QUALITY_EVENT_LOG  = ObisCode.fromString("0.0.99.98.4.255");
    private static ObisCode COMMUNICATION_LOG        = ObisCode.fromString("0.0.99.98.5.255");
    private static ObisCode POWER_FAILURE_EVENT_LOG  = ObisCode.fromString("1.0.99.97.0.255");


    public AS3000GLogBookFactory(AS3000G protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        supportedLogBooks.add(STANDARD_EVENT_LOG);
        supportedLogBooks.add(FRAUD_DETECTION_LOG);
        supportedLogBooks.add(DISCONNECTOR_CONTROL_LOG);
        supportedLogBooks.add(POWER_QUALITY_EVENT_LOG);
        supportedLogBooks.add(COMMUNICATION_LOG);
        supportedLogBooks.add(POWER_FAILURE_EVENT_LOG);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new AS3000GStandardEventLog(protocol.getTimeZone(), dataContainer, protocol.getDlmsSessionProperties().useBeaconMirrorDeviceDialect()).getMeterEvents();
        } else if (logBookObisCode.equals(FRAUD_DETECTION_LOG)) {
            meterEvents = new AS3000GFraudDetectionLog(protocol.getTimeZone(), dataContainer, protocol.getDlmsSessionProperties().useBeaconMirrorDeviceDialect()).getMeterEvents();
        } else if (logBookObisCode.equals(DISCONNECTOR_CONTROL_LOG)) {
            meterEvents = new AS3000DisconnectorControlLog(protocol.getTimeZone(), dataContainer, protocol.getDlmsSessionProperties().useBeaconMirrorDeviceDialect()).getMeterEvents();
        } else {
            //map the meter events in order to change the device type of the code to the correct device type from protocol
            return super.parseEvents(dataContainer, logBookObisCode)
                        .stream()
                        .map(item -> {
                            item.getEventType().setType(protocol.getTypeMeter());
                            return item; })
                        .collect(Collectors.toList());
        }
        //map the meter events in order to change the device type of the code to the correct device type from protocol
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents).stream().map(item -> {item.getEventType().setType(protocol.getTypeMeter()); return item;}).collect(Collectors.toList());
    }

}