package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/devices/{mrid}/securitypropertysets")
public class DeviceSecurityPropertySetResource {

    private final DeviceSecurityPropertySetInfoFactory deviceSecurityPropertySetInfoFactory;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public DeviceSecurityPropertySetResource(DeviceService deviceService, DeviceSecurityPropertySetInfoFactory deviceSecurityPropertySetInfoFactory, ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService, MdcPropertyUtils mdcPropertyUtils) {
        this.deviceService = deviceService;
        this.deviceSecurityPropertySetInfoFactory = deviceSecurityPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    /**
     * Models named set of security properties whose values are managed against a Device.
     * The exact set of PropertySpecs that are used is determined by the AuthenticationDeviceAccessLevel
     * and/or EncryptionDeviceAccessLevel select in the SecurityPropertySet.
     * That in turn depends on the actual DeviceProtocol.
     *
     * @summary Fetch a uniquely identified device security property set
     *
     * @param mrid mRID of the device
     * @param deviceSecurityPropertySetId Id of the device's security set
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified security property set
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{securityPropertySetId}")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public DeviceSecurityPropertySetInfo getDeviceSecurityPropertySet(@PathParam("mrid") String mrid, @PathParam("securityPropertySetId") long deviceSecurityPropertySetId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityPropertySet securityPropertySet = device.getDeviceConfiguration().getSecurityPropertySets().stream()
                .filter(sps -> sps.getId() == deviceSecurityPropertySetId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SECURITY_PROPERTY_SET));

        return deviceSecurityPropertySetInfoFactory.from(device, securityPropertySet, uriInfo, fieldSelection.getFields());
    }

    /**
     * Models named set of security properties whose values are managed against a Device.
     * The exact set of PropertySpecs that are used is determined by the AuthenticationDeviceAccessLevel
     * and/or EncryptionDeviceAccessLevel select in the SecurityPropertySet.
     * That in turn depends on the actual DeviceProtocol.
     *
     * @summary Fetch a SET OF device security property sets
     *
     * @param mrid mRID of the device
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     *
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public PagedInfoList<DeviceSecurityPropertySetInfo> getDeviceSecurityPropertySets(@PathParam("mrid") String mrid, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        List<DeviceSecurityPropertySetInfo> infos = device.getDeviceConfiguration().getSecurityPropertySets().stream()
                .sorted(Comparator.comparing(SecurityPropertySet::getName))
                .map(sps -> deviceSecurityPropertySetInfoFactory.from(device, sps, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceSecurityPropertySetResource.class)
                .resolveTemplate("mrid", device.getmRID());
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * Models named set of security properties whose values are managed against a Device.
     * The exact set of PropertySpecs that are used is determined by the AuthenticationDeviceAccessLevel
     * and/or EncryptionDeviceAccessLevel select in the SecurityPropertySet.
     * That in turn depends on the actual DeviceProtocol.
     *
     * @summary Uptdate a uniquely identified device security property set
     *
     * @param mrid mRID of the device
     * @param deviceSecurityPropertySetId Id of the device's security set up update
     * @param propertySetInfo Payload describing the updated values for the security set
     * @param uriInfo uriInfo
     * @return Updated security set
     */
    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    @Path("/{securityPropertySetId}")
    public DeviceSecurityPropertySetInfo createDeviceSecurityPropertySet(@PathParam("mrid") String mrid, @Context UriInfo uriInfo,
                                                                         @PathParam("securityPropertySetId") long deviceSecurityPropertySetId,
                                                                         DeviceSecurityPropertySetInfo propertySetInfo) {
        if (propertySetInfo.device==null || propertySetInfo.device.version==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "device.version");
        }
        Device device = deviceService.findAndLockDeviceBymRIDAndVersion(mrid, propertySetInfo.device.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE));

        SecurityPropertySet securityPropertySet = deviceConfigurationService.findSecurityPropertySet(deviceSecurityPropertySetId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_SECURITY_PROPERTY_SET));

        TypedProperties typedProperties = TypedProperties.empty();
        for (PropertySpec propertySpec : securityPropertySet.getPropertySpecs()) {
            if (propertyHasValue(propertySpec, propertySetInfo.properties)) {
                Object newPropertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, propertySetInfo.properties);
                typedProperties.setProperty(propertySpec.getName(), newPropertyValue);
            } else {
                typedProperties.removeProperty(propertySpec.getName());
            }
        }

        device.setSecurityProperties(securityPropertySet, typedProperties);
        device.save();
        return deviceSecurityPropertySetInfoFactory.from(device, securityPropertySet, uriInfo, Collections.emptyList());
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
        return deviceSecurityPropertySetInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

    private boolean propertyHasValue(PropertySpec propertySpec, Collection<PropertyInfo> propertyInfos) {
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (propertyInfo.key.equals(propertySpec.getName())) {
                if (propertyInfo.getPropertyValueInfo() != null && propertyInfo.getPropertyValueInfo().propertyHasValue!=null) {
                    return propertyInfo.getPropertyValueInfo().propertyHasValue;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
