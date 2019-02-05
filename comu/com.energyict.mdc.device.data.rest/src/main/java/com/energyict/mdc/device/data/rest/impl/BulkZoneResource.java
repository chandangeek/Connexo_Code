package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.appserver.rest.AppServerHelper;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.ZoneAction;
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
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.ZoneOnDeviceQueueMessage;
import com.energyict.mdc.device.data.ZoneOnDevicesFilterSpecification;
import com.energyict.mdc.device.data.exceptions.InvalidSearchDomain;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BulkZoneResource {

    private final ExceptionFactory exceptionFactory;
    private final AppServerHelper appServerHelper;
    private final JsonService jsonService;
    private final MessageService messageService;
    private final SearchService searchService;
    private final MeteringZoneService meteringZoneService;
    private final DeviceService deviceService;
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;

    private final static int LIMIT_DEVICES_ALREADY_LINKED_TO_ZONE_TYPE = 10;

    @Inject
    public BulkZoneResource(ExceptionFactory exceptionFactory, AppServerHelper appServerHelper,
                            JsonService jsonService, MessageService messageService, SearchService searchService, MeteringZoneService meteringZoneService,
                            DeviceService deviceService, MeteringService meteringService, Thesaurus thesaurus) {
        this.exceptionFactory = exceptionFactory;
        this.appServerHelper = appServerHelper;
        this.jsonService = jsonService;
        this.messageService = messageService;
        this.searchService = searchService;
        this.meteringZoneService = meteringZoneService;
        this.deviceService = deviceService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ZONE)
    public Response addOrRemoveZoneToDeviceSet(BulkRequestInfo request) {
        if (!appServerHelper.verifyActiveAppServerExists(MeteringZoneService.BULK_ZONE_QUEUE_DESTINATION)) {
            throw exceptionFactory.newException(MessageSeeds.NO_APPSERVER);
        }
        if (request.action == null || (!request.action.equalsIgnoreCase("addToZone") && !request.action.equalsIgnoreCase("removeFromZone"))) {
            throw exceptionFactory.newException(MessageSeeds.BAD_ACTION);
        }
        ZoneAction zoneAction = request.action.equalsIgnoreCase("addToZone") ? ZoneAction.Add : ZoneAction.Remove;
        ZoneOnDevicesFilterSpecification zoneFilter = new ZoneOnDevicesFilterSpecification();
        Stream<Device> deviceStream;

        if (request.filter != null) {
            JsonQueryFilter filter = new JsonQueryFilter(request.filter);
            Optional<SearchDomain> deviceSearchDomain = searchService.findDomain(Device.class.getName());
            setZoneFilterProperties(filter, deviceSearchDomain, zoneFilter);

            SearchBuilder<Object> searchBuilder = getObjectSearchBuilder(zoneFilter);
            deviceStream = searchBuilder.toFinder().stream().map(Device.class::cast);
        } else {
            deviceStream = request.deviceIds.stream().map(deviceService::findDeviceById).filter(Optional::isPresent).map(Optional::get);
        }

        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(MeteringZoneService.BULK_ZONE_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            deviceStream.forEach(
                    device -> processMessagePost(new ZoneOnDeviceQueueMessage(device.getId(), request.zoneId, request.zoneTypeId, zoneAction), destinationSpec.get()));
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }

        return Response.ok().entity("{\"success\":\"true\"}").build();
    }

    @GET
    @Transactional
    @Path("/byZoneTypeId")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ZONE)
    public Response getDevicesOnZoneType(@Context UriInfo uriInfo, @QueryParam("zoneTypeId") long zoneTypeId, @QueryParam("zoneId") long zoneId, @BeanParam JsonQueryFilter filter) {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();

        Stream<Device> deviceStream;
        if (filter != null && filter.hasFilters()) {
            Optional<SearchDomain> deviceSearchDomain = searchService.findDomain(Device.class.getName());
            ZoneOnDevicesFilterSpecification zoneFilter = new ZoneOnDevicesFilterSpecification();
            setZoneFilterProperties(filter, deviceSearchDomain, zoneFilter);

            SearchBuilder<Object> searchBuilder = getObjectSearchBuilder(zoneFilter);
            deviceStream = searchBuilder.toFinder().stream().map(Device.class::cast);
        } else {
            List<Long> deviceIds = parameters.get("deviceIds").stream().map(deviceId -> Long.parseLong(deviceId))
                    .collect(Collectors.toList());
            deviceStream = deviceIds.stream().map(deviceService::findDeviceById).filter(Optional::isPresent).map(Optional::get);
        }

        List<Device> deviceList = getDevicesLinkedToDifferentZoneFromZoneType(deviceStream.collect(Collectors.toList()), zoneTypeId, zoneId);

        return Response.ok(DeviceZoneInfo.from(deviceList.size(), deviceList.stream()
                .limit(LIMIT_DEVICES_ALREADY_LINKED_TO_ZONE_TYPE)
                .map(device -> device.getName())
                .collect(Collectors.toList()))).build();
    }

    @GET
    @Transactional
    @Path("/byZoneId")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_ZONE)
    public Response getDevicesOnZone(@Context UriInfo uriInfo, @QueryParam("zoneTypeId") long zoneTypeId, @QueryParam("zoneId") long zoneId, @BeanParam JsonQueryFilter filter) {

        Optional<SearchDomain> deviceSearchDomain = searchService.findDomain(Device.class.getName());
        ZoneOnDevicesFilterSpecification zoneFilter = new ZoneOnDevicesFilterSpecification();
        setZoneFilterProperties(filter, deviceSearchDomain, zoneFilter);

        SearchBuilder<Object> searchBuilder = getObjectSearchBuilder(zoneFilter);
        Stream<Device>  deviceStream = searchBuilder.toFinder().stream().map(Device.class::cast);

        return Response.ok(DeviceInfo.fromDevices(deviceStream.collect(Collectors.toList()))).build();

    }

    private void setZoneFilterProperties(@BeanParam JsonQueryFilter filter, Optional<SearchDomain> deviceSearchDomain, ZoneOnDevicesFilterSpecification zoneFilter) {
        if (filter.hasFilters() && deviceSearchDomain.isPresent()) {
            deviceSearchDomain.get().getPropertiesValues(searchableProperty -> SearchablePropertyValueConverter.convert(searchableProperty, filter))
                    .stream()
                    .forEach(propertyValue -> {
                        zoneFilter.properties.put(propertyValue.getProperty().getName(), propertyValue.getValueBean());
                    });
        }
    }

    private Function<SearchableProperty, SearchablePropertyValue> getPropertyMapper(ZoneOnDevicesFilterSpecification filter) {
        return searchableProperty -> new SearchablePropertyValue(searchableProperty, filter.properties.get(searchableProperty.getName()));
    }


    private void processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
    }

    public List<Device> getDevicesLinkedToDifferentZoneFromZoneType(List<Device> devices, long zoneTypeId, long zoneId) {
        List<Device> filteredDevices = new ArrayList<>();
        try {
            devices.forEach(device -> {
                Optional<EndDevice> endDevice = meteringService.findEndDeviceByName(device.getName());
                if (endDevice.isPresent()) {
                    meteringZoneService.getByEndDevice(endDevice.get())
                            .stream()
                            .filter(endDeviceZone -> endDeviceZone.getZone().getZoneType().getId() == zoneTypeId
                                    && endDeviceZone.getZone().getId() != zoneId)
                            .findFirst().ifPresent(addDevice -> filteredDevices.add(device));
                }
            });
        } catch (Exception ex) {
            throw exceptionFactory.newException(MessageSeeds.BAD_REQUEST);
        }
        return filteredDevices;
    }

    private SearchBuilder<Object> getObjectSearchBuilder(ZoneOnDevicesFilterSpecification filter) {
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
