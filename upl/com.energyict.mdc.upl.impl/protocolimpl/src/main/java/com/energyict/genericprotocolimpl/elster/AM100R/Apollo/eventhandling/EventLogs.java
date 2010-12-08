package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloMeter;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.*;

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
    private final ApolloMeter meterProtocol;

    /**
     * The list of all MeterEvents
     */
    private List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

    /**
     * Default constructor
     *
     * @param meterProtocol the meterProtocol to create events for
     */
    public EventLogs(ApolloMeter meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Getter for the MeterEvent list
     *
     * @param fromCalendar the time to start collection events from the device
     * @return a list of MeterEvents
     * @throws IOException when something happened during the read or parsing of the events
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
     * Collect the events from the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.SynchronizationEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws IOException
     */
    private void getObjectSynchronizationEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getObjectSynchronizationEventLog().getBuffer();
        SynchronizationEvents soe = new SynchronizationEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(soe.getMeterEvents());
    }


    /**
     * Collect the events from the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.PowerContractEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws IOException
     */
    private void getPowerContractEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getPowerContractEventLog().getBuffer();
        PowerContractEvents pce = new PowerContractEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(pce.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.PowerQualityFinishedEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws IOException
     */
    private void getPowerQualityEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getPowerQualityEventLog().getBuffer();
        PowerQualityFinishedEvents pqfe = new PowerQualityFinishedEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(pqfe.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.FirmwareEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws IOException
     */
    private void getFirmwareEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getFirmwareEventLog().getBuffer();
        FirmwareEvents fe = new FirmwareEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(fe.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.DemandManagementEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws IOException
     */
    private void getDemandManagementEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getDemandManagementEventLog().getBuffer();
        DemandManagementEvents dme = new DemandManagementEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(dme.getMeterEvents());

    }

    /**
     * Collect the events from the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.CommonEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws IOException
     */
    private void getCommonEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getCommonEventLog().getBuffer();
        CommonEvents ce = new CommonEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(ce.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.FraudDetectionEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws IOException
     */
    private void getFraudEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getFraudDetectionEventLog().getBuffer();
        FraudDetectionEvents fde = new FraudDetectionEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(fde.getMeterEvents());
    }

    /**
     * Collect the events from the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.StandardEvents}
     *
     * @param fromCalendar the time to start collecting events from
     * @throws IOException
     */
    protected void getStandardEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getStandardEventLog().getBuffer();
        StandardEvents se = new StandardEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(se.getMeterEvents());
    }

    /**
     * Getter for the {@link #meterProtocol}
     *
     * @return the {@link #meterProtocol}
     */
    private ApolloMeter getMeterProtocol() {
        return this.meterProtocol;
    }
}

