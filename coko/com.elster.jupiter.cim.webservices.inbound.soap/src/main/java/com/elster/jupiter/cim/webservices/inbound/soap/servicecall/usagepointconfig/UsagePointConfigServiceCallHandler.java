/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.parent.AbstractServiceCallHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.Action;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.UsagePointBuilder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.usagepointconfig.UsagePoint;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.time.Instant;
import java.util.Optional;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS UsagePointConfig
 */
public class UsagePointConfigServiceCallHandler extends AbstractServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "UsagePointConfigServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = null;

    private final Provider<UsagePointBuilder> usagePointBuilderProvider;
    private final JsonService jsonService;

    @Inject
    public UsagePointConfigServiceCallHandler(Provider<UsagePointBuilder> usagePointBuilderProvider,
            JsonService jsonService) {
        this.usagePointBuilderProvider = usagePointBuilderProvider;
        this.jsonService = jsonService;
    }

    @Override
    protected void process(ServiceCall serviceCall) {
        UsagePointConfigDomainExtension extension = serviceCall.getExtension(UsagePointConfigDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        UsagePoint usagePoint = jsonService.deserialize(extension.getUsagePoint(), UsagePoint.class);
        try {
            switch (Action.valueOf(extension.getOperation())) {
            case CREATE:
                UsagePointBuilder.PreparedUsagePointBuilder builder = usagePointBuilderProvider.get().from(usagePoint,
                        0);
                Instant requestTimestamp = extension.getRequestTimestamp();
                if (requestTimestamp != null) {
                    builder.at(requestTimestamp);
                }
                builder.create();
                break;
            case UPDATE:
                usagePointBuilderProvider.get().from(usagePoint, 0).update();
                break;
            default:
                break;
            }
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } catch (Exception faultMessage) {
            if (faultMessage instanceof FaultMessage) {
                Optional<ErrorType> errorType = ((FaultMessage) faultMessage).getFaultInfo().getReply().getError()
                        .stream().findFirst();
                if (errorType.isPresent()) {
                    extension.setErrorMessage(errorType.get().getDetails());
                    extension.setErrorCode(errorType.get().getCode());
                } else {
                    extension.setErrorMessage(faultMessage.getLocalizedMessage());
                }
            } else if (faultMessage instanceof ConstraintViolationException) {
                extension.setErrorMessage(((ConstraintViolationException) faultMessage).getConstraintViolations()
                        .stream().findFirst().map(ConstraintViolation::getMessage).orElseGet(faultMessage::getMessage));
            } else {
                extension.setErrorMessage(faultMessage.getLocalizedMessage());
            }
            serviceCall.update(extension);
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }
}
