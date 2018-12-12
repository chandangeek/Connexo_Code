package com.energyict.protocolimplv2.nta.esmr50.common.events;


import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.FraudDetectionLog;


/**
 * Extends the original DSMR4.0 FraudDetectionLog with additional events for ESMR5.0
 */

public class ESMR50FraudDetectionLog extends FraudDetectionLog {
    public ESMR50FraudDetectionLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }
    public ESMR50FraudDetectionLog(DataContainer dc) {
        super(dc);
    }
}
