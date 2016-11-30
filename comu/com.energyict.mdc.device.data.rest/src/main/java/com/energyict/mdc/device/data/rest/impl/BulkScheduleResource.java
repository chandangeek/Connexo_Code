package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.appserver.rest.AppServerHelper;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.ComScheduleOnDevicesFilterSpecification;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ItemizeComScheduleQueueMessage;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.ScheduleAction;
import com.energyict.mdc.scheduling.ScheduleAddStrategy;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BulkScheduleResource {

    private final ExceptionFactory exceptionFactory;
    private final AppServerHelper appServerHelper;
    private final JsonService jsonService;
    private final MessageService messageService;
    private final SearchService searchService;
    private final SchedulingService schedulingService;

    @Inject
    public BulkScheduleResource(ExceptionFactory exceptionFactory, AppServerHelper appServerHelper,
                                JsonService jsonService, MessageService messageService, SearchService searchService, SchedulingService schedulingService) {
        this.exceptionFactory = exceptionFactory;
        this.appServerHelper = appServerHelper;
        this.jsonService = jsonService;
        this.messageService = messageService;
        this.searchService = searchService;
        this.schedulingService = schedulingService;
    }

    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response addOrRemoveComScheduleToDeviceSet(BulkRequestInfo request) {
        if (!appServerHelper.verifyActiveAppServerExists(SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION) || !appServerHelper.verifyActiveAppServerExists(SchedulingService.COM_SCHEDULER_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        if (request.action == null || (!request.action.equalsIgnoreCase("add") && !request.action.equalsIgnoreCase("remove"))) {
            throw exceptionFactory.newException(MessageSeeds.BAD_ACTION);
        }
        ItemizeComScheduleQueueMessage message = new ItemizeComScheduleQueueMessage();
        message.action = request.action.equalsIgnoreCase("add") ? ScheduleAction.Add : ScheduleAction.Remove;
        message.deviceIds = request.deviceIds;
        message.scheduleIds = request.scheduleIds;
        List<ComSchedule> comSchedules = message.scheduleIds.stream()
                .map(schedulingService::findSchedule)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if(message.action.equals(ScheduleAction.Add)) {
            checkValidity(comSchedules);
            message.strategy = request.strategy.equalsIgnoreCase("keep") ? ScheduleAddStrategy.KEEP_EXISTING : ScheduleAddStrategy.REMOVE_EXISTING;
        }
        if (request.filter != null) {
            JsonQueryFilter filter = new JsonQueryFilter(request.filter);
            Optional<SearchDomain> deviceSearchDomain = searchService.findDomain(Device.class.getName());
            if (filter.hasFilters() && deviceSearchDomain.isPresent()) {
                message.filter = new ComScheduleOnDevicesFilterSpecification();
                deviceSearchDomain.get().getPropertiesValues(searchableProperty -> SearchablePropertyValueConverter.convert(searchableProperty, filter))
                        .stream()
                        .forEach(propertyValue -> {
                            message.filter.properties.put(propertyValue.getProperty().getName(), propertyValue.getValueBean());
                        });
            }
        }

        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(SchedulingService.FILTER_ITEMIZER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            return processMessagePost(message, destinationSpec.get());
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }
    }

    /**
     * Checks if the comschedules have overlapping tasks
     * @param comSchedules
     */
    private void checkValidity(List<ComSchedule> comSchedules) {
        boolean overlap = comSchedules.stream()
                .anyMatch(schedule1 -> comSchedules.stream()
                        .anyMatch(schedule2 -> !schedule1.equals(schedule2) && hasOverlappingTasks(schedule1, schedule2)));

        if(overlap) {
            throw exceptionFactory.newException(MessageSeeds.OVERLAPPING_COMTASKS_IN_COMSCHEDULES);
        }

    }

    private boolean hasOverlappingTasks(ComSchedule schedule1, ComSchedule schedule2) {
        List<ComTask> overlappingComTasks = new ArrayList<>(schedule1.getComTasks());
        overlappingComTasks.retainAll(schedule2.getComTasks());

        return overlappingComTasks.size() > 0;
    }

    private Response processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
        return Response.ok().entity("{\"success\":\"true\"}").build();
    }
}
