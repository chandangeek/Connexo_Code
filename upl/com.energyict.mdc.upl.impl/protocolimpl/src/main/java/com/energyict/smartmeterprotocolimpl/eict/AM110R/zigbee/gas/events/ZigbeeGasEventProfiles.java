package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.events;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.BasicEventLog;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.EventUtils;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.ZigbeeGas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 22/07/11
 * Time: 9:56
 */
public class ZigbeeGasEventProfiles {

    private final ZigbeeGas zigbeeGas;

    public ZigbeeGasEventProfiles(ZigbeeGas zigbeeGas) {
        this.zigbeeGas = zigbeeGas;
    }

    public ZigbeeGas getZigbeeGas() {
        return zigbeeGas;
    }

    public List<MeterEvent> getEvents(Date from) throws IOException {
        ArrayList<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        getLogger().log(Level.INFO, "Reading EVENTS from G-meter with serial number " + getZigbeeGas().getProperties().getSerialNumber() + ".");
        if (from == null) {
            from = ProtocolUtils.getClearLastMonthDate(this.zigbeeGas.getTimeZone());
        }
        Calendar fromCalendar = getFromCalendar(from);
        int logbookSelectorBitMask = zigbeeGas.getProperties().getLogbookSelector();

        meterEvents.addAll(((logbookSelectorBitMask &  0x01) == 0x01) ? getStandardEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x02) == 0x02) ? getFraudDetectionEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x04) == 0x04) ? getDisconnectControlEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x08) == 0x08) ? getFirmwareEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x10) == 0x10) ? getPowerQualityEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x20) == 0x20) ? getCommFailureEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x40) == 0x40) ? getPrepaymentEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x80) == 0x80) ? getCoTSEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x100) == 0x100) ? getManufacturerEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        EventUtils.removeDuplicateEvents(meterEvents);
        EventUtils.removeStoredEvents(meterEvents, fromCalendar.getTime());        
        return meterEvents;
    }

    private Calendar getFromCalendar(Date from) {
        Calendar fromCal = ProtocolUtils.getCleanCalendar(getZigbeeGas().getTimeZone());
        fromCal.setTime(from);
        return fromCal;
    }

    private Logger getLogger() {
        return getZigbeeGas().getLogger();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getZigbeeGas().getDlmsSession().getCosemObjectFactory();
    }

    private List<MeterEvent> getStandardEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.STANDARD_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFraudDetectionEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.FRAUD_DETECTION_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getDisconnectControlEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.DISCONNECT_CONTROL_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFirmwareEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.FIRMWARE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getPowerQualityEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.POWER_QUALITY_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getCommFailureEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.COMM_FAILURE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getPrepaymentEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.PREPAYMENT_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getCoTSEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.COTS_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getManufacturerEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.MANUFACTURER_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }
}
