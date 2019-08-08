package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.appserver.rest.AppServerHelper;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.exceptions.InvalidSearchDomain;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class BulkLoadProfileResource {

    private final ExceptionFactory exceptionFactory;
    private final AppServerHelper appServerHelper;
    private final JsonService jsonService;
    private final MessageService messageService;
    private final SearchService searchService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;

    @Inject
    public BulkLoadProfileResource(ExceptionFactory exceptionFactory,
                                   AppServerHelper appServerHelper,
                                   JsonService jsonService,
                                   MessageService messageService,
                                   SearchService searchService,
                                   DeviceService deviceService,
                                   Thesaurus thesaurus) {
        this.exceptionFactory = exceptionFactory;
        this.appServerHelper = appServerHelper;
        this.jsonService = jsonService;
        this.messageService = messageService;
        this.searchService = searchService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_DATA)
    public Response changeLoadProfileStart(BulkRequestInfo request) {

        if (!appServerHelper.verifyActiveAppServerExists(LoadProfileService.BULK_LOADPROFILE_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        if (!"changeLPStart".equalsIgnoreCase(request.action)) {
            throw exceptionFactory.newException(MessageSeeds.BAD_ACTION);
        }

        LoadProfileOnDevicesFilterSpecification lpFilter = new LoadProfileOnDevicesFilterSpecification();
        Stream<Device> deviceStream;

        if (request.filter != null) {
            JsonQueryFilter filter = new JsonQueryFilter(request.filter);
            Optional<SearchDomain> deviceSearchDomain = searchService.findDomain(Device.class.getName());
            setFilterProperties(filter, deviceSearchDomain, lpFilter);

            SearchBuilder<Object> searchBuilder = getObjectSearchBuilder(lpFilter);
            deviceStream = searchBuilder.toFinder().stream().map(Device.class::cast);
        } else {
            deviceStream = request.deviceIds.stream().map(deviceService::findDeviceById).filter(Optional::isPresent).map(Optional::get);
        }

        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(LoadProfileService.BULK_LOADPROFILE_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            deviceStream.forEach(
                    device -> processMessagePost(new LoadProfileOnDeviceQueueMessage(device.getId(), request.loadProfileName, request.loadProfileLastReading), destinationSpec.get()));
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }

        return Response.ok().entity("{\"success\":\"true\"}").build();
    }

    private void setFilterProperties(@BeanParam JsonQueryFilter filter, Optional<SearchDomain> deviceSearchDomain, LoadProfileOnDevicesFilterSpecification lpFilter) {
        if (filter.hasFilters() && deviceSearchDomain.isPresent()) {
            deviceSearchDomain.get().getPropertiesValues(searchableProperty -> SearchablePropertyValueConverter.convert(searchableProperty, filter))
                    .stream()
                    .forEach(propertyValue -> {
                        lpFilter.properties.put(propertyValue.getProperty().getName(), propertyValue.getValueBean());
                    });
        }
    }

    private Function<SearchableProperty, SearchablePropertyValue> getPropertyMapper(LoadProfileOnDevicesFilterSpecification filter) {
        return searchableProperty -> new SearchablePropertyValue(searchableProperty, filter.properties.get(searchableProperty.getName()));
    }


    private void processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
    }

    private SearchBuilder<Object> getObjectSearchBuilder(LoadProfileOnDevicesFilterSpecification filter) {
        Optional<SearchDomain> searchDomain = searchService.findDomain(Device.class.getName());
        if (searchDomain.isPresent()) {
            SearchBuilder<Object> searchBuilder = searchService.search(searchDomain.get());
            for (SearchablePropertyValue propertyValue : searchDomain.get().getPropertiesValues(getPropertyMapper(filter))) {
                try {
                    propertyValue.addAsCondition(searchBuilder);
                } catch (InvalidValueException e) {
                    throw new RuntimeException(e);
                }
            }
            return searchBuilder;
        } else {
            throw new InvalidSearchDomain(thesaurus, Device.class.getName());
        }
    }
}
