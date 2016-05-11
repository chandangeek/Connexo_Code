package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointProvider;

/**
 * Created by bvn on 5/11/16.
 */
public interface WebService<T extends EndPointProvider> {
    void setName(String name);

    String getName();

    void setEndPointProvider(T endPointProvider);

    T getEndPointProvider();

    WebService<T> init(String name, T endPointProvider);

    ManagedEndpoint createEndpoint();

    boolean isInbound();
}
