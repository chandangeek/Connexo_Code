package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.SocketFactory;
import java.util.concurrent.atomic.AtomicReference;

public interface SocketFactoryProvider {
    public static final AtomicReference<SocketFactory> instance = new AtomicReference<>();

    public SocketFactory getSocketFactory();
}
