package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceCommandResource {
    private final ResourceHelper resourceHelper;
    private final DeviceMessageInfoFactory deviceMessageInfoFactory;
    private final DeviceMessageService deviceMessageService;
    private final DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory;

    @Inject
    public DeviceCommandResource(ResourceHelper resourceHelper, DeviceMessageInfoFactory deviceMessageInfoFactory, DeviceMessageService deviceMessageService, DeviceMessageCategoryInfoFactory deviceMessageCategoryInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceMessageInfoFactory = deviceMessageInfoFactory;
        this.deviceMessageService = deviceMessageService;
        this.deviceMessageCategoryInfoFactory = deviceMessageCategoryInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceCommands(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<DeviceMessageInfo> infos = device.getMessages().stream().
                sorted((c1, c2) -> c2.getReleaseDate().compareTo(c1.getReleaseDate())).
                map(deviceMessageInfoFactory::asInfo).
                collect(toList());

        return PagedInfoList.asJson("commands", infos, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/categories")
    public List<DeviceMessageCategoryInfo> getAllAvailableDeviceCategories(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);

        Set<DeviceMessageId> supportedMessagesSpecs = device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();
        if (supportedMessagesSpecs.isEmpty()) {
            return Collections.emptyList();
        }

        List<DeviceMessageEnablement> deviceMessageEnablements = device.getDeviceConfiguration().getDeviceMessageEnablements();
        List<DeviceMessageCategoryInfo> infos = new ArrayList<>();

        for (DeviceMessageCategory category : deviceMessageService.allCategories()) {
            List<DeviceMessageSpec> deviceMessageSpecs = category.getMessageSpecifications().stream()
                    .filter(deviceMessageSpec -> supportedMessagesSpecs.contains(deviceMessageSpec.getId())) // limit to device message specs supported by the protocol support
                    .filter(dms -> deviceMessageEnablements.stream().map(DeviceMessageEnablement::getDeviceMessageId).anyMatch(id->id==dms.getId())) // limit to device message specs enabled on the config
                    // TODO add user filtering
                    .sorted((dms1, dms2) -> dms1.getName().compareToIgnoreCase(dms2.getName()))
                    .collect(Collectors.toList());
            if (!deviceMessageSpecs.isEmpty()) {
                infos.add(deviceMessageCategoryInfoFactory.asInfo(category, deviceMessageSpecs));
            }
        }
        return infos;
    }

}
