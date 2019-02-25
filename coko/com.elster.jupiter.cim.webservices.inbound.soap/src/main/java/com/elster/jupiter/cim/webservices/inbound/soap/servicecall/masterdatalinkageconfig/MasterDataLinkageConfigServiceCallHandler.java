/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import javax.inject.Provider;

import java.time.Clock;

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
        // MeterConfigDomainExtension extensionFor = serviceCall.getExtensionFor(new MeterConfigCustomPropertySet()).get();
        // LinkageInfo meterInfo = jsonService.deserialize(extensionFor.getMeter(), LinkageInfo.class);
        // try {
        // MasterDataLinkageConfigResponseMessageType response = null;
        // switch (OperationEnum.getFromString(extensionFor.getOperation())) {
        // case LINK:
        // masterDataLinkageHandlerProvider.get().forLinkageInfo(meterInfo).createLinkage();
        //
        // break;
        // case UNLINK:
        // masterDataLinkageHandlerProvider.get().forLinkageInfo(meterInfo).closeLinkage();
        // break;
        // default:
        // break;
        // }
        // serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        // } catch (Exception faultMessage) {
        // MeterConfigDomainExtension extension = serviceCall.getExtension(MeterConfigDomainExtension.class)
        // .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        // extension.setErrorCode(OperationEnum.getFromString(extension.getOperation()).getDefaultErrorCode());
        // if (faultMessage instanceof FaultMessage) {
        // Optional<ErrorType> errorType = ((FaultMessage) faultMessage).getFaultInfo().getReply().getError()
        // .stream().findFirst();
        // if (errorType.isPresent()) {
        // extension.setErrorMessage(errorType.get().getDetails());
        // extension.setErrorCode(errorType.get().getCode());
        // } else {
        // extension.setErrorMessage(faultMessage.getLocalizedMessage());
        // }
        // } else if (faultMessage instanceof ConstraintViolationException) {
        // extension.setErrorMessage(((ConstraintViolationException) faultMessage).getConstraintViolations()
        // .stream().findFirst().map(ConstraintViolation::getMessage).orElseGet(faultMessage::getMessage));
        // } else {
        // extension.setErrorMessage(faultMessage.getLocalizedMessage());
        // }
        // serviceCall.update(extension);
        // serviceCall.requestTransition(DefaultState.FAILED);
        // }
    }

    private ReplyTypeFactory getReplyTypeFactory() {
        if (replyTypeFactory == null) {
            replyTypeFactory = new ReplyTypeFactory(thesaurus);
        }
        return replyTypeFactory;
    }
}
