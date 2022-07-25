/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.rest.util.BooleanValueInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.exceptions.InvalidSearchDomain;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SAPRegisteredNotificationOnDeviceQueueMessage;
import com.energyict.mdc.sap.soap.webservices.SapBulkActionDeviceFilterSpecification;
import com.energyict.mdc.sap.soap.webservices.SetPushEventsToSapOnDeviceQueueMessage;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/")
public class SapResource {
    private final static String SEND_SAP_REGISTERED_NOTIFICATION_START = "sendSAPRNStart";
    private final static String SET_PUSH_EVENTS_TO_SAP = "setPushEventsToSapFlag";

    private final String BULK_SAPREGISTEREDNOTIFICATION_QUEUE_DESTINATION = "BulkSAPRegNotificationQD";
    private final String BULK_SETPUSHEVENTSTOSAP_QUEUE_DESTINATION = "BulkSAPPushEventsQD";
    private final ExceptionFactory exceptionFactory;
    private final DeviceService deviceService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final EndPointConfigurationService endPointConfigurationService;
    private final RegisteredNotificationEndPointInfoFactory registeredNotificationEndPointInfoFactory;
    private final UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification;
    private final Clock clock;
    private final JsonService jsonService;
    private final MessageService messageService;
    private final SearchService searchService;
    private final Thesaurus thesaurus;

    @Inject
    public SapResource(ExceptionFactory exceptionFactory, DeviceService deviceService, SAPCustomPropertySets sapCustomPropertySets,
                       EndPointConfigurationService endPointConfigurationService, RegisteredNotificationEndPointInfoFactory registeredEndPointConfigurationInfoFactory,
                       UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification, Clock clock,
                       JsonService jsonService, MessageService messageService, SearchService searchService, Thesaurus thesaurus) {
        this.exceptionFactory = exceptionFactory;
        this.deviceService = deviceService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.endPointConfigurationService = endPointConfigurationService;
        this.registeredNotificationEndPointInfoFactory = registeredEndPointConfigurationInfoFactory;
        this.utilitiesDeviceRegisteredNotification = utilitiesDeviceRegisteredNotification;
        this.clock = clock;
        this.jsonService = jsonService;
        this.messageService = messageService;
        this.searchService = searchService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Transactional
    @Path("/devices/{deviceName}/hassapcas")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.SEND_WEB_SERVICE_REQUEST})
    public Response hasSapCas(@PathParam("deviceName") String deviceName) {
        Device device = deviceService.findDeviceByName(deviceName).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));
        boolean hasSapCas = false;

        if (sapCustomPropertySets.doesDeviceHaveSapCPS(device)) {
            hasSapCas = true;
        }

        return Response.ok().entity(new BooleanValueInfo(hasSapCas)).build();
    }

    @GET
    @Transactional
    @Path("/devices/{deviceName}/registers/havesapcas")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,
            com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION,
            com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response isSapCasAssignedToAnyRegister(@PathParam("deviceName") String deviceName) {
        Device device = deviceService.findDeviceByName(deviceName).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));
        boolean haveSapCas = device.getRegisters().stream().anyMatch(sapCustomPropertySets::doesRegisterHaveSapCPS);
        return Response.ok().entity(new BooleanValueInfo(haveSapCas)).build();
    }

    @GET
    @Transactional
    @Path("/devices/{deviceName}/channels/havesapcas")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,
            com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION,
            com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response isSapCasAssignedToAnyChannel(@PathParam("deviceName") String deviceName) {
        Device device = deviceService.findDeviceByName(deviceName).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));
        boolean haveSapCas = device.getChannels().stream().anyMatch(sapCustomPropertySets::doesChannelHaveSapCPS);
        return Response.ok().entity(new BooleanValueInfo(haveSapCas)).build();
    }

    @GET
    @Transactional
    @Path("/registerednotificationendpoints")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.SEND_WEB_SERVICE_REQUEST})
    public Response getAvailableRegisteredNotificationEndpoints(@BeanParam JsonQueryParameters queryParams) {
        List<RegisteredNotificationEndPointInfo> registeredNotificationEndPointInfos = getActiveRegisteredNotificationEndpointConfigurations()
                .map(registeredNotificationEndPointInfoFactory::from)
                .sorted(Comparator.comparing(info -> info.name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        return Response.ok(PagedInfoList.fromCompleteList("registeredNotificationEndpoints", registeredNotificationEndPointInfos, queryParams)).build();
    }

    @POST
    @Transactional
    @Path("/devices/{deviceName}/sendregisterednotification/")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.SEND_WEB_SERVICE_REQUEST})
    public Response sendRegisteredNotification(@PathParam("deviceName") String deviceName, RegisteredNotificationEndPointInfo registeredNotificationEndPointInfo) {

        EndPointConfiguration endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(registeredNotificationEndPointInfo.id)
                .filter(EndPointConfiguration::isActive)
                .filter(e -> e.getWebServiceName().equals(UtilitiesDeviceRegisteredNotification.NAME))
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_REGISTERED_NOTIFICATION_ENDPOINT, registeredNotificationEndPointInfo.id));

        Device device = deviceService.findDeviceByName(deviceName)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));

        String sapDeviceId = sapCustomPropertySets.getSapDeviceId(device)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_ID_ATTRIBUTE_IS_NOT_SET));

        if (sapCustomPropertySets.isRegistered(device)) {
            throw exceptionFactory.newException(MessageSeeds.DEVICE_ALREADY_REGISTERED);
        }

        if (!sapCustomPropertySets.isAnyLrnPresent(device.getId(), clock.instant())) {
            throw exceptionFactory.newException(MessageSeeds.NO_LRN);
        }

        if (!utilitiesDeviceRegisteredNotification.call(sapDeviceId, Collections.singleton(endPointConfiguration))) {
            throw exceptionFactory.newException(MessageSeeds.REQUEST_SENDING_HAS_FAILED);
        }

        return Response.ok().build();
    }

    private Stream<EndPointConfiguration> getActiveRegisteredNotificationEndpointConfigurations() {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(UtilitiesDeviceRegisteredNotification.NAME)
                .stream()
                .filter(EndPointConfiguration::isActive);
    }

    @PUT
    @Transactional
    @Path("/sendregisterednotifications/{endpointId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.SEND_WEB_SERVICE_REQUEST)
    public Response sendRegisteredNotification(BulkRegisteredNotificationEndPointInfo request) {

        if (!SEND_SAP_REGISTERED_NOTIFICATION_START.equalsIgnoreCase(request.action)) {
            throw exceptionFactory.newException(MessageSeeds.BAD_ACTION);
        }

        SapBulkActionDeviceFilterSpecification sapRNFilter = new SapBulkActionDeviceFilterSpecification();
        Stream<Device> deviceStream;

        if (request.filter != null) {
            JsonQueryFilter filter = new JsonQueryFilter(request.filter);
            Optional<SearchDomain> deviceSearchDomain = searchService.findDomain(Device.class.getName());
            if (deviceSearchDomain.isPresent()) {
                setFilterProperties(filter, deviceSearchDomain.get(), sapRNFilter);
                SearchBuilder<Object> searchBuilder = getObjectSearchBuilder(sapRNFilter, deviceSearchDomain.get());
                deviceStream = searchBuilder.toFinder().stream().map(Device.class::cast);
            } else {
                throw new InvalidSearchDomain(thesaurus, Device.class.getName());
            }
        } else {
            deviceStream = deviceService.findAllDevices(where("id").in(request.deviceIds)).stream();
        }

        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(BULK_SAPREGISTEREDNOTIFICATION_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            deviceStream.forEach(
                    device -> processMessagePost(new SAPRegisteredNotificationOnDeviceQueueMessage(device.getId(), request.endPointId), destinationSpec.get()));
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }

        return Response.ok().entity("{\"success\":\"true\"}").build();
    }

    private void setFilterProperties(JsonQueryFilter filter, SearchDomain deviceSearchDomain, SapBulkActionDeviceFilterSpecification sapRNFilter) {
        if (filter.hasFilters()) {
            deviceSearchDomain.getPropertiesValues(searchableProperty -> SearchablePropertyValueConverter.convert(searchableProperty, filter))
                    .stream()
                    .forEach(propertyValue -> {
                        sapRNFilter.properties.put(propertyValue.getProperty().getName(), propertyValue.getValueBean());
                    });
        }
    }

    @PUT
    @Transactional
    @Path("/setpusheventstosap")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.SEND_WEB_SERVICE_REQUEST, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response setPushEventsToSap(BulkSetPushEventsToSapInfo request) {

        if (!SET_PUSH_EVENTS_TO_SAP.equalsIgnoreCase(request.action)) {
            throw exceptionFactory.newException(MessageSeeds.BAD_ACTION);
        }

        SapBulkActionDeviceFilterSpecification setPushEventsToSapFlagFilter = new SapBulkActionDeviceFilterSpecification();
        Stream<Device> deviceStream;

        if (request.filter != null) {
            JsonQueryFilter filter = new JsonQueryFilter(request.filter);
            Optional<SearchDomain> deviceSearchDomain = searchService.findDomain(Device.class.getName());
            if (deviceSearchDomain.isPresent()) {
                setFilterProperties(filter, deviceSearchDomain.get(), setPushEventsToSapFlagFilter);
                SearchBuilder<Object> searchBuilder = getObjectSearchBuilder(setPushEventsToSapFlagFilter, deviceSearchDomain.get());
                deviceStream = searchBuilder.toFinder().stream().map(Device.class::cast);
            } else {
                throw new InvalidSearchDomain(thesaurus, Device.class.getName());
            }
        } else {
            deviceStream = deviceService.findAllDevices(where("id").in(request.deviceIds)).stream();
        }

        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(BULK_SETPUSHEVENTSTOSAP_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            deviceStream.forEach(
                    device -> processMessagePost(new SetPushEventsToSapOnDeviceQueueMessage(device.getId(), request.pushEventsToSap), destinationSpec.get()));
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }

        return Response.ok().entity("{\"success\":\"true\"}").build();
    }

    private Function<SearchableProperty, SearchablePropertyValue> getPropertyMapper(SapBulkActionDeviceFilterSpecification filter) {
        return searchableProperty -> new SearchablePropertyValue(searchableProperty, filter.properties.get(searchableProperty.getName()));
    }

    private void processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
    }

    private SearchBuilder<Object> getObjectSearchBuilder(SapBulkActionDeviceFilterSpecification filter, SearchDomain searchDomain) {
        SearchBuilder<Object> searchBuilder = searchService.search(searchDomain);
        for (SearchablePropertyValue propertyValue : searchDomain.getPropertiesValues(getPropertyMapper(filter))) {
            try {
                propertyValue.addAsCondition(searchBuilder);
            } catch (InvalidValueException e) {
                throw new RuntimeException(e);
            }
        }
        return searchBuilder;
    }
}
