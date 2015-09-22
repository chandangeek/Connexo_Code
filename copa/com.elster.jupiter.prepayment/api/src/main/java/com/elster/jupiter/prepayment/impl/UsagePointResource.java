package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.util.Date;
import java.util.Optional;

/**
 * Created by bvn on 9/16/15.
 */
@Path("usagepoints/{mrid}/contactor")
public class UsagePointResource {

    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final Clock clock;

    @Inject
    public UsagePointResource(MeteringService meteringService, DeviceService deviceService, Clock clock) {
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.clock = clock;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response updateContactor(@PathParam("mrid") String mRID, ContactorInfo contactorInfo) {

        UsagePoint usagePoint = meteringService.findUsagePoint(mRID).orElseThrow(() -> new WebApplicationException("No such usagepoint", Response.Status.NOT_FOUND));
        MeterActivation meterActivation = usagePoint.getCurrentMeterActivation().orElseThrow(() -> new WebApplicationException("No current meter activation", Response.Status.NOT_FOUND));
        Meter meter = meterActivation.getMeter().orElseThrow(() -> new WebApplicationException("No meter in activation", Response.Status.NOT_FOUND));
        Device device = deviceService.findByUniqueMrid(meter.getMRID()).orElseThrow(() -> new WebApplicationException("No such meter", Response.Status.NOT_FOUND));
        DeviceMessageId deviceMessageId = getMessageId(contactorInfo);
        ComTaskEnablement comTaskEnablement = getComTaskEnablementForDeviceMessage(device, deviceMessageId);
        Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getComTasks().stream()
                        .anyMatch(comTask -> comTask.getId() == comTaskEnablement.getComTask().getId()))
                .findFirst();
        createDeviceMessageOnDevice(contactorInfo, device, deviceMessageId);
        existingComTaskExecution.orElseGet(()->createAdHocComTaskExecution(device, comTaskEnablement)).runNow();

        return Response.status(Response.Status.ACCEPTED).build();
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

    private void createDeviceMessageOnDevice(ContactorInfo contactorInfo, Device device, DeviceMessageId deviceMessageId) {
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(deviceMessageId).setReleaseDate(clock.instant());
        if (contactorInfo.activationDate!=null) {
            deviceMessageBuilder.addProperty(DeviceMessageConstants.contactorActivationDateAttributeName,Date.from(contactorInfo.activationDate));
        }
        deviceMessageBuilder.add();
    }

    private ComTaskEnablement getComTaskEnablementForDeviceMessage(Device device, DeviceMessageId deviceMessageId) {
        return device.getDeviceConfiguration().
                    getComTaskEnablements().stream().
                    filter(cte -> cte.getComTask().getProtocolTasks().stream().
                            filter(task -> task instanceof MessagesTask).
                            flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                            flatMap(category -> category.getMessageSpecifications().stream()).
                            filter(dms -> dms.getId().equals(deviceMessageId)).
                            findFirst().
                            isPresent()).
                    findAny().orElseThrow(() -> new WebApplicationException("No comtask for command", Response.Status.BAD_REQUEST));
    }

    private DeviceMessageId getMessageId(ContactorInfo contactorInfo) {
        // TODO load limiting DeviceMessageId.LOAD_BALANCING...
        switch (contactorInfo.status) {
            case connected:
                return contactorInfo.activationDate!=null?DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE:DeviceMessageId.CONTACTOR_CLOSE;
            case disconnected:
                return contactorInfo.activationDate!=null?DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE:DeviceMessageId.CONTACTOR_OPEN;
            case armed:
                return contactorInfo.activationDate!=null?DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE:DeviceMessageId.CONTACTOR_ARM;
            default:
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

}
