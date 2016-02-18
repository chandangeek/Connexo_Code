package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.MessagesTask;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn on 9/16/15.
 */
@Path("usagepoints/{mrid}")
public class UsagePointResource {

    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final Clock clock;
    private final ExceptionFactory exceptionFactory;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    public UsagePointResource(MeteringService meteringService, DeviceService deviceService, Clock clock, ExceptionFactory exceptionFactory, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.clock = clock;
        this.exceptionFactory = exceptionFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/contactor")
    @Transactional
    public Response updateContactor(@PathParam("mrid") String mRID, ContactorInfo contactorInfo, @Context UriInfo uriInfo) {
        Device device = findDeviceThroughUsagePoint(mRID);
        List<DeviceMessageId> deviceMessageIds = getMessageIdsOfAllRequiredMessages(contactorInfo);
        List<DeviceMessage<Device>> deviceMessages = createDeviceMessagesOnDevice(contactorInfo, device, deviceMessageIds);
        getComTaskEnablementsForDeviceMessages(device, deviceMessageIds).forEach(comTaskEnablement -> {
            Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                    .filter(cte -> cte.getComTasks().stream()
                            .anyMatch(comTask -> comTask.getId() == comTaskEnablement.getComTask().getId()))
                    .findFirst();
            existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement)).runNow();

        });
        URI uri = uriInfo.getBaseUriBuilder().
                path(UsagePointResource.class).
                path(UsagePointResource.class, "getDeviceMessage").
                build(mRID, deviceMessages.get(deviceMessages.size() - 1)
                        .getId()); //TODO: now temporary using ID of last message, should be reworked with 'Service calls'

        return Response.accepted().location(uri).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/messages/{messageId}")
    @Transactional
    public Response getDeviceMessage(@PathParam("mrid") String mRID, @PathParam("messageId") long id) {
        Device device = findDeviceThroughUsagePoint(mRID);
        DeviceMessage<Device> deviceMessage = device.getMessages().stream().filter(msg -> msg.getId() == id).findFirst().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE));
        DeviceMessageInfo info = new DeviceMessageInfo();
        info.status = deviceMessage.getStatus();
        info.sentDate = deviceMessage.getSentDate().orElse(null);
        return Response.ok(info).build();
    }

    private Device findDeviceThroughUsagePoint(String mRID) {
        UsagePoint usagePoint = meteringService.findUsagePoint(mRID).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_USAGE_POINT));
        MeterActivation meterActivation = usagePoint.getCurrentMeterActivation().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_CURRENT_METER_ACTIVATION));
        Meter meter = meterActivation.getMeter().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METER_IN_ACTIVATION));
        return deviceService.findByUniqueMrid(meter.getMRID()).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_DEVICE_FOR_METER, meter.getMRID()));
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

    private List<DeviceMessage<Device>> createDeviceMessagesOnDevice(ContactorInfo contactorInfo, Device device, List<DeviceMessageId> deviceMessageIds) {
        List<DeviceMessage<Device>> deviceMessages = new ArrayList<>();
        for (DeviceMessageId deviceMessageId : deviceMessageIds) {
            Optional<DeviceMessageSpec> deviceMessageSpec = this.deviceMessageSpecificationService.findMessageSpecById(deviceMessageId
                    .dbValue());
            Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(deviceMessageId)
                    .setReleaseDate(clock.instant());

            if (contactorInfo.activationDate != null && deviceMessageSpecHasPropertySpec(deviceMessageSpec, DeviceMessageConstants.contactorActivationDateAttributeName)) {
                deviceMessageBuilder.addProperty(DeviceMessageConstants.contactorActivationDateAttributeName, Date.from(contactorInfo.activationDate));
            }
            if (contactorInfo.loadLimit != null && deviceMessageSpecHasPropertySpec(deviceMessageSpec, DeviceMessageConstants.normalThresholdAttributeName)) {
                deviceMessageBuilder.addProperty(DeviceMessageConstants.normalThresholdAttributeName, contactorInfo.loadLimit);
            }
            if (contactorInfo.loadTolerance != null && deviceMessageSpecHasPropertySpec(deviceMessageSpec, DeviceMessageConstants.overThresholdDurationAttributeName)) {
                deviceMessageBuilder.addProperty(DeviceMessageConstants.overThresholdDurationAttributeName, new TimeDuration(contactorInfo.loadTolerance));
            }
            if (contactorInfo.tariffs != null && deviceMessageSpecHasPropertySpec(deviceMessageSpec, DeviceMessageConstants.tariffsAttributeName)) {
                String tariffs = Arrays.asList(contactorInfo.tariffs)
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                deviceMessageBuilder.addProperty(DeviceMessageConstants.tariffsAttributeName, tariffs);
            }
            if (contactorInfo.readingType != null && deviceMessageSpecHasPropertySpec(deviceMessageSpec, DeviceMessageConstants.readingTypeAttributeName)) {
                deviceMessageBuilder.addProperty(DeviceMessageConstants.readingTypeAttributeName, contactorInfo.readingType);
            }

            deviceMessages.add(deviceMessageBuilder.add());
        }

        return deviceMessages;
    }

    private boolean deviceMessageSpecHasPropertySpec(Optional<DeviceMessageSpec> deviceMessageSpec, String propertyName) {
        return deviceMessageSpec.isPresent() && deviceMessageSpec.get().getPropertySpec(propertyName).isPresent();
    }

    private Stream<ComTaskEnablement> getComTaskEnablementsForDeviceMessages(Device device, List<DeviceMessageId> deviceMessageIds) {
        List<ComTaskEnablement> comTaskEnablements = new ArrayList<>();
        deviceMessageIds.stream()
                .forEach(deviceMessageId -> comTaskEnablements.add(device.getDeviceConfiguration()
                        .getComTaskEnablements()
                        .stream()
                        .
                                filter(cte -> cte.getComTask().getProtocolTasks().stream().
                                        filter(task -> task instanceof MessagesTask).
                                        flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                                        flatMap(category -> category.getMessageSpecifications().stream()).
                                        filter(dms -> dms.getId().equals(deviceMessageId)).
                                        findFirst().
                                        isPresent())
                        .
                                findAny()
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_COMTASK_FOR_COMMAND))));

        return comTaskEnablements.stream().distinct();
    }

    private List<DeviceMessageId> getMessageIdsOfAllRequiredMessages(ContactorInfo contactorInfo) {
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        if (contactorInfo.status != null) {
            switch (contactorInfo.status) {
                case connected:
                    deviceMessageIds.add(contactorInfo.activationDate != null ? DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_CLOSE);
                    break;
                case disconnected:
                    deviceMessageIds.add(contactorInfo.activationDate != null ? DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_OPEN);
                    break;
                case armed:
                    deviceMessageIds.add(contactorInfo.activationDate != null ? DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_OPEN);
                    deviceMessageIds.add(contactorInfo.activationDate != null ? DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_ARM);
                    break;
                default:
                    throw exceptionFactory.newException(MessageSeeds.UNKNOWN_STATUS);
            }
        }

        if (contactorInfo.loadLimit != null && contactorInfo.loadTolerance != null) {
            deviceMessageIds.add(contactorInfo.tariffs != null ? DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION_WITH_TARIFFS : DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION);
        } else if (contactorInfo.loadTolerance != null) {
            deviceMessageIds.add(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_DURATION);
        } else if (contactorInfo.loadLimit != null) {
            deviceMessageIds.add(contactorInfo.tariffs != null ? DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD_WITH_TARIFFS : DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD);
        }

        if (contactorInfo.readingType != null) {
            deviceMessageIds.add(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_MEASUREMENT_READING_TYPE);
        }
        return deviceMessageIds;
    }
}