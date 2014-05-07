package com.energyict.mdc.engine.impl.events;

/**
 * Provides an implementation for the {@link FilteringEventReceiverFactory}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (17:43)
 */
public class FilteringEventReceiverFactoryImpl implements FilteringEventReceiverFactory {

    @Override
    public FilteringEventReceiver newFor (EventReceiver eventReceiver) {
        return new FilteringEventReceiverImpl(eventReceiver);
    }

}