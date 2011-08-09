package com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.BasicEventLog;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300;
import com.energyict.smartmeterprotocolimpl.elster.apollo.ObisCodeProvider;

import java.util.*;
import java.util.logging.Logger;

/**
 * TODO the events are fetched without using the fromCalendar
 * Provides functionality to collect the events from the device and return them as a list of {@link com.energyict.protocol.MeterEvent}
 * Copyrights EnergyICT
 * Date: 2-dec-2010
 * Time: 13:01:44
 */
public class ApolloEventProfiles {

    private final AS300 meterProtocol;

    /**
     * Default constructor
     *
     * @param meterProtocol the meterProtocol to create events for
     */
    public ApolloEventProfiles(AS300 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Getter for the MeterEvent list
     *
     * @param fromCalendar the time to start collection events from the device
     * @return a list of MeterEvents
     */
    public List<MeterEvent> getEventLog(Calendar fromCalendar) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        meterEvents.addAll(getStandardEvents(fromCalendar));
        meterEvents.addAll(getFraudEvents(fromCalendar));
        meterEvents.addAll(getDisconnectControlEvents(fromCalendar));
        meterEvents.addAll(getFirmwareEvents(fromCalendar));
        meterEvents.addAll(getPowerQualityEvents(fromCalendar));
        meterEvents.addAll(getPowerFailureEvents(fromCalendar));
        meterEvents.addAll(getCommunicationFailureEvents(fromCalendar));
        meterEvents.addAll(getPrepaymentEvents(fromCalendar));
        meterEvents.addAll(getClockSyncEvents(fromCalendar));
        return meterEvents;
    }

    /**
     * Collect the events from the SynchronizationEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    private List<MeterEvent> getPrepaymentEvents(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.PREPAYMENT_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Collect the events from the PowerContractEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    private List<MeterEvent> getPowerFailureEvents(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.POWER_FAILURE_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }


    /**
     * Collect the events from the PowerQualityFinishedEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    private List<MeterEvent> getPowerQualityEvents(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.POWER_QUALITY_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Collect the events from the FirmwareEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    private List<MeterEvent> getFirmwareEvents(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.FIRMWARE_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Collect the events from the DemandManagementEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    private List<MeterEvent> getCommunicationFailureEvents(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.COMMUNICATION_FAILURE_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Collect the events from the CommonEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    private List<MeterEvent> getDisconnectControlEvents(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.DISCONNECT_CONTROL_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Collect the events from the FraudDetectionEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    private List<MeterEvent> getFraudEvents(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.FRAUD_DETECTION_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Collect the events from the StandardEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    protected List<MeterEvent> getStandardEvents(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.STANDARD_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Collect the events from the StandardEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    protected List<MeterEvent> getClockSyncEvents(Calendar from) {
        // TODO: Now we only use the device code & timestamp. We should use ALL info from the logbook later on
        BasicEventLog basicEventLog = new BasicEventLog(
                ObisCodeProvider.CLOCK_SYNC_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Getter for the {@link #meterProtocol}
     *
     * @return the {@link #meterProtocol}
     */
    private AS300 getMeterProtocol() {
        return this.meterProtocol;
    }

    /**
     * Getter for the AS300's cosemObjectFactory
     *
     * @return the CosemObjectFactory of the AS300
     */
    private CosemObjectFactory getCosemObjectFactory() {
        return getMeterProtocol().getDlmsSession().getCosemObjectFactory();
    }

    /**
     * The protocol logger
     *
     * @return the protocol logger object
     */
    private Logger getLogger() {
        return getMeterProtocol().getLogger();
    }

}

