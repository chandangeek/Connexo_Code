/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Component(name = "com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandHandler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=UsagePointCommandHandler")
public class UsagePointCommandHandler implements ServiceCallHandler {

    public static final String USAGE_POINT_COMMAND_HANDLER_NAME = "UsagePointCommandHandler";
    public static final String USAGE_POINT_COMMAND_HANDLER_VERSION = "v1.0";

    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringService meteringService;
    private volatile BpmService bpmService;

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case SUCCESSFUL:
                sendSuccessResponce(serviceCall);
                break;
            case PARTIAL_SUCCESS:
                getPartiallySuccessResponce(serviceCall);
                break;
            case FAILED:
            case CANCELLED:
                getFailureResponce(serviceCall);
                break;
        }
    }

    public void sendSuccessResponce(ServiceCall serviceCall) {
        serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .ifPresent(extension -> sendResponse(extension.getCallbackSuccessURL()));
    }

    public void getPartiallySuccessResponce(ServiceCall serviceCall) {
        serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .ifPresent(extension -> sendResponse(extension.getCallbackPartialSuccessURL()));
    }

    public void getFailureResponce(ServiceCall serviceCall) {
        serviceCall.getExtension(UsagePointCommandDomainExtension.class)
                .ifPresent(extension -> sendResponse(extension.getCallbackFailureURL()));
    }


    public void sendResponse(String targetURL) {
        this.bpmService.getBpmServer().doPost(targetURL, null);

    }
}
