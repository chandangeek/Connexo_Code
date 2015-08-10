package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.appserver.rest.AppServerHelper;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.ComScheduleOnDevicesFilterSpecification;
import com.energyict.mdc.device.data.ItemizeComScheduleQueueMessage;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.scheduling.ScheduleAction;
import com.energyict.mdc.scheduling.SchedulingService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class BulkScheduleResource {

    private final ExceptionFactory exceptionFactory;
    private final AppServerHelper appServerHelper;
    private final JsonService jsonService;
    private final MessageService messageService;

    @Inject
    public BulkScheduleResource(ExceptionFactory exceptionFactory, AppServerHelper appServerHelper,
                                JsonService jsonService, MessageService messageService) {
        this.exceptionFactory = exceptionFactory;
        this.appServerHelper = appServerHelper;
        this.jsonService = jsonService;
        this.messageService = messageService;
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
}
