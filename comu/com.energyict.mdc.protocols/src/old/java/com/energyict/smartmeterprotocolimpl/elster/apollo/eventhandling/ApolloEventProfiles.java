/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.EventUtils;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300ObisCodeProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

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
    public List<MeterEvent> getEventLog(Calendar fromCalendar) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        int logbookSelectorBitMask = meterProtocol.getProperties().getLogbookSelector();
        meterEvents.addAll(((logbookSelectorBitMask & 0x01) == 0x01) ? getStandardEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x02) == 0x02) ? getFraudEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x04) == 0x04) ? getDisconnectControlEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x08) == 0x08) ? getFirmwareEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x10) == 0x10) ? getPowerQualityEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x20) == 0x20) ? getPowerFailureEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x40) == 0x40) ? getCommunicationFailureEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x80) == 0x80) ? getTariffUpdateEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x100) == 0x100) ? getClockSyncEvents(fromCalendar) : new ArrayList<MeterEvent>());
        EventUtils.removeDuplicateEvents(meterEvents);
        EventUtils.removeStoredEvents(meterEvents, fromCalendar.getTime());
        return meterEvents;
    }

    /**
     * Collect the events from the SynchronizationEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    private List<MeterEvent> getPrepaymentEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.PREPAYMENT_EVENTLOG_OBISCODE,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Collect the events from the SynchronizationEvents logbook
     *
     * @param from the time to start collecting events from
     * @return the List of meterEvents
     */
    private List<MeterEvent> getTariffUpdateEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.TARIFF_UPDATE_EVENTLOG_OBISCODE,
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
    private List<MeterEvent> getPowerFailureEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.POWER_FAILURE_EVENTLOG_OBISCODE,
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
    private List<MeterEvent> getPowerQualityEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.POWER_QUALITY_EVENTLOG_OBISCODE,
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
    private List<MeterEvent> getFirmwareEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.FIRMWARE_EVENTLOG_OBISCODE,
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
    private List<MeterEvent> getCommunicationFailureEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.COMMUNICATION_FAILURE_EVENTLOG_OBISCODE,
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
    private List<MeterEvent> getDisconnectControlEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.DISCONNECT_CONTROL_EVENTLOG_OBISCODE,
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
    private List<MeterEvent> getFraudEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.FRAUD_DETECTION_EVENTLOG_OBISCODE,
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
    protected List<MeterEvent> getStandardEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.STANDARD_EVENTLOG_OBISCODE,
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
    protected List<MeterEvent> getClockSyncEvents(Calendar from) throws IOException {
        AS300EventLog basicEventLog = new AS300EventLog(
                AS300ObisCodeProvider.CLOCK_SYNC_EVENTLOG_OBISCODE,
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

