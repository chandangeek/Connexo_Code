package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointProvider;

/**
 * Created by bvn on 5/11/16.
 */
public abstract class WebServiceImpl<T extends EndPointProvider> implements WebService<T> {
    private String name;
    private T endPointProvider;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setEndPointProvider(T endPointProvider) {
        this.endPointProvider = endPointProvider;
    }

    @Override
    public T getEndPointProvider() {
        return this.endPointProvider;
    }

    @Override
    public WebService<T> init(String name, T endPointProvider) {
        this.setName(name);
        this.setEndPointProvider(endPointProvider);
        return this;
    }
}
