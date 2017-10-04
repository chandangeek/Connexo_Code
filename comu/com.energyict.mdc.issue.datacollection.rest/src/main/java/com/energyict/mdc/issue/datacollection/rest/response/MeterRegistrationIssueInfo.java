/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MeterRegistrationIssueInfo<T extends DeviceInfo> extends DataCollectionIssueInfo<T> {

    //MDC device mRID
    public String master;
    public String masterUsagePoint;
    public IdWithNameInfo masterState;
    public IdWithNameInfo masterDeviceType;
    public IdWithNameInfo masterDeviceConfig;
    public Instant masterFrom;
    public Instant masterTo;
    public List<GatewayInfo> gateways;


    public MeterRegistrationIssueInfo(IssueDataCollection issue, Class<T> deviceInfoClass){
        super(issue, deviceInfoClass);
        gateways = new ArrayList<>();
    }
}
