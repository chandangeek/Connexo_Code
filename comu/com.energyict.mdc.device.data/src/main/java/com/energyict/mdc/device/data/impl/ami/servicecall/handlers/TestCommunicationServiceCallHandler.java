package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.tasks.BasicCheckTask;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

@Component(name = "com.energyict.servicecall.ami.test.communication.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class TestCommunicationServiceCallHandler extends AbstractOperationServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "TestCommunicationServiceCallHandler";

    private volatile CompletionOptionsCallBack completionOptionsCallBack;

    public TestCommunicationServiceCallHandler() {
        super();
    }

    // Constructor only to be used in JUnit tests
    public TestCommunicationServiceCallHandler(CompletionOptionsCallBack completionOptionsCallBack) {
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

    @Override
    protected void handleAllDeviceCommandsExecutedSuccessfully(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        triggerCommunicationTask(serviceCall, domainExtension);
        serviceCall.requestTransition(DefaultState.WAITING);
    }

    private void triggerCommunicationTask(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        serviceCall.log(LogLevel.INFO, "Scheduling comtask task to check communication");
        Device device = (Device) serviceCall.getTargetObject().get();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        serviceCall.update(domainExtension);

        ComTaskEnablement comTaskEnablement = getBasicCheckComTaskEnablement(device, serviceCall);
        Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getComTask().getId() == comTaskEnablement.getComTask().getId())
                .findFirst();
        existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement)).scheduleNow();
    }

    private ComTaskExecution createAdHocComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(comTaskExecutionBuilder::connectionTask);
        }
        ComTaskExecution manuallyScheduledComTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    private ComTaskEnablement getBasicCheckComTaskEnablement(Device device, ServiceCall serviceCall) {
        Optional<ComTaskEnablement> enablementOptional = device.getDeviceConfiguration()
                .getComTaskEnablements()
                .stream()
                .filter(cte -> cte.getComTask().getProtocolTasks().stream().
                        filter(task -> task instanceof BasicCheckTask).
                        findFirst().
                        isPresent())
                .findAny();
        if (enablementOptional.isPresent()) {
            return enablementOptional.get();
        } else {
            serviceCall.log(LogLevel.SEVERE, MessageSeeds.NO_STATUS_INFORMATION_COMTASK.getDefaultFormat());
            getCompletionOptionsCallBack().sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.NO_COMTASK_TO_VERIFY_BREAKER_STATUS);
            serviceCall.requestTransition(DefaultState.FAILED);
            throw new IllegalStateException(getThesaurus().getFormat(MessageSeeds.NO_BASIC_CHECK_COMTASK).format());
        }
    }
}