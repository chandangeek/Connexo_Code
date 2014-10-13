package com.elster.jupiter.metering;

import java.time.Instant;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.google.common.collect.Range;

public class EndDeviceEventRecordFilterSpecification {
    
    public long logBookId = -1L;
    
    public Range<Instant> range = Range.all();
    
    public EndDeviceDomain domain = null;
    
    public EndDeviceSubDomain subDomain = null;
    
    public EndDeviceEventorAction eventOrAction = null;

}
