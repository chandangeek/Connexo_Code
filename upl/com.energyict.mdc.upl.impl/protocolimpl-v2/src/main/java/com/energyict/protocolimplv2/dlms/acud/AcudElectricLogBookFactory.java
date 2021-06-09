package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.acud.events.electric.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AcudElectricLogBookFactory extends AcudLogBookFactory {

    protected static ObisCode POWER_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    protected static ObisCode SYNCHRO_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    protected static ObisCode COMMON_EVENT_LOG = ObisCode.fromString("0.0.99.98.2.255");
    protected static ObisCode MEMORY_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");
    protected static ObisCode TAMPER1_EVENT_LOG = ObisCode.fromString("0.0.99.98.4.255");
    protected static ObisCode TAMPER2_EVENT_LOG = ObisCode.fromString("0.0.99.98.5.255");
    protected static ObisCode COMMUNICATION_EVENT_LOG = ObisCode.fromString("0.0.99.98.6.255");
    protected static ObisCode QUALITY_EVENT_LOG = ObisCode.fromString("0.0.99.98.7.255");
    protected static ObisCode CUT_EVENT_LOG = ObisCode.fromString("0.0.99.98.8.255");
    protected static ObisCode CURRENT_EVENT_LOG = ObisCode.fromString("0.0.99.98.9.255");
    protected static ObisCode DISCONNECTOR_EVENT_LOG = ObisCode.fromString("0.0.99.98.10.255");
    protected static ObisCode FIRMWARE_EVENT_LOG = ObisCode.fromString("0.0.99.98.11.255");
    protected static ObisCode PASSWORD_EVENT_LOG = ObisCode.fromString("0.0.99.98.12.255");
    protected static ObisCode SECURITY_EVENT_LOG = ObisCode.fromString("0.0.99.98.13.255");
    protected static ObisCode MAXDEMAND_EVENT_LOG = ObisCode.fromString("1.0.94.20.62.255");

    private static final ObisCode[] supportedLogBooks = new ObisCode[]{
            POWER_EVENT_LOG,
            SYNCHRO_EVENT_LOG,
            COMMON_EVENT_LOG,
            MEMORY_EVENT_LOG,
            TAMPER1_EVENT_LOG,
            TAMPER2_EVENT_LOG,
            COMMUNICATION_EVENT_LOG,
            QUALITY_EVENT_LOG,
            CUT_EVENT_LOG,
            CURRENT_EVENT_LOG,
            DISCONNECTOR_EVENT_LOG,
            FIRMWARE_EVENT_LOG,
            PASSWORD_EVENT_LOG,
            SECURITY_EVENT_LOG,
            MAXDEMAND_EVENT_LOG,
    };

    public AcudElectricLogBookFactory(Acud protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(POWER_EVENT_LOG)) {
            meterEvents = new PowerEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(SYNCHRO_EVENT_LOG)) {
            meterEvents = new SynchroEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(COMMON_EVENT_LOG)) {
            meterEvents = new CommonEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MEMORY_EVENT_LOG)) {
            meterEvents = new MemoryEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(COMMUNICATION_EVENT_LOG)) {
            meterEvents = new CommEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(QUALITY_EVENT_LOG)) {
            meterEvents = new QualityEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(CUT_EVENT_LOG)) {
            meterEvents = new CutEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(CURRENT_EVENT_LOG)) {
            meterEvents = new CurrentEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(FIRMWARE_EVENT_LOG)) {
            meterEvents = new FirmwareEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(PASSWORD_EVENT_LOG)) {
            meterEvents = new PasswordEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(SECURITY_EVENT_LOG)) {
            meterEvents = new SecurityEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(DISCONNECTOR_EVENT_LOG)) {
            meterEvents = new DisconnectorEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(TAMPER1_EVENT_LOG)) {
            meterEvents = new Tamper1EventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(TAMPER2_EVENT_LOG)) {
            meterEvents = new Tamper2EventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(MAXDEMAND_EVENT_LOG)) {
            meterEvents = new MaxDemandEventLog(getProtocol().getTimeZone(), dataContainer).getMeterEvents();
        } else {
            return new ArrayList<>();
        }

        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents).stream().map(item -> {
            item.getEventType().setType(getProtocol().getTypeMeter());
            return item;
        }).collect(Collectors.toList());
    }

    protected List<ObisCode> getSupportedLogBooks() {
        return Arrays.asList(supportedLogBooks);
    }
}
