/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageAction;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageHandler;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.ConfigEventInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.parent.AbstractServiceCallHandler;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.Optional;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MasterDataLinkageConfig
 */
public class MasterDataLinkageConfigServiceCallHandler extends AbstractServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MasterDataLinkageConfigServiceCallHandler";
    public static final String VERSION = "v1.0";

    private final Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider;
    private final JsonService jsonService;

    @Inject
    public MasterDataLinkageConfigServiceCallHandler(
            Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider, JsonService jsonService) {
        this.masterDataLinkageHandlerProvider = masterDataLinkageHandlerProvider;
        this.jsonService = jsonService;
    }

    @Override
    protected void process(ServiceCall serviceCall) {
        MasterDataLinkageConfigDomainExtension extension = serviceCall
                .getExtension(MasterDataLinkageConfigDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        ConfigEventInfo configurationEvent = jsonService.deserialize(extension.getConfigurationEvent(),
                ConfigEventInfo.class);
        UsagePointInfo usagePoint = jsonService.deserialize(extension.getUsagePoint(), UsagePointInfo.class);
        MeterInfo meter = jsonService.deserialize(extension.getMeter(), MeterInfo.class);
        try {
            switch (MasterDataLinkageAction.valueOf(extension.getOperation())) {
            case CREATE:
                masterDataLinkageHandlerProvider.get().from(configurationEvent, usagePoint, meter).createLinkage();
                break;
            case CLOSE:
                masterDataLinkageHandlerProvider.get().from(configurationEvent, usagePoint, meter).closeLinkage();
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
