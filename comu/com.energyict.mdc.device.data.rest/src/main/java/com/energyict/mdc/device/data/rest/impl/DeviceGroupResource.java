/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedGroup;
import com.elster.jupiter.metering.groups.GroupBuilder;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.BulkDeviceMessageQueueMessage;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.google.common.collect.Range;
import org.json.JSONArray;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Path("/devicegroups")
public class DeviceGroupResource {
    private final MeteringGroupsService meteringGroupsService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final SearchService searchService;
    private final ExceptionFactory exceptionFactory;
    private final DeviceGroupInfoFactory deviceGroupInfoFactory;
    private final ResourceHelper resourceHelper;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final JsonService jsonService;
    private final MessageService messageService;
    private final DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory;
    private final DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory;
    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public DeviceGroupResource(MeteringGroupsService meteringGroupsService, MeteringService meteringService,
                               DeviceService deviceService, SearchService searchService, ExceptionFactory exceptionFactory,
                               DeviceGroupInfoFactory deviceGroupInfoFactory, ResourceHelper resourceHelper,
                               DeviceMessageSpecificationService deviceMessageSpecificationService, DeviceConfigurationService deviceConfigurationService,
                               JsonService jsonService, MessageService messageService,
                               DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory, DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory,
                               PropertyValueInfoService propertyValueInfoService) {
        this.meteringGroupsService = meteringGroupsService;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.searchService = searchService;
        this.exceptionFactory = exceptionFactory;
        this.deviceGroupInfoFactory = deviceGroupInfoFactory;
        this.resourceHelper = resourceHelper;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.jsonService = jsonService;
        this.messageService = messageService;
        this.deviceMessageCategoryInfoFactory = deviceMessageCategoryInfoFactory;
        this.deviceMessageSpecInfoFactory = deviceMessageSpecInfoFactory;
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getDeviceGroups(@QueryParam("type") String typeName, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        Query<EndDeviceGroup> query = getDeviceGroupQueryByType(typeName);
        Condition condition = buildCondition(filter);
        Order order = Order.ascending("upper(name)");
        List<EndDeviceGroup> endDeviceGroups;
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            int from = queryParameters.getStart().get() + 1;
            int to = from + queryParameters.getLimit().get();
            endDeviceGroups = query.select(condition, from, to, order);
        } else {
            endDeviceGroups = query.select(condition, order);
        }
        List<DeviceGroupInfo> deviceGroupInfos = deviceGroupInfoFactory.from(endDeviceGroups);
        return PagedInfoList.fromPagedList("devicegroups", deviceGroupInfos, queryParameters);
    }

    private Query<EndDeviceGroup> getDeviceGroupQueryByType(@QueryParam("type") String typeName) {
        if (QueryEndDeviceGroup.class.getSimpleName().equalsIgnoreCase(typeName)) {
            return meteringGroupsService.getQueryEndDeviceGroupQuery();
        } else {
            return meteringGroupsService.getEndDeviceGroupQuery();
        }
    }

    private Condition buildCondition(JsonQueryFilter filter) {
        Condition condition = Condition.TRUE;
        if (filter.hasProperty("name")) {
            condition = condition.and(where("name").isEqualTo(filter.getString("name")));
        }
        return condition;
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    public DeviceGroupInfo getDeviceGroup(@PathParam("id") long id) {
        return deviceGroupInfoFactory.from(resourceHelper.findEndDeviceGroupOrThrowException(id));
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_GROUP)
    public Response createDeviceGroup(DeviceGroupInfo deviceGroupInfo,
                                      @QueryParam("validate") @DefaultValue("false") boolean onlyValidate) {
        new RestValidationBuilder()
                .notEmpty(deviceGroupInfo.name, "name")
                .notEmpty(deviceGroupInfo.dynamic, "groupType")
                .validate();
        EndDeviceGroup endDeviceGroup;
        if (deviceGroupInfo.dynamic) {
            endDeviceGroup = buildQueryEndDeviceGroup(deviceGroupInfo, onlyValidate);
        } else {
            endDeviceGroup = buildEnumeratedEndDeviceGroup(deviceGroupInfo, onlyValidate);
        }
        return onlyValidate ?
                Response.accepted().build() :
                Response.status(Response.Status.CREATED).entity(deviceGroupInfoFactory.from(endDeviceGroup)).build();
    }

    private EnumeratedEndDeviceGroup buildEnumeratedEndDeviceGroup(DeviceGroupInfo deviceGroupInfo, boolean onlyValidate) {
        GroupBuilder.GroupCreator<? extends EnumeratedEndDeviceGroup> creator = meteringGroupsService
                .createEnumeratedEndDeviceGroup(onlyValidate ?
                        new EndDevice[0] :
                        buildListOfEndDevices(deviceGroupInfo))
                .setName(deviceGroupInfo.name)
                .setLabel("MDC")
                .setMRID("MDC:" + deviceGroupInfo.name);
        return onlyValidate ? creator.validate() : creator.create();
    }

    private QueryEndDeviceGroup buildQueryEndDeviceGroup(DeviceGroupInfo deviceGroupInfo, boolean onlyValidate) {
        SearchDomain deviceSearchDomain = findDeviceSearchDomainOrThrowException();
        GroupBuilder.GroupCreator<? extends QueryEndDeviceGroup> creator = meteringGroupsService
                .createQueryEndDeviceGroup(onlyValidate ?
                        new SearchablePropertyValue[0] :
                        buildSearchablePropertyConditions(deviceGroupInfo))
                .setName(deviceGroupInfo.name)
                .setSearchDomain(deviceSearchDomain)
                .setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider")
                .setLabel("MDC")
                .setMRID("MDC:" + deviceGroupInfo.name);
        return onlyValidate ? creator.validate() : creator.create();
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    @Path("/{id}")
    public Response editDeviceGroup(DeviceGroupInfo info, @PathParam("id") long id) {
        info.id = id;
        EndDeviceGroup endDeviceGroup = resourceHelper.lockEndDeviceGroupOrThrowException(info);
        endDeviceGroup.setName(info.name);
        endDeviceGroup.setMRID("MDC:" + info.name);

        if (info.dynamic) {
            ((QueryEndDeviceGroup) endDeviceGroup).setConditions(Arrays.asList(buildSearchablePropertyConditions(info)));
        } else {
            syncListWithInfo((EnumeratedEndDeviceGroup) endDeviceGroup, info);
        }
        endDeviceGroup.update();
        return Response.ok().entity(deviceGroupInfoFactory.from(endDeviceGroup)).build();
    }

    @DELETE
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_GROUP)
    @Path("/{id}")
    public Response removeDeviceGroup(@PathParam("id") long id, DeviceGroupInfo info) {
        info.id = id;
        EndDeviceGroup endDeviceGroup = resourceHelper.lockEndDeviceGroupOrThrowException(info);
        endDeviceGroup.delete();
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/{id}/devices")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    public PagedInfoList getDevicesOfStaticDeviceGroup(@PathParam("id") long deviceGroupId, @BeanParam JsonQueryParameters queryParameters) {
        EndDeviceGroup endDeviceGroup = resourceHelper.findEndDeviceGroupOrThrowException(deviceGroupId);
        List<Device> devices;
        if (endDeviceGroup.isDynamic()) {
            devices = fetchDevicesOfQueryEndDeviceGroup((QueryEndDeviceGroup) endDeviceGroup, queryParameters);
        } else {
            devices = fetchDevicesOfEnumEndDeviceGroup((EnumeratedEndDeviceGroup) endDeviceGroup, queryParameters);
        }
        return PagedInfoList.fromPagedList("devices", DeviceGroupMemberInfo.from(devices), queryParameters);
    }

    @GET
    @Transactional
    @Path("/{id}/devices/count")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    public Response getDeviceGroupCount(@PathParam("id") long deviceGroupId, @BeanParam JsonQueryParameters queryParameters) {
        EndDeviceGroup endDeviceGroup = resourceHelper.findEndDeviceGroupOrThrowException(deviceGroupId);
        long numberOfSearchResults = endDeviceGroup.getMemberCount(Instant.now());
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("numberOfSearchResults", numberOfSearchResults);
        return Response.ok().entity(jsonResponse).build();
    }

    @GET
    @Transactional
    @Path("/{id}/commands")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    public List<DeviceMessageCategoryInfo> getAvailableCommandsForDeviceGroup(@PathParam("id") long deviceGroupId) {
        EndDeviceGroup endDeviceGroup = resourceHelper.findEndDeviceGroupOrThrowException(deviceGroupId);
        List<DeviceMessageCategory> deviceMessageCategories = deviceMessageSpecificationService.filteredCategoriesForUserSelection();
        List<DeviceMessageSpecWrapper> allExistingDeviceMessageSpecs = deviceMessageCategories.stream()
                .flatMap(cat->cat.getMessageSpecifications().stream())
                .map(DeviceMessageSpecWrapper::new)
                .collect(toList());

        List<DeviceConfiguration> allCommonDeviceConfigurations = deviceConfigurationService.getDeviceConfigsByDeviceGroup(endDeviceGroup);
        List<DeviceMessageCategoryInfo> categoryInfos = new ArrayList<>();
        for (DeviceMessageCategory deviceMessageCategory : deviceMessageCategories) {
            List<DeviceMessageSpecWrapper> commonDeviceMessageSpecs = new ArrayList<>(allExistingDeviceMessageSpecs);
            for (DeviceConfiguration deviceConfiguration : allCommonDeviceConfigurations) {
                List<DeviceMessageSpecWrapper> deviceMessageSpecWrappersInCategory = deviceConfiguration.getEnabledAndAuthorizedDeviceMessageSpecsIn(deviceMessageCategory)
                        .stream()
                        .map(DeviceMessageSpecWrapper::new)
                        .collect(toList());
                commonDeviceMessageSpecs.retainAll(deviceMessageSpecWrappersInCategory);
            }
            if (!commonDeviceMessageSpecs.isEmpty()) {
                DeviceMessageCategoryInfo info = deviceMessageCategoryInfoFactory.asInfo(deviceMessageCategory);
                info.deviceMessageSpecs = commonDeviceMessageSpecs.stream()
                        .map(DeviceMessageSpecWrapper::getDeviceMessageSpec)
                        .map(deviceMessageSpecInfoFactory::asInfoWithMessagePropertySpecs)
                        .collect(toList());
                categoryInfos.add(info);
            }
        }

        return categoryInfos;

    }

    @POST
    @Transactional
    @Path("/{id}/commands")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    public Response createCommandForAllDevicesInDeviceGroup(@PathParam("id") long deviceGroupId, DeviceMessageInfo deviceMessageInfo) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(DeviceMessageService.BULK_DEVICE_MESSAGE_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            DeviceMessageId deviceMessageId = DeviceMessageId.valueOf(deviceMessageInfo.messageSpecification.id);
            EndDeviceGroup endDeviceGroup = resourceHelper.findEndDeviceGroupOrThrowException(deviceGroupId);
            Map<String, String> properties = convertPropertyInfosToMap(deviceMessageInfo.properties);
            BulkDeviceMessageQueueMessage message = new BulkDeviceMessageQueueMessage(endDeviceGroup.getId(), deviceMessageId, deviceMessageInfo.releaseDate.getEpochSecond(), properties);
            return processMessagePost(message, destinationSpec.get());
        } else {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_QUEUE);
        }
    }

    private Map<String, String> convertPropertyInfosToMap(List<PropertyInfo> propertyInfos) {
        Map<String, String> properties = new HashMap<>();
        propertyInfos.stream().forEach(info -> {
            Object value = info.getPropertyValueInfo().getValue();
            properties.put(info.key, value==null?null:jsonService.serialize(value));
        });
        return properties;
    }

    private Map<String, Object> getPropertiesFromInfo(DeviceMessageInfo deviceMessageInfo, DeviceMessageId deviceMessageId) {
        DeviceMessageSpec deviceMessageSpec = deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_SPEC));
        Map<String, Object> properties = new HashMap<>();
        if (deviceMessageInfo.properties != null) {
            try {
                for (PropertySpec propertySpec : deviceMessageSpec.getPropertySpecs()) {
                    Object propertyValue = propertyValueInfoService.findPropertyValue(propertySpec, deviceMessageInfo.properties);
                    if (propertyValue != null) {
                        properties.put(propertySpec.getName(), propertyValue);
                    }
                }
            } catch (LocalizedFieldValidationException e) {
                throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties."+e.getViolatingProperty());
            }
        }
        return properties;
    }

    /**
     * Provides hashcode and equals
     */
    class DeviceMessageSpecWrapper {
        private final DeviceMessageSpec deviceMessageSpec;

        DeviceMessageSpecWrapper(DeviceMessageSpec deviceMessageSpec) {
            this.deviceMessageSpec = deviceMessageSpec;
        }

        public DeviceMessageSpec getDeviceMessageSpec() {
            return deviceMessageSpec;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DeviceMessageSpecWrapper that = (DeviceMessageSpecWrapper) o;
            return Objects.equals(deviceMessageSpec.getCategory().getName(), that.deviceMessageSpec.getCategory().getName())
                    && Objects.equals(deviceMessageSpec.getName(), that.deviceMessageSpec.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(deviceMessageSpec.getName());
        }
    }

    private Response processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
        return Response.accepted().entity("{\"success\":\"true\"}").build();
    }

    private List<Device> fetchDevicesOfEnumEndDeviceGroup(EnumeratedEndDeviceGroup endDeviceGroup, JsonQueryParameters queryParameters) {
        List<Device> devices = new ArrayList<>();
        List<? extends EndDevice> endDevices;
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            endDevices = endDeviceGroup.getMembers(Instant.now(), queryParameters.getStart().get(), queryParameters.getLimit().get());
        } else {
            endDevices = endDeviceGroup.getMembers(Instant.now());
        }
        if (!endDevices.isEmpty()) {
            Condition mdcMembers = where("id").in(endDevices.stream().map(EndDevice::getAmrId).collect(toList()));
            devices = deviceService.findAllDevices(mdcMembers).sorted("name", true).stream().collect(toList());
        }
        return devices;
    }

    private List<Device> fetchDevicesOfQueryEndDeviceGroup(QueryEndDeviceGroup endDeviceGroup, JsonQueryParameters queryParameters) {
        Finder<?> finder = findDeviceSearchDomainOrThrowException().finderFor(endDeviceGroup.getSearchablePropertyConditions());
        return finder.from(queryParameters).find().stream().map(Device.class::cast).collect(Collectors.toList());
    }

    private SearchablePropertyValue[] buildSearchablePropertyConditions(DeviceGroupInfo deviceGroupInfo) {
        SearchDomain searchDomain = findDeviceSearchDomainOrThrowException();
        JsonQueryFilter filter = new JsonQueryFilter(deviceGroupInfo.filter);
        if (!filter.hasFilters()) {
            throw exceptionFactory.newException(MessageSeeds.AT_LEAST_ONE_SEARCH_CRITERIA);
        }
        return searchDomain.getPropertiesValues(property -> SearchablePropertyValueConverter.convert(property, filter))
                .stream().toArray(SearchablePropertyValue[]::new);
    }

    private EndDevice[] buildListOfEndDevices(DeviceGroupInfo deviceGroupInfo) {
        Stream<Long> deviceIds;
        if (deviceGroupInfo.devices == null) {//static device group with ALL option
            SearchBuilder<Device> searchBuilder = searchService.search(Device.class);
            Stream.of(buildSearchablePropertyConditions(deviceGroupInfo)).forEach(searchablePropertyValue -> {
                try {
                    searchablePropertyValue.addAsCondition(searchBuilder);
                } catch (InvalidValueException e) {
                    throw new LocalizedFieldValidationException(MessageSeeds.SEARCHABLE_PROPERTY_INVALID_VALUE, "filter." + searchablePropertyValue.getValueBean().getPropertyName());
                }
            });
            deviceIds = searchBuilder.toFinder().stream().map(HasId::getId);
        } else {
            deviceIds = deviceGroupInfo.devices.stream();
        }
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return deviceIds.map(number -> amrSystem.findMeter(number.toString()))
                .flatMap(asStream())
                .toArray(EndDevice[]::new);
    }

    private void syncListWithInfo(EnumeratedEndDeviceGroup enumeratedEndDeviceGroup, DeviceGroupInfo deviceGroupInfo) {
        EndDevice[] endDevices = buildListOfEndDevices(deviceGroupInfo);
        Map<Long, EnumeratedGroup.Entry<EndDevice>> currentEntries = enumeratedEndDeviceGroup.getEntries().stream().collect(indexedById());
        // remove those no longer mapped
        currentEntries.entrySet().stream()
                .filter(entry -> Stream.of(endDevices).mapToLong(EndDevice::getId).noneMatch(id -> id == entry.getKey()))
                .forEach(entry -> enumeratedEndDeviceGroup.remove(entry.getValue()));
        // add new ones
        Stream.of(endDevices)
                .filter(device -> !currentEntries.containsKey(device.getId()))
                .forEach(device -> enumeratedEndDeviceGroup.add(device, Range.atLeast(Instant.EPOCH)));
    }

    private Collector<EnumeratedGroup.Entry<EndDevice>, ?, Map<Long, EnumeratedGroup.Entry<EndDevice>>> indexedById() {
        return Collectors.toMap(entry -> entry.getMember().getId(), Function.identity());
    }

    private SearchDomain findDeviceSearchDomainOrThrowException() {
        return searchService.findDomain(Device.class.getName()).
                orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_SEARCH_DOMAIN_NOT_REGISTERED));
    }
}
