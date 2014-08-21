package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.protocol.api.services.HexService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides access to the OSGi services that are needed by
 * the core ComServer components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-08 (09:32)
 */
public interface ServiceProvider
    extends
        RunningComServerImpl.ServiceProvider,
        ComServerDAOImpl.ServiceProvider {

    public final AtomicReference<ServiceProvider> instance = new AtomicReference<>();

}