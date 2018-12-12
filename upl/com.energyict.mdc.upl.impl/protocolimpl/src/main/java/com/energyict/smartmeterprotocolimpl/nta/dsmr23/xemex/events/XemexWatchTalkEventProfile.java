package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.events;

import com.energyict.dlms.DataContainer;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.EventsLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.FraudDetectionLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;

/**
 * @author sva
 * @since 19/03/14 - 14:28
 */
public class XemexWatchTalkEventProfile extends EventProfile {

    public XemexWatchTalkEventProfile(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    protected EventsLog getStandardEventsLog(DataContainer dcEvent) {
        return new XemeWatchTalkStandardEventLog(dcEvent, this.protocol.getDateTimeDeviationType());
    }

    @Override
    protected FraudDetectionLog getFraudDetectionLog(DataContainer dcFraudDetection) {
        return new XemexWatchTalkFraudDetectionLog(dcFraudDetection, this.protocol.getDateTimeDeviationType());
    }
}
