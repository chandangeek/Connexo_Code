package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.util.time.Interval;

public class EndDeviceEventRecordFilterSpecification {
    
    public long logBookId = -1L;
    
    public Interval interval = Interval.sinceEpoch();
    
    public EndDeviceDomain domain = null;
    
    public EndDeviceSubDomain subDomain = null;
    
    public EndDeviceEventorAction eventOrAction = null;

}
