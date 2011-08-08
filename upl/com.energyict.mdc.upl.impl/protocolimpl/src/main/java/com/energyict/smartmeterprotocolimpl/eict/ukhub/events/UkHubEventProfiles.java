package com.energyict.smartmeterprotocolimpl.eict.ukhub.events;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.BasicEventLog;

import java.util.*;
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

    public List<MeterEvent> getEvents(Date from) {
        ArrayList<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        getLogger().log(Level.INFO, "Reading EVENTS from HUB with serial number " + getUkHub().getSerialNumber() + ".");

        Calendar fromCalendar = getFromCalendar(from);
        meterEvents.addAll(getStandardEventLog(fromCalendar));
        meterEvents.addAll(getFraudDetectionEventLog(fromCalendar));
        meterEvents.addAll(getFirmwareEventLog(fromCalendar));
        meterEvents.addAll(getHanManagementFailureEventLog(fromCalendar));
        meterEvents.addAll(getCommunicationsFailureEventLog(fromCalendar));

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

    private List<MeterEvent> getFirmwareEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.FIRMWARE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getHanManagementFailureEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.HAN_MANAGEMENT_FAILURE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getCommunicationsFailureEventLog(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.COMM_FAILURE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }


}
