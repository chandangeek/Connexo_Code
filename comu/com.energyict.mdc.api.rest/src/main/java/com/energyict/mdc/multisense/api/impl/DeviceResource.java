package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.ResourceHelper;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;
import static java.util.stream.Collectors.toList;

/**
 * @title A device represents a physical element in the grid. This can be e.g. a meter, gateway, controller, in home display, concentrator, etc.
 * @servicetag Device
 * @author bvn
 */
@Path("/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final DeviceInfoFactory deviceInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ExceptionFactory exceptionFactory;
    private final TopologyService topologyService;
    private final ResourceHelper resourceHelper;
    private final Clock clock;


    @Inject
    public DeviceResource(DeviceService deviceService, DeviceInfoFactory deviceInfoFactory, DeviceConfigurationService deviceConfigurationService,
                          ExceptionFactory exceptionFactory, TopologyService topologyService, ResourceHelper resourceHelper, Clock clock) {
        this.deviceService = deviceService;
        this.deviceInfoFactory = deviceInfoFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.exceptionFactory = exceptionFactory;
        this.topologyService = topologyService;
        this.resourceHelper = resourceHelper;
        this.clock = clock;
    }

    /**
     * View the contents of a uniquely identified device. A device represents a physical element in the grid.
     * This can be e.g. a meter, gateway, controller, in home display, concentrator, etc.
     *
     * @summary View device identified by mRID
     *
     * @param mRID The device's mRID
     * @param uriInfo uriInfo
     * @param fields field selection
     * @return Device information and links to related resources
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{mrid}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceInfo getDevice(@PathParam("mrid") String mRID, @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        return deviceService.findDeviceByMrid(mRID)
                .map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fields.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
    }

    /**
     * View all devices
     *
     * @summary View all devices
     *
     * @param queryParameters Paging parameters 'start' and 'limit'
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Device information and links to related resources
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceInfo> getDevices(@BeanParam JsonQueryParameters queryParameters, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        List<DeviceInfo> infos = deviceService.findAllDevices(Condition.TRUE).from(queryParameters).stream()
                .map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fieldSelection.getFields())).collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * Adds a new device to the system
     *
     * @summary Create a new device
     *
     * @param info Payload describing the values for the to-be-created device
     * @param uriInfo uriInfo
     *
     * @return location href to newly created device
     * @responseheader location href to newly created device
     */
    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response createDevice(DeviceInfo info, @Context UriInfo uriInfo) {
        Optional<DeviceConfiguration> deviceConfiguration = Optional.empty();
        if (info.deviceConfiguration != null && info.deviceConfiguration.id != null) {
            deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfiguration.id);
        }

        Device newDevice;
        if (!is(info.batch).emptyOrOnlyWhiteSpace()) {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.name, info.batch, Instant.now(clock));
        } else {
            newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.name, Instant.now(clock));
        }
        newDevice.setSerialNumber(info.serialNumber);
        newDevice.setYearOfCertification(info.yearOfCertification);
        newDevice.save();

        URI uri = uriInfo.getBaseUriBuilder().
                path(DeviceResource.class).
                path(DeviceResource.class, "getDevice").
                build(newDevice.getmRID());

        return Response.created(uri).build();
    }

    /**
     * Update an existing device
     *
     * @summary update a device with the provided values
     *
     * @param mrid The device's mRID
     * @param info JSON description of new device field values
     * @param uriInfo uriInfo
     * @return Device with updated fields or an error if something went wrong
     */
    @PUT @Transactional
    @Path("/{mrid}")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceInfo updateDevice(@PathParam("mrid") String mrid, DeviceInfo info, @Context UriInfo uriInfo) {
        if (info.version==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "version");
        }
        Device device = deviceService.findAndLockDeviceBymRIDAndVersion(mrid, info.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.CONFLICT_ON_DEVICE));
        if (info.masterDevice != null && info.masterDevice.mRID != null) {
            if (device.getDeviceConfiguration().isDirectlyAddressable()) {
                throw exceptionFactory.newException(MessageSeeds.IMPOSSIBLE_TO_SET_MASTER_DEVICE, device.getmRID());
            }
            Optional<Device> currentGateway = topologyService.getPhysicalGateway(device);
            if (!currentGateway.isPresent() || !currentGateway.get().getmRID().equals(info.masterDevice.mRID)) {
                Device newGateway = deviceService.findDeviceByMrid(info.masterDevice.mRID)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_GATEWAY));
                topologyService.setPhysicalGateway(device, newGateway);
            }
        } else {
            if (topologyService.getPhysicalGateway(device).isPresent()) {
                topologyService.clearPhysicalGateway(device);
            }
        }
        device.setName(info.name);
        device.setSerialNumber(info.serialNumber);
        device.setYearOfCertification(info.yearOfCertification);
        device.save();
        return deviceInfoFactory.asHypermedia(device, uriInfo, Collections.emptyList());
    }


    /**
     * Delete a device identified by mRID
     *
     * @summary Delete a device
     *
     * @param mrid The device's unique mRID identifier
     *
     * @return No content
     */
    @DELETE @Transactional
    @Path("/{mrid}")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response deleteDevice(@PathParam("mrid") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        device.delete();
        return Response.noContent().build();
    }

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @summary List the fields available on this type of entity
     *
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return deviceInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}

