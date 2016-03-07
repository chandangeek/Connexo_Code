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
            try {
                serviceCall.requestTransition(DefaultState.FAILED);
            } catch (RuntimeException e2) {
                logException(serviceCall, e2);
                ((ServiceCallImpl) serviceCall).setState(DefaultState.FAILED);
            }

            return false;
        }
    }

    private void logException(ServiceCall serviceCall, RuntimeException e) {
        NlsMessageFormat format = thesaurus
                .getFormat(MessageSeeds.SERVICE_CALL_HANDLER_FAILURE);
        serviceCall.log(format.format(), e);
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        try {
            delegate.onStateChange(serviceCall, oldState, newState);
        } catch (RuntimeException e) { // safety net for buggy ServiceCallHandler implementations
            logException(serviceCall, e);
            try {
                serviceCall.requestTransition(DefaultState.FAILED);
            } catch (RuntimeException e2) {
                logException(serviceCall, e2);
                ((ServiceCallImpl) serviceCall).setState(DefaultState.FAILED);
            }
        }
    }

    @Override
    public void onChildStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        try {
            delegate.onChildStateChange(serviceCall, oldState, newState);
        } catch (RuntimeException e) {
            logException(serviceCall.getParent().get(), e);
            try {
                serviceCall.requestTransition(DefaultState.FAILED);
            } catch (RuntimeException e2) {
                logException(serviceCall, e2);
                ((ServiceCallImpl) serviceCall).setState(DefaultState.FAILED);
            }
        }
    }
}
