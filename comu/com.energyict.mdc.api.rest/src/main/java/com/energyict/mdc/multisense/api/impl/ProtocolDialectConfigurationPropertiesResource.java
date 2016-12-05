package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;


@Path("/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/protocoldialectconfigurationproperties")
public class ProtocolDialectConfigurationPropertiesResource {

    private final ProtocolDialectConfigurationPropertiesInfoFactory protocolDialectConfigurationPropertiesInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ProtocolDialectConfigurationPropertiesResource(DeviceConfigurationService deviceConfigurationService, ProtocolDialectConfigurationPropertiesInfoFactory protocolDialectConfigurationPropertiesInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.protocolDialectConfigurationPropertiesInfoFactory = protocolDialectConfigurationPropertiesInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * A protocol dialect is a communication protocol of which specific parameters can be changed so that certain tasks
     * can be carried out. E.g. firmware dialect the parameter DatagramPDU size has a default value of 500 bytes however
     * because firmware upgrades are often larger than this, the size of this parameter can be increased.
     *
     * The amount of protocol dialects and which parameters can be changed is determined by the protocol of which the
     * dialect originates. Typically protocol dialects are used for defining communication tasks.
     *
     * @summary Defines a set of properties used to configure the protocol.

     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
     * @param id Id of the protocol property set
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The uniquely identified property set
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public ProtocolDialectConfigurationPropertiesInfo getProtocolDialectConfigurationProperty(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigId") long deviceConfigId,
            @PathParam("id") long id,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        ProtocolDialectConfigurationPropertiesInfo info = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(dc -> dc.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getProtocolDialectConfigurationPropertiesList().stream()
                .filter(props->props.getId()==id)
                .map(props->protocolDialectConfigurationPropertiesInfoFactory.from(props, uriInfo, fieldSelection.getFields()))
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_PROTOCOL_DIALECT_PROPERTIES));

        return info;
    }

    /**
     * A protocol dialect is a communication protocol of which specific parameters can be changed so that certain tasks
     * can be carried out. E.g. firmware dialect the parameter DatagramPDU size has a default value of 500 bytes however
     * because firmware upgrades are often larger than this, the size of this parameter can be increased.
     *
     * The amount of protocol dialects and which parameters can be changed is determined by the protocol of which the
     * dialect originates. Typically protocol dialects are used for defining communication tasks.
     *
     * @summary Defines a set of properties used to configure the protocol.
     *
     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public PagedInfoList<ProtocolDialectConfigurationPropertiesInfo> getProtocolDialectConfigurationProperties(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigId") long deviceConfigId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo,
            @BeanParam JsonQueryParameters queryParameters) {
        List<ProtocolDialectConfigurationPropertiesInfo> infos = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(dc -> dc.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getProtocolDialectConfigurationPropertiesList().stream()
                .map(props->protocolDialectConfigurationPropertiesInfoFactory.from(props, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ProtocolDialectConfigurationPropertiesResource.class)
                .resolveTemplate("deviceTypeId", deviceTypeId)
                .resolveTemplate("deviceConfigId", deviceConfigId);

        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
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
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return protocolDialectConfigurationPropertiesInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
