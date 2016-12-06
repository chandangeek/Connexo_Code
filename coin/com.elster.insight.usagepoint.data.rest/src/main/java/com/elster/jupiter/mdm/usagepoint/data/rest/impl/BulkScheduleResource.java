package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.appserver.rest.AppServerHelper;
import com.elster.jupiter.mdm.usagepoint.data.Action;
import com.elster.jupiter.mdm.usagepoint.data.ItemizeAddCalendarMessage;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointFilter;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
import com.elster.jupiter.util.json.JsonService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BulkScheduleResource {

    private final ExceptionFactory exceptionFactory;
    private final JsonService jsonService;
    private final MessageService messageService;
    private final SearchService searchService;
    private final AppServerHelper appServerHelper;
    private final Clock clock;

    @Inject
    public BulkScheduleResource(ExceptionFactory exceptionFactory, JsonService jsonService, MessageService messageService, SearchService searchService, AppServerHelper appServerHelper, Clock clock) {
        this.exceptionFactory = exceptionFactory;
        this.jsonService = jsonService;
        this.messageService = messageService;
        this.searchService = searchService;
        this.appServerHelper = appServerHelper;
        this.clock = clock;
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.calendar.security.Privileges.Constants.MANAGE_TOU_CALENDARS})
    public Response bulkAddCalendarToUsagePoint(BulkRequestInfo request) {
        if (!appServerHelper.verifyActiveAppServerExists(UsagePointDataService.BULK_ITEMIZER_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        ItemizeAddCalendarMessage message = buildMessage(request);

        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(UsagePointDataService.BULK_ITEMIZER_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            return processMessagePost(message, destinationSpec.get());
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }
    }

    ItemizeAddCalendarMessage buildMessage(BulkRequestInfo request) {
        ItemizeAddCalendarMessage message = new ItemizeAddCalendarMessage();
        message.setAction(Arrays.stream(Action.values())
                .filter(action -> action.matches(request.action))
                .findAny()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.BAD_ACTION, request.action)));
        message.setUsagePointMRIDs(request.usagePointMRIDs);
        message.setCalendarIds(request.calendarIds);
        message.setStartTime(request.startTime == null ? clock.millis() : request.startTime);
        if (request.filter != null) {
            JsonQueryFilter filter = new JsonQueryFilter(request.filter);
            Optional<SearchDomain> usagePointSearchDomain = searchService.findDomain(UsagePoint.class.getName());
            if (filter.hasFilters() && usagePointSearchDomain.isPresent()) {
                message.setUsagePointFilter(toUsagePointFilter(filter, usagePointSearchDomain.get()));
            }
        }
        return message;
    }

    UsagePointFilter toUsagePointFilter(JsonQueryFilter filter, SearchDomain deviceSearchDomain) {
        Map<String, SearchablePropertyValue.ValueBean> valueBeanMap = deviceSearchDomain
                .getPropertiesValues(searchableProperty -> SearchablePropertyValueConverter.convert(searchableProperty, filter))
                .stream()
                .collect(Collectors.toMap(
                        propertyValue -> propertyValue.getProperty().getName(),
                        SearchablePropertyValue::getValueBean
                ));
        return new UsagePointFilter(valueBeanMap);
    }

    private Response processMessagePost(ItemizeAddCalendarMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
        return Response.ok().entity("{\"success\":\"true\"}").build();
    }
}
