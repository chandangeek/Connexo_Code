package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.tasks.StatusInformationTask;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * Abstract implementation of {@link ServiceCallHandler} interface which handles the different steps for
 * the connect/dicsonnect/arm of the devices breaker.
 *
 * @author sva
 * @since 06/06/16 - 13:05
 */
public abstract class AbstractContactorOperationServiceCallHandler extends AbstractOperationServiceCallHandler {

    private volatile DeviceService deviceService;

    public AbstractContactorOperationServiceCallHandler() {
    }

    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                if (oldState != DefaultState.PENDING) {
                    CommandServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CommandCustomPropertySet()).get();
                    if (CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES.equals(domainExtension.getCommandOperationStatus())) {
                        if (domainExtension.getNrOfUnconfirmedDeviceCommands() == 0) {
                            serviceCall.log(LogLevel.INFO, "All device commands have been executed successfully.");
                            triggerStatusInformationTask(serviceCall, domainExtension);
                            serviceCall.requestTransition(DefaultState.WAITING);
                        } else {
                            // Still awaiting confirmation on other messages
                            serviceCall.requestTransition(DefaultState.WAITING);
                        }
                    } else if (domainExtension.getCommandOperationStatus().equals(CommandOperationStatus.READ_STATUS_INFORMATION)) {
                        verifyBreakerStatus(serviceCall);
                    }
                }
                break;
            case SUCCESSFUL:
                sendFinishedMessageToDestinationSpec(serviceCall);
            default:
                // No specific action required for these states
                break;
        }
    }

    private void triggerStatusInformationTask(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        serviceCall.log(LogLevel.INFO, "Scheduling 'Status information' task in order to verify the breaker status");
        Device device = (Device) serviceCall.getTargetObject().get();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        serviceCall.update(domainExtension);

        ComTaskEnablement comTaskEnablement = getStatusInformationComTaskEnablement(device);
        Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getComTasks().stream().anyMatch(comTask -> comTask.getId() == comTaskEnablement.getComTask().getId()))
                .findFirst();
        existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement)).scheduleNow();
    }

    private ManuallyScheduledComTaskExecution createAdHocComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(comTaskExecutionBuilder::connectionTask);
        }
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    private ComTaskEnablement getStatusInformationComTaskEnablement(Device device) {
        return device.getDeviceConfiguration()
                .getComTaskEnablements()
                .stream()
                .filter(cte -> cte.getComTask().getProtocolTasks().stream().
                        filter(task -> task instanceof StatusInformationTask).
                        findFirst().
                        isPresent())
                .findAny()
                .orElseThrow(() -> new IllegalStateException(getThesaurus().getFormat(MessageSeeds.NO_COMTASK_FOR_COMMAND).format()));
    }

    private void verifyBreakerStatus(ServiceCall serviceCall) {
        Device device = (Device) serviceCall.getTargetObject().get();
        ActivatedBreakerStatus breakerStatus = deviceService.getActiveBreakerStatus(device)
                .orElseThrow(() -> new IllegalStateException(getThesaurus().getFormat(MessageSeeds.NO_ACTIVE_BREAKER_STATUS).format(device.getmRID())));

        if (breakerStatus.getBreakerStatus().equals(getDesiredBreakerStatus())) {
            serviceCall.log(LogLevel.INFO, MessageFormat.format("Confirmed device breaker status: {0}", breakerStatus.getBreakerStatus()));
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        } else {
            serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Device breaker status {0} doesn''t match expected status {1}", breakerStatus.getBreakerStatus(), getDesiredBreakerStatus()));
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }

    public abstract BreakerStatus getDesiredBreakerStatus();
}