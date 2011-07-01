package com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300;

import java.io.IOException;
import java.util.*;

/**
 * TODO the events are fetched without using the fromCalendar
 * Provides functionality to collect the events from the device and return them as a list of {@link com.energyict.protocol.MeterEvent}
 * Copyrights EnergyICT
 * Date: 2-dec-2010
 * Time: 13:01:44
 */
public class EventLogs {

    private final AS300 meterProtocol;
    private List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

    /**
     * Default constructor
     *
     * @param meterProtocol the meterProtocol to create events for
     */
    public EventLogs(AS300 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Getter for the MeterEvent list
     *
     * @param fromCalendar the time to start collection events from the device
     * @return a list of MeterEvents
     * @throws java.io.IOException when something happened during the read or parsing of the events
     */
    public List<MeterEvent> getEventLog(Calendar fromCalendar) throws IOException {
        try {
            getStandardEvents(fromCalendar);
            getFraudEvents(fromCalendar);
            getCommonEvents(fromCalendar);
            getDemandManagementEvents(fromCalendar);
            getFirmwareEvents(fromCalendar);
            getPowerContractEvents(fromCalendar);
            getPowerQualityEvents(fromCalendar);
            getObjectSynchronizationEvents(fromCalendar);
        } catch (IOException e) {
            getMeterProtocol().getLogger().info("Failed to read one of the Event profiles, events will not be stored. " + e.getMessage());
            throw e;
        }
        return this.meterEvents;
    }

    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.SynchronizationEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    private void getObjectSynchronizationEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getObjectSynchronizationEventLog().getBuffer(fromCalendar);
        SynchronizationEvents soe = new SynchronizationEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(soe.getMeterEvents());
    }


    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.PowerContractEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    private void getPowerContractEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getPowerContractEventLog().getBuffer(fromCalendar);
        PowerContractEvents pce = new PowerContractEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(pce.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.PowerQualityFinishedEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    private void getPowerQualityEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getPowerQualityEventLog().getBuffer(fromCalendar);
        PowerQualityFinishedEvents pqfe = new PowerQualityFinishedEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(pqfe.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.FirmwareEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    private void getFirmwareEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getFirmwareEventLog().getBuffer(fromCalendar);
        FirmwareEvents fe = new FirmwareEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(fe.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.DemandManagementEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    private void getDemandManagementEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getDemandManagementEventLog().getBuffer(fromCalendar);
        DemandManagementEvents dme = new DemandManagementEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(dme.getMeterEvents());

    }

    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.CommonEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    private void getCommonEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getCommonEventLog().getBuffer(fromCalendar);
        CommonEvents ce = new CommonEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(ce.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.FraudDetectionEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    private void getFraudEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getFraudDetectionEventLog().getBuffer(fromCalendar);
        FraudDetectionEvents fde = new FraudDetectionEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(fde.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.StandardEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    protected void getStandardEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getStandardEventLog().getBuffer(fromCalendar);
        StandardEvents se = new StandardEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(se.getMeterEvents());
    }

    /**
     * Getter for the {@link #meterProtocol}
     *
     * @return the {@link #meterProtocol}
     */
    private AS300 getMeterProtocol() {
        return this.meterProtocol;
    }
}

