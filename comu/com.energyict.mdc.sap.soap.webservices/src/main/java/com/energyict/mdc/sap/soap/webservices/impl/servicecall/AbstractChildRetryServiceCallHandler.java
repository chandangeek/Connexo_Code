/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.RetrySearchDataSourceDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.MessageFormat;

public abstract class AbstractChildRetryServiceCallHandler implements ServiceCallHandler {
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    protected volatile SAPCustomPropertySets sapCustomPropertySets;
    protected volatile WebServiceActivator webServiceActivator;

    public AbstractChildRetryServiceCallHandler() {
    }

    @Inject
    public AbstractChildRetryServiceCallHandler(SAPCustomPropertySets sapCustomPropertySets, WebServiceActivator webServiceActivator) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.webServiceActivator = webServiceActivator;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case ONGOING:
                processServiceCall(serviceCall);
                break;
            case CANCELLED:
                cancelServiceCall(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    protected abstract RetrySearchDataSourceDomainExtension getMasterDomainExtension(ServiceCall serviceCall);

    protected abstract void processServiceCall(ServiceCall serviceCall);

    protected void cancelServiceCall(ServiceCall serviceCall) {
    }

    protected void setError(ServiceCall serviceCall, MessageSeed error, Object... args) {
    }

    protected void failServiceCall(ServiceCall serviceCall, MessageSeed messageSeed, Object... args) {
        serviceCall.log(LogLevel.SEVERE, MessageFormat.format(messageSeed.getDefaultFormat(), args));
        setError(serviceCall, messageSeed, args);
        serviceCall.requestTransition(DefaultState.FAILED);
    }

    protected void failedAttempt(ServiceCall serviceCall, MessageSeeds error, Object... args) {
        RetrySearchDataSourceDomainExtension masterExtension = getMasterDomainExtension(serviceCall);
        BigDecimal attempts = new BigDecimal(webServiceActivator.getSapProperty(AdditionalProperties.OBJECT_SEARCH_ATTEMPTS));
        BigDecimal currentAttempt = masterExtension.getAttemptNumber();
        if (currentAttempt.compareTo(attempts) != -1) {
            serviceCall.log(LogLevel.SEVERE, MessageFormat.format(error.getDefaultFormat(), args));
            setError(serviceCall, error, args);
            serviceCall.requestTransition(DefaultState.FAILED);
        } else {
            serviceCall.log(LogLevel.WARNING, MessageFormat.format(error.getDefaultFormat(), args));
            serviceCall.requestTransition(DefaultState.PAUSED);
        }
    }
}
