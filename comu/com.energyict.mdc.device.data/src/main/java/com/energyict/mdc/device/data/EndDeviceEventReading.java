package com.energyict.mdc.device.data;

import java.util.Date;

/**
 * Created by bvn on 8/21/14.
 */
public interface EndDeviceEventReading {
    public Date getEventDate();
    public Date getReadingDate();
    public Long getEventLogId();
    public String getCimCode();
    public String getDeviceType();
    public com.elster.jupiter.cbo.EndDeviceDomain getDomain();
    public com.elster.jupiter.cbo.EndDeviceSubDomain getSubDomain();
    public com.elster.jupiter.cbo.EndDeviceEventorAction getEventOrAction();
    public String getCode();
    public String getDeviceCode();
    public String getMessage();
}
