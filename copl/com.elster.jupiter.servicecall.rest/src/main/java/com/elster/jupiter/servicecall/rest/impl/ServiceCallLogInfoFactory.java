/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallLog;

import javax.inject.Inject;

/**
 * Created by bvn on 3/2/16.
 */
public class ServiceCallLogInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ServiceCallLogInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ServiceCallLogInfo from(ServiceCallLog serviceCallLog) {
        ServiceCallLogInfo info = new ServiceCallLogInfo();
        info.logLevel = thesaurus.getFormat(serviceCallLog.getLogLevel()).format();
        info.message = serviceCallLog.getMessage();
        info.timestamp = serviceCallLog.getTime();
        return info;
    }
}
