package com.energyict.mdc.device.data.crlrequest.rest;

import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfo;

public class CrlRequestTaskInfo {
    public Long id;
    public LongIdWithNameInfo deviceGroup;
    public SecurityAccessorInfo securityAccessor;
    public String certificateAlias;
    public String caName;
    public String requestFrequency;
    public long version;
}
