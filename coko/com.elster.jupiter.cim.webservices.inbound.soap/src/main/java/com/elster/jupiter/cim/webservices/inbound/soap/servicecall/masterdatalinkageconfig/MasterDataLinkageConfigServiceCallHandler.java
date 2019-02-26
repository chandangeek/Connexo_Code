/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.OperationEnum;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent;
import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.schema.message.ErrorType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.time.Clock;
import java.util.Optional;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MeterConfig
 */
public class MasterDataLinkageConfigServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MasterDataLinkageConfigServiceCallHandler";
    public static final String VERSION = "v1.0";

    private final Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider;
    private final Clock clock;
    private final JsonService jsonService;
    private final Thesaurus thesaurus;

    private ReplyTypeFactory replyTypeFactory;

    @Inject
    public MasterDataLinkageConfigServiceCallHandler(
            Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider, Clock clock, JsonService jsonService,
            Thesaurus thesaurus) {
        this.masterDataLinkageHandlerProvider = masterDataLinkageHandlerProvider;
        this.clock = clock;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
        case PENDING:
            serviceCall.requestTransition(DefaultState.ONGOING);
            break;
        case ONGOING:
            processLinkageConfigServiceCall(serviceCall);
            break;
        case SUCCESSFUL:
        case FAILED:
        default:
            break;
        }
    }

    private void processLinkageConfigServiceCall(ServiceCall serviceCall) {
        MasterDataLinkageConfigDomainExtension extension = serviceCall
                .getExtension(MasterDataLinkageConfigDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        ConfigurationEvent configurationEvent = jsonService.deserialize(extension.getConfigurationEvent(),
                ConfigurationEvent.class);
        UsagePoint usagePoint = jsonService.deserialize(extension.getUsagePoint(), UsagePoint.class);
        Meter meter = jsonService.deserialize(extension.getMeter(), Meter.class);
        try {
            switch (OperationEnum.getFromString(extension.getOperation())) {
            case LINK:
                masterDataLinkageHandlerProvider.get().from(configurationEvent, usagePoint, meter).createLinkage();

                break;
            case UNLINK:
                masterDataLinkageHandlerProvider.get().from(configurationEvent, usagePoint, meter).closeLinkage();
                break;
            default:
                break;
            }
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } catch (Exception faultMessage) {
            extension.setErrorCode(OperationEnum.getFromString(extension.getOperation()).getDefaultErrorCode());
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

    private ReplyTypeFactory getReplyTypeFactory() {
        if (replyTypeFactory == null) {
            replyTypeFactory = new ReplyTypeFactory(thesaurus);
        }
        return replyTypeFactory;
    }
}
