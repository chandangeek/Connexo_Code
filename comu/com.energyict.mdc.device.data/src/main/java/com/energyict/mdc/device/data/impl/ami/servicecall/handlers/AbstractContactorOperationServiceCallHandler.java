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
    protected void handleAllDeviceCommandsExecutedSuccessfully(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        triggerStatusInformationTask(serviceCall, domainExtension);
        serviceCall.requestTransition(DefaultState.WAITING);
    }

    private void triggerStatusInformationTask(ServiceCall serviceCall, CommandServiceCallDomainExtension domainExtension) {
        serviceCall.log(LogLevel.INFO, "Scheduling 'Status information' task in order to verify the breaker status");
        Device device = (Device) serviceCall.getTargetObject().get();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        serviceCall.update(domainExtension);

        ComTaskEnablement comTaskEnablement = getStatusInformationComTaskEnablement(device, serviceCall);
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

    private ComTaskEnablement getStatusInformationComTaskEnablement(Device device, ServiceCall serviceCall) {
        Optional<ComTaskEnablement> enablementOptional = device.getDeviceConfiguration()
                .getComTaskEnablements()
                .stream()
                .filter(cte -> cte.getComTask().getProtocolTasks().stream().
                        filter(task -> task instanceof StatusInformationTask).
                        findFirst().
                        isPresent())
                .findAny();
        if (enablementOptional.isPresent()) {
            return enablementOptional.get();
        } else {
            serviceCall.log(LogLevel.SEVERE, MessageSeeds.NO_STATUS_INFORMATION_COMTASK.getDefaultFormat());
            serviceCall.requestTransition(DefaultState.FAILED);
            throw new IllegalStateException(getThesaurus().getFormat(MessageSeeds.NO_STATUS_INFORMATION_COMTASK).format());
        }
    }

    @Override
    protected void verifyDeviceStatus(ServiceCall serviceCall) {
        Device device = (Device) serviceCall.getTargetObject().get();
        Optional<ActivatedBreakerStatus> breakerStatus = deviceService.getActiveBreakerStatus(device);
        if (breakerStatus.isPresent()) {
            if (breakerStatus.get().getBreakerStatus().equals(getDesiredBreakerStatus())) {
                serviceCall.log(LogLevel.INFO, MessageFormat.format("Confirmed device breaker status: {0}", breakerStatus.get().getBreakerStatus()));
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                serviceCall.log(LogLevel.SEVERE, MessageFormat.format("Device breaker status {0} doesn''t match expected status {1}",
                        breakerStatus.get().getBreakerStatus(),
                        getDesiredBreakerStatus())
                );
                serviceCall.requestTransition(DefaultState.FAILED);
            }
        } else {
            //Else in case no active breaker status is present, put the ServiceCall again in waiting state
            serviceCall.requestTransition(DefaultState.WAITING);
        }
    }

    protected abstract BreakerStatus getDesiredBreakerStatus();
}