package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloMeter;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 2-dec-2010
 * Time: 13:01:44
 */
public class EventLogs {

    private final ApolloMeter meterProtocol;

    private List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

    public EventLogs(ApolloMeter meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * TODO the fromCalendar should be used
     *
     * @param fromCalendar
     * @return
     * @throws IOException
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


    private void getObjectSynchronizationEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getObjectSynchronizationEventLog().getBuffer();
        SynchronizationEvents soe = new SynchronizationEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(soe.getMeterEvents());
    }


    private void getPowerContractEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getPowerContractEventLog().getBuffer();
        PowerContractEvents pce = new PowerContractEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(pce.getMeterEvents());
    }


    private void getPowerQualityEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getPowerQualityEventLog().getBuffer();
        PowerQualityFinishedEvents pqfe = new PowerQualityFinishedEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(pqfe.getMeterEvents());
    }

    private void getFirmwareEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getFirmwareEventLog().getBuffer();
        FirmwareEvents fe = new FirmwareEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(fe.getMeterEvents());
    }

    private void getDemandManagementEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getDemandManagementEventLog().getBuffer();
        DemandManagementEvents dme = new DemandManagementEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(dme.getMeterEvents());

    }

    private void getCommonEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getCommonEventLog().getBuffer();
        CommonEvents ce = new CommonEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(ce.getMeterEvents());
    }

    private void getFraudEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getFraudDetectionEventLog().getBuffer();
        FraudDetectionEvents fde = new FraudDetectionEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(fde.getMeterEvents());
    }

    private ApolloMeter getMeterProtocol() {
        return this.meterProtocol;
    }

    protected void getStandardEvents(Calendar fromCalendar) throws IOException {
        DataContainer dc = getMeterProtocol().getApolloObjectFactory().getStandardEventLog().getBuffer();
        StandardEvents se = new StandardEvents(dc, getMeterProtocol().getTimeZone());
        meterEvents.addAll(se.getMeterEvents());
    }
}

