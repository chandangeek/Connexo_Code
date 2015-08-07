package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.appserver.rest.AppServerHelper;
import com.elster.jupiter.datavault.impl.ExceptionFactory;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.scheduling.ScheduleAction;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BulkScheduleResource {
    private static final String LOAD_ALL_DEVICES = "all";

    private final DeviceService deviceService;
    private final ResourceHelper resourceHelper;
    private final SchedulingService schedulingService;
    private final Thesaurus thesaurus;
    private final ExceptionFactory exceptionFactory;
    private final AppServerHelper appServerHelper;
    private final JsonService jsonService;
    private final MessageService messageService;

    @Inject
    public BulkScheduleResource(DeviceService deviceService, ResourceHelper resourceHelper, SchedulingService schedulingService,
                                Thesaurus thesaurus, ExceptionFactory exceptionFactory, AppServerHelper appServerHelper,
                                JsonService jsonService, MessageService messageService) {
        this.deviceService = deviceService;
        this.resourceHelper = resourceHelper;
        this.schedulingService = schedulingService;
        this.thesaurus = thesaurus;
        this.exceptionFactory = exceptionFactory;
        this.appServerHelper = appServerHelper;
        this.jsonService = jsonService;
        this.messageService = messageService;
        DeviceHolder.deviceService = deviceService;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response addComScheduleToDeviceSet(BulkRequestInfo request, @BeanParam JsonQueryFilter queryFilter) {
        return putActionOnQueue(request, queryFilter, ScheduleAction.Add);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response deleteComScheduleFromDeviceSet(BulkRequestInfo request, @BeanParam JsonQueryFilter queryFilter) {
        return putActionOnQueue(request, queryFilter, ScheduleAction.Remove);
    }

    private Response putActionOnQueue(BulkRequestInfo request, @BeanParam JsonQueryFilter queryFilter, ScheduleAction action) {
        if (!appServerHelper.verifyActiveAppServerExists(SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION) || !appServerHelper.verifyActiveAppServerExists(SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        ItemizeComScheduleQueueMessage message = new ItemizeComScheduleQueueMessage();
        message.action = action;
        message.deviceMRIDs = request.deviceMRIDs;
        message.scheduleIds = request.scheduleIds;
        message.comScheduleOnDevicesFilterSpecification = getFilterFromRequest(queryFilter);

        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            return processMessagePost(message, destinationSpec.get());
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }
    }

    private ComScheduleOnDevicesFilterSpecification getFilterFromRequest(JsonQueryFilter queryFilter) {
        ComScheduleOnDevicesFilterSpecification filter = new ComScheduleOnDevicesFilterSpecification();
        filter.mRID = queryFilter.getString("mRID");
        filter.serialNumber = queryFilter.getString("serialNumber");
        filter.deviceTypes = queryFilter.getLongList("deviceTypes");
        filter.deviceConfigurations = queryFilter.getLongList("deviceConfigurations");
        if (filter.mRID!=null && filter.deviceConfigurations!=null || filter.deviceTypes!=null || filter.serialNumber!=null) {
            return filter;
        } else {
            return null;
        }
    }

    private Response processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
        return Response.ok().entity("{\"success\":\"true\"}").build();
    }















    private ComSchedulesBulkInfo processBulkAction(BulkRequestInfo request, BulkAction action, StandardParametersBean queryParameters) {
        ComSchedulesBulkInfo response = new ComSchedulesBulkInfo();
        Map<String, DeviceHolder> deviceMap = getDeviceMapForBulkAction(request, response, queryParameters);
        for (Long scheduleId : request.scheduleIds) {
            Optional<ComSchedule> scheduleRef = schedulingService.findSchedule(scheduleId);
            if (!scheduleRef.isPresent()) {
                String failMessage = MessageSeeds.NO_SUCH_COM_SCHEDULE.format(thesaurus, scheduleId);
                response.nextAction(failMessage).failCount = deviceMap.size();
            } else {
                processScheduleForBulkAction(deviceMap, scheduleRef.get(), action, response);
            }
        }
        return response;
    }

    private void processScheduleForBulkAction(Map<String, DeviceHolder> deviceMap, ComSchedule schedule, BulkAction action, ComSchedulesBulkInfo response) {
        response.nextAction(schedule.getName());
        for (DeviceHolder device : deviceMap.values()) {
            processSchedule(device, schedule, action, response);
        }
    }

    private void processSchedule(DeviceHolder holder, ComSchedule schedule, BulkAction action, ComSchedulesBulkInfo response) {
        Device device = holder.get();
        try {
            action.doAction(device, schedule);
            device.save();
            response.success();
        } catch (LocalizedException localizedEx) {
            response.fail(DeviceInfo.from(device), localizedEx.getLocalizedMessage(), localizedEx.getClass().getSimpleName());
        } catch (ConstraintViolationException validationException) {
            response.fail(DeviceInfo.from(device), getMessageForConstraintViolation(validationException, device, schedule),
                    validationException.getClass().getSimpleName());
            holder.obsolete();
        }
    }

    private Map<String, DeviceHolder> getDeviceMapForBulkAction(BulkRequestInfo request, ComSchedulesBulkInfo response, StandardParametersBean queryParameters) {
        Map<String, DeviceHolder> deviceMap = new HashMap<>();
        String loadAllDevicesAsStr = queryParameters.getFirst(LOAD_ALL_DEVICES);
        if (Boolean.parseBoolean(loadAllDevicesAsStr)) {
            Condition condition = resourceHelper.getQueryConditionForDevice(queryParameters);
            Finder<Device> allDevicesFinder = deviceService.findAllDevices(condition);
            List<Device> allDevices = allDevicesFinder.find();
            for (Device device : allDevices) {
                deviceMap.put(device.getmRID(), new DeviceHolder(device));
            }
        } else {
            for (String mrid : request.deviceMRIDs) {
                try {
                    deviceMap.put(mrid, new DeviceHolder(resourceHelper.findDeviceByMrIdOrThrowException(mrid)));
                } catch (LocalizedException ex) {
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.mRID = mrid;
                    response.generalFail(deviceInfo, ex.getLocalizedMessage(), ex.getClass().getSimpleName());
                }
            }
        }
        return deviceMap;
    }

    private String getMessageForConstraintViolation(ConstraintViolationException ex, Device device, ComSchedule schedule) {
        if (ex.getConstraintViolations() != null && !ex.getConstraintViolations().isEmpty()) {
            return ex.getConstraintViolations().iterator().next().getMessage();
        }
        return MessageSeeds.DEVICE_VALIDATION_BULK_MSG.format(thesaurus, schedule.getName(), device.getName());
    }

    private static interface BulkAction {
        public void doAction(Device device, ComSchedule schedule);
    }

    private static final class DeviceHolder {
        private static DeviceService deviceService;

        private Device device;
        private boolean obsolete;

        private DeviceHolder(Device device) {
            this.device = device;
        }

        public Device get() {
            // We need to reload device because comTaskExecution collection
            // could contain bad schedule from previous iteration
            if (obsolete) {
                reload();
            }
            return device;
        }

        private void reload() {
            device = deviceService.findByUniqueMrid(device.getmRID()).get();
            obsolete = false;
        }

        public void obsolete() {
            obsolete = true;
        }
    }
}
