package com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.AS300DPET;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.CommonEvents;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.DemandManagementEvents;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.DisconnectControlEvents;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.FirmwareEvents;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.FraudDetectionEvents;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.PowerContractEvents;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.PowerQualityFinishedEvents;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.PowerQualityNotFinishedEvents;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.StandardEvents;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.SynchronizationEvents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * TODO the events are fetched without using the fromCalendar
 * Provides functionality to collect the events from the device and return them as a list of {@link com.energyict.protocol.MeterEvent}
 * <p/>
 * Copyrights EnergyICT
 * Date: 2-dec-2010
 * Time: 13:01:44
 */
public class EventLogs {

    /**
     * The used meterProtocol
     */
    private final AS300DPET meterProtocol;

    /**
     * The list of all MeterEvents
     */
    private List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

    /**
     * Default constructor
     *
     * @param meterProtocol the meterProtocol to create events for
     */
    public EventLogs(AS300DPET meterProtocol) {
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
            getPowerQualityNotFinishedEvents(fromCalendar);
            getDisconnectControlEvents(fromCalendar);
        } catch (IOException e) {
            getMeterProtocol().getLogger().info("Failed to read one of the Event profiles, events will not be stored. " + e.getMessage());
            throw e;
        }
        return this.meterEvents;
    }

    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.SynchronizationEvents}
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
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.PowerContractEvents}
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
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.PowerQualityFinishedEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    private void getPowerQualityEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getPowerQualityFinishedEventLog().getBuffer(fromCalendar);
        PowerQualityFinishedEvents pqfe = new PowerQualityFinishedEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(pqfe.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.FirmwareEvents}
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
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.DemandManagementEvents}
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
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.CommonEvents}
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
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.FraudDetectionEvents}
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
     * Collect the events from the {@link com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups.StandardEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws java.io.IOException
     */
    protected void getStandardEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getStandardEventLog().getBuffer(fromCalendar);
        StandardEvents se = new StandardEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(se.getMeterEvents());
    }

    protected void getPowerQualityNotFinishedEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getPowerQualityNotFinishedEventLog().getBuffer(fromCalendar);
        PowerQualityNotFinishedEvents se = new PowerQualityNotFinishedEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(se.getMeterEvents());
    }

    protected void getDisconnectControlEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getObjectFactory().getDisconnectControlLog().getBuffer(fromCalendar);
        DisconnectControlEvents se = new DisconnectControlEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(se.getMeterEvents());
    }

    /**
     * Getter for the {@link #meterProtocol}
     *
     * @return the {@link #meterProtocol}
     */
    private AS300DPET getMeterProtocol() {
        return this.meterProtocol;
    }
}

