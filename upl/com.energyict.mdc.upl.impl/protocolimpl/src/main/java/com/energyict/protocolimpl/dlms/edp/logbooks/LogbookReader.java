package com.energyict.protocolimpl.dlms.edp.logbooks;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.edp.CX20009;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 16:39
 * Author: khe
 */
public class LogbookReader {

    private final CX20009 protocol;

    public LogbookReader(CX20009 protocol) {
        this.protocol = protocol;
    }

    public List<MeterEvent> readAllEvents(Date from, Date to) throws IOException {
        Calendar fromCal = Calendar.getInstance(protocol.getTimeZone());
        fromCal.setTime(from);
        Calendar toCal = Calendar.getInstance(protocol.getTimeZone());
        toCal.setTime(to);

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        meterEvents.addAll(new StandardLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new ContractedPowerLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new FirmwareLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new ClockSyncLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new ConfigurationLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new DisconnectorLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new PowerFailureLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new QualityOfServiceLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new AntiFraudLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new DemandManagementLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new CommunicationLogbookParser(protocol).readMeterEvents(fromCal, toCal));
        meterEvents.addAll(new PublicLightingLogbookParser(protocol).readMeterEvents(fromCal, toCal));

        return meterEvents;
    }
}