package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.events;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.BasicEventLog;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas;

import java.util.*;
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

    public List<MeterEvent> getEvents(Date from) {
        ArrayList<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + getZigbeeGas().getSerialNumber() + ".");
        if (from == null) {
            from = ProtocolUtils.getClearLastMonthDate(this.zigbeeGas.getTimeZone());
        }
        Calendar fromCalendar = getFromCalendar(from);
        meterEvents.addAll(getStandardEventLog(fromCalendar));
        meterEvents.addAll(getDisconnectControlEventLog(fromCalendar));
        meterEvents.addAll(getFraudDetectionEventLog(fromCalendar));
        meterEvents.addAll(getFirmwareEventLog(fromCalendar));
        meterEvents.addAll(getCommFailureEventLog(fromCalendar));
        meterEvents.addAll(getPrepaymentEventLog(fromCalendar));
        meterEvents.addAll(getNotificationEventLog(fromCalendar));
        meterEvents.addAll(getTariffUpdatesEventLog(fromCalendar));
        meterEvents.addAll(getMirrorUpdatesEventLog(fromCalendar));

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

    private List<MeterEvent> getStandardEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.STANDARD_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFraudDetectionEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.FRAUD_DETECTION_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getDisconnectControlEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.DISCONNECT_CONTROL_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFirmwareEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.FIRMWARE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getCommFailureEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.COMM_FAILURE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getPrepaymentEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.PREPAYMENT_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getNotificationEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.NOTIFICATION_FLAGS_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getTariffUpdatesEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.TARIFF_UPDATES_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getMirrorUpdatesEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.MIRROR_UPDATES_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

}
