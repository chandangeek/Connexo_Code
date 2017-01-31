/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;

import javax.inject.Inject;

public class EndpointConfigurationLogInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public EndpointConfigurationLogInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public EndpointConfigurationLogInfo from(EndPointLog endPointLog) {
        EndpointConfigurationLogInfo info = new EndpointConfigurationLogInfo();
        info.logLevel = thesaurus.getString(endPointLog.getLogLevel().getKey(), endPointLog.getLogLevel()
                .getDefaultFormat());
        info.message = endPointLog.getMessage();
        info.timestamp = endPointLog.getTime();
        return info;
    }
}
