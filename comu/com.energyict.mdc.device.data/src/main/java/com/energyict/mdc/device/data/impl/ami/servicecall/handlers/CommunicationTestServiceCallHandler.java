package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.servicecall.ami.test.communication.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + CommunicationTestServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class CommunicationTestServiceCallHandler extends AbstractOperationServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "TestDeviceCommunicationServiceCallHandler";
    public static final String APPLICATION = "MDC";

    private volatile CompletionOptionsCallBack completionOptionsCallBack;

    public CommunicationTestServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public CommunicationTestServiceCallHandler(CompletionOptionsCallBack completionOptionsCallBack) {
        this.completionOptionsCallBack = completionOptionsCallBack;
    }

    @Reference
    public void setCompletionOptionsCallBack(CompletionOptionsCallBack completionOptionsCallBack) {
        this.completionOptionsCallBack = completionOptionsCallBack;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case SUCCESSFUL:
                completionOptionsCallBack.sendFinishedMessageToDestinationSpec(serviceCall);
                break;
            case FAILED:
            case PARTIAL_SUCCESS:
            case CANCELLED:
                completionOptionsCallBack.sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.ONE_OR_MORE_DEVICE_COMMANDS_FAILED);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }
}