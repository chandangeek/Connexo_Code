package com.energyict.smartmeterprotocolimpl.eict.ukhub.events;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.BasicEventLog;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.EventUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 8/08/11
 * Time: 8:35
 */
public class UkHubEventProfiles {

    private final UkHub ukHub;

    public UkHubEventProfiles(UkHub ukHub) {
        this.ukHub = ukHub;
    }

    public List<MeterEvent> getEvents(Date from) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();
        getLogger().log(Level.INFO, "Reading EVENTS from HUB with serial number " + getUkHub().getSerialNumber() + ".");

        if(from == null){
            from = ProtocolUtils.getClearLastMonthDate(this.ukHub.getTimeZone());
        }
        Calendar fromCalendar = getFromCalendar(from);

        int logbookSelectorBitMask = ukHub.getProperties().getLogbookSelector();
        meterEvents.addAll(((logbookSelectorBitMask &  0x01) == 0x01) ? getStandardEventLog(fromCalendar) : new ArrayList<>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x02) == 0x02) ? getFraudDetectionEventLog(fromCalendar) : new ArrayList<>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x04) == 0x04) ? getFirmwareEventLog(fromCalendar) : new ArrayList<>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x08) == 0x08) ? getHanManagementFailureEventLog(fromCalendar) : new ArrayList<>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x10) == 0x10) ? getCommunicationsFailureEventLog(fromCalendar) : new ArrayList<>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x20) == 0x20) ? getManufacturerLogbook(fromCalendar) : new ArrayList<>());
        EventUtils.removeDuplicateEvents(meterEvents);
        EventUtils.removeStoredEvents(meterEvents, fromCalendar.getTime());
        return meterEvents;
    }

    private Calendar getFromCalendar(Date from) {
        Calendar fromCal = ProtocolUtils.getCleanCalendar(getUkHub().getTimeZone());
        fromCal.setTime(from);
        return fromCal;
    }

    private Logger getLogger() {
        return getUkHub().getLogger();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getUkHub().getDlmsSession().getCosemObjectFactory();
    }

    public UkHub getUkHub() {
        return ukHub;
    }

    private List<MeterEvent> getStandardEventLog(Calendar from) throws IOException {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.STANDARD_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFraudDetectionEventLog(Calendar from) throws IOException {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.FRAUD_DETECTION_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFirmwareEventLog(Calendar from) throws IOException {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.FIRMWARE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getHanManagementFailureEventLog(Calendar from) throws IOException {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.HAN_MANAGEMENT_FAILURE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getCommunicationsFailureEventLog(Calendar from) throws IOException {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.COMM_FAILURE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getManufacturerLogbook(Calendar from) throws IOException {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.MANUFACTURER_SPECIFIC_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }
}
