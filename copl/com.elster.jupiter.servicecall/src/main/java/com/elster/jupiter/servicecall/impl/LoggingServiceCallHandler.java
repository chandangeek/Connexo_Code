/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

class LoggingServiceCallHandler implements ServiceCallHandler {

    private final Thesaurus thesaurus;
    private final ServiceCallHandler delegate;

    LoggingServiceCallHandler(ServiceCallHandler delegate, Thesaurus thesaurus) {
        this.delegate = delegate;
        this.thesaurus = thesaurus;
    }

    @Override
    public boolean allowStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        try {
            return delegate.allowStateChange(serviceCall, oldState, newState);
        } catch (RuntimeException e) { // safety net for buggy ServiceCallHandler implementations
            logException(serviceCall, e);
            return false;
        }
    }

    private void logException(ServiceCall serviceCall, RuntimeException e) {
        NlsMessageFormat format = thesaurus.getFormat(MessageSeeds.SERVICE_CALL_HANDLER_FAILURE);
        serviceCall.log(format.format(), e);
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        try {
            delegate.onStateChange(serviceCall, oldState, newState);
        } catch (RuntimeException e) { // safety net for buggy ServiceCallHandler implementations
            logException(serviceCall, e);
            throw e;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        try {
            delegate.onChildStateChange(parentServiceCall, childServiceCall, oldState, newState);
        } catch (RuntimeException e) {
            logException(childServiceCall.getParent().get(), e);
            throw e;
        }
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }
}
