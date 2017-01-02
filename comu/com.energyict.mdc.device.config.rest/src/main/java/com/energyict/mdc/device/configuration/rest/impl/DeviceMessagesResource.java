package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DeviceMessagesResource {

    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public DeviceMessagesResource(ResourceHelper resourceHelper, Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory) {
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getDeviceMessages(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam JsonQueryParameters queryParameters) {

        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);

        List<DeviceMessageId> supportedMessages = deviceType.getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getId)
                        .map(DeviceMessageId::havingId)
                        .collect(Collectors.toList())).orElse(Collections.emptyList());

        if (supportedMessages.isEmpty()) {
            return PagedInfoList.fromPagedList("categories", Collections.emptyList(), queryParameters);
        }

        List<DeviceMessageEnablement> deviceMessageEnablements = deviceConfiguration.getDeviceMessageEnablements();
        List<DeviceMessageCategoryInfo> infos = new ArrayList<>();

        for (DeviceMessageCategory category : deviceMessageSpecificationService.filteredCategoriesForComTaskDefinition()) {
            List<DeviceMessageSpec> messages = category.getMessageSpecifications().stream()
                    .filter(m -> supportedMessages.contains(m.getId()))
                    .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                    .collect(Collectors.toList());
            if (!messages.isEmpty()) {
                infos.add(DeviceMessageCategoryInfo.from(category, messages, deviceMessageEnablements, thesaurus));
            }
        }

        Collections.sort(infos, (c1, c2) -> c1.name.compareTo(c2.name));
        return PagedInfoList.fromPagedList("categories", infos, queryParameters);
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response activateDeviceMessages(@PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam JsonQueryParameters queryParameters,
            DeviceMessageEnablementInfo info) {
        DeviceConfiguration deviceConfiguration = resourceHelper.lockDeviceConfigurationOrThrowException(info.deviceConfiguration);

        Set<DeviceMessageId> existingEnablements = deviceConfiguration.getDeviceMessageEnablements()
                .stream().map(DeviceMessageEnablement::getDeviceMessageId).collect(Collectors.toSet());

        for (Long messageId : info.messageIds) {
            DeviceMessageSpec messageSpec = deviceMessageSpecificationService.findMessageSpecById(messageId).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE_SPEC, messageId));
            if (!existingEnablements.contains(messageSpec.getId())) {
                DeviceMessageEnablementBuilder enablementBuilder = deviceConfiguration.createDeviceMessageEnablement(messageSpec.getId());
                info.privileges.stream().map(p -> p.privilege).forEach(enablementBuilder::addUserAction);
                enablementBuilder.build();
            }
        }

        return Response.ok().build();
    }

    @DELETE @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deactivateDeviceMessages(@PathParam("deviceConfigurationId") long deviceConfigurationId, DeviceMessageEnablementInfo info) {
        DeviceConfiguration deviceConfiguration = resourceHelper.lockDeviceConfigurationOrThrowException(info.deviceConfiguration);

        for (Long messageId: info.messageIds) {
            DeviceMessageSpec messageSpec = deviceMessageSpecificationService.findMessageSpecById(messageId).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE_SPEC, messageId));
            deviceConfiguration.removeDeviceMessageEnablement(messageSpec.getId());
        }

        deviceConfiguration.save();
        return Response.ok().build();
    }

    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response changeDeviceMessagePrivileges(@PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam JsonQueryParameters queryParameters,
            DeviceMessageEnablementInfo enablementInfo) {
        enablementInfo.deviceConfiguration.id = deviceConfigurationId;
        DeviceConfiguration deviceConfiguration = resourceHelper.lockDeviceConfigurationOrThrowException(enablementInfo.deviceConfiguration);
        List<DeviceMessageEnablement> deviceMessageEnablements = deviceConfiguration.getDeviceMessageEnablements();

        for (DeviceMessageEnablement deviceMessageEnablement : deviceMessageEnablements) {
            if (enablementInfo.messageIds.contains(deviceMessageEnablement.getDeviceMessageId().dbValue())) {
                Arrays.asList(DeviceMessageUserAction.values()).stream().forEach(deviceMessageEnablement::removeDeviceMessageUserAction);
                enablementInfo.privileges.stream().map(p -> p.privilege).forEach(deviceMessageEnablement::addDeviceMessageUserAction);
            }
        }
        deviceConfiguration.save();

        return Response.ok().build();
    }
}
