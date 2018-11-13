package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.events;


import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.FraudDetectionLog;

/**
 * Extends the original DSMR4.0 FraudDetectionLog with additional events for ESMR5.0
 */
@Deprecated
public class ESMR50FraudDetectionLog extends FraudDetectionLog{
    public ESMR50FraudDetectionLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }
}
