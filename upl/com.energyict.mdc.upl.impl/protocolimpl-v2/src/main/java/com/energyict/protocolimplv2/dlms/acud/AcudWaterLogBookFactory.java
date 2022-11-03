package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cim.EndDeviceType;
import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.acud.events.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class AcudWaterLogBookFactory extends AcudLogBookFactory {

    protected static ObisCode TIME_CHANGE_FROM_EVENT_LOG = ObisCode.fromString("8.0.99.98.3.255");
    protected static ObisCode TIME_CHANGE_UPTO_EVENT_LOG = ObisCode.fromString("8.0.99.98.4.255");
    protected static ObisCode EOB_RESET_EVENT_LOG = ObisCode.fromString("8.0.99.98.5.255");
    protected static ObisCode COMM_PORT_EVENT_LOG = ObisCode.fromString("8.0.99.98.9.255");
    protected static ObisCode POWER_LINE_CUT_EVENT_LOG = ObisCode.fromString("8.0.99.98.11.255");
    protected static ObisCode TAMPER1_EVENT_LOG = ObisCode.fromString("8.0.99.98.12.255");
    protected static ObisCode VALVE_CONTROL_EVENT_LOG = ObisCode.fromString("8.0.99.98.15.255");
    protected static ObisCode PASSWORD_CHANGES_EVENT_LOG = ObisCode.fromString("8.0.99.98.16.255");
    protected static ObisCode SECURITY_ASSOCIATION_EVENT_LOG = ObisCode.fromString("8.0.99.98.19.255");
    protected static ObisCode DISPLAY_ROLL_OVER_EVENT_LOG = ObisCode.fromString("8.0.99.98.20.255");

    private static final ObisCode[] supportedLogBooks = new ObisCode[]{
            TIME_CHANGE_FROM_EVENT_LOG,
            TIME_CHANGE_UPTO_EVENT_LOG,
            EOB_RESET_EVENT_LOG,
            COMM_PORT_EVENT_LOG,
            POWER_LINE_CUT_EVENT_LOG,
            TAMPER1_EVENT_LOG,
            VALVE_CONTROL_EVENT_LOG,
            PASSWORD_CHANGES_EVENT_LOG,
            SECURITY_ASSOCIATION_EVENT_LOG,
            DISPLAY_ROLL_OVER_EVENT_LOG,
    };

    public AcudWaterLogBookFactory(Acud protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        return parseWaterEvents(getProtocol().getTimeZone(), dataContainer, logBookObisCode, getProtocol().getTypeMeter());
    }

    protected List<ObisCode> getSupportedLogBooks() {
        return Arrays.asList(supportedLogBooks);
    }

    public static boolean isLogBookSupported(ObisCode obisCode) {
        return Arrays.asList(supportedLogBooks).contains(obisCode);
    }

    public static List<MeterProtocolEvent> parseWaterEvents(TimeZone timeZone, DataContainer dataContainer, ObisCode logBookObisCode, EndDeviceType meterType) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(TIME_CHANGE_FROM_EVENT_LOG)) {
            meterEvents = new TimeBeforeChangeEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(TIME_CHANGE_UPTO_EVENT_LOG)) {
            meterEvents = new TimeAfterChangeEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(EOB_RESET_EVENT_LOG)) {
            meterEvents = new EOBResetEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(COMM_PORT_EVENT_LOG)) {
            meterEvents = new CommPortEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(POWER_LINE_CUT_EVENT_LOG)) {
            meterEvents = new PowerLineCutEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(VALVE_CONTROL_EVENT_LOG)) {
            meterEvents = new OutputValveControlEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(SECURITY_ASSOCIATION_EVENT_LOG)) {
            meterEvents = new SecurityEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(DISPLAY_ROLL_OVER_EVENT_LOG)) {
            meterEvents = new RollOverToZeroEventLog(timeZone, dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(TAMPER1_EVENT_LOG)) {
            meterEvents = new Tamper1EventLog(timeZone, dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents).stream().map(item -> {
            item.getEventType().setType(meterType);
            return item;
        }).collect(Collectors.toList());
    }
}
