/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.Map;

@ProviderType
public interface OutboundEndPointProvider extends EndPointProvider {
    RequestSender using(String methodName);

    @ProviderType
    interface RequestSender {
        RequestSender toEndpoints(Collection<EndPointConfiguration> endPointConfigurations);

        RequestSender toEndpoints(EndPointConfiguration... endPointConfigurations);

        RequestSender toAllEndpoints();

        Map<EndPointConfiguration, ?> send(Object request);

        void send(String message);

    }
}
