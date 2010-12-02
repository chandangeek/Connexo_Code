package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocol.MeterEvent;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 2-dec-2010
 * Time: 13:01:44
 */
public class EventLogs {

    private final ApolloMeter meterProtocol;

    public EventLogs(ApolloMeter meterProtocol){
        this.meterProtocol = meterProtocol;
    }

    public List<MeterEvent> getEventLog(Calendar fromCalendar, Calendar toCalendar) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        meterEvents.addAll(readLogBook(getMeterProtocol().getApolloObjectFactory().getStandardEventLog(), fromCalendar, toCalendar));
        meterEvents.addAll(readLogBook(getMeterProtocol().getApolloObjectFactory().getFraudDetectionEventLog(), fromCalendar, toCalendar));
        meterEvents.addAll(readLogBook(getMeterProtocol().getApolloObjectFactory().getPowerQualityEventLog(), fromCalendar, toCalendar));
        meterEvents.addAll(readLogBook(getMeterProtocol().getApolloObjectFactory().getDemandManagementEventLog(), fromCalendar, toCalendar));
        meterEvents.addAll(readLogBook(getMeterProtocol().getApolloObjectFactory().getCommonEventLog(), fromCalendar, toCalendar));
        meterEvents.addAll(readLogBook(getMeterProtocol().getApolloObjectFactory().getPowerContractEventLog(), fromCalendar, toCalendar));
        meterEvents.addAll(readLogBook(getMeterProtocol().getApolloObjectFactory().getFirmwareEventLog(), fromCalendar, toCalendar));
        meterEvents.addAll(readLogBook(getMeterProtocol().getApolloObjectFactory().getObjectSynchronizationEventLog(), fromCalendar, toCalendar));
        return meterEvents;
    }

    private List<MeterEvent> readLogBook(ProfileGeneric standardEventLog, Calendar fromCalendar, Calendar toCalendar) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private ApolloMeter getMeterProtocol(){
        return this.meterProtocol;
    }
}

