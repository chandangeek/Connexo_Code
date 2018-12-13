/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;

/**
 * Created by bvn on 5/11/16.
 */
public interface EndPointFactory<T extends EndPointProvider> {
    void setName(String name);

    String getName();

    void setEndPointProvider(T endPointProvider);

    T getEndPointProvider();

    EndPointFactory<T> init(String name, T endPointProvider);

    ManagedEndpoint createEndpoint(EndPointConfiguration endPointConfiguration);

    boolean isInbound();

    WebServiceProtocol getProtocol();
}
