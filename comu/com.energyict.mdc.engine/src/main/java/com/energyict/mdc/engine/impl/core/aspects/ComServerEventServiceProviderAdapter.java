package com.energyict.mdc.engine.impl.core.aspects;

import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;

import com.elster.jupiter.util.time.Clock;

/**
* Adapts the {@link com.energyict.mdc.engine.impl.core.ServiceProvider}
* to the {@link AbstractComServerEventImpl.ServiceProvider} interface.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-08-19 (10:16)
*/
public class ComServerEventServiceProviderAdapter implements AbstractComServerEventImpl.ServiceProvider {

    private final com.energyict.mdc.engine.impl.core.ServiceProvider delegate;

    public ComServerEventServiceProviderAdapter() {
        this.delegate = ServiceProvider.instance.get();
    }

    @Override
    public Clock clock() {
        return delegate.clock();
    }

}