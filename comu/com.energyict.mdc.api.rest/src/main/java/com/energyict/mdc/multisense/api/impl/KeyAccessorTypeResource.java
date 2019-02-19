package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Path("/devices/{mrid}/keyAccessorTypes")
public class KeyAccessorTypeResource {
    private final String KEY_PROPERTY = "key";

    private final KeyAccessorTypeInfoFactory keyAccessorTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DeviceService deviceService;
    private final SecurityManagementService securityManagementService;
    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public KeyAccessorTypeResource(DeviceService deviceService, ExceptionFactory exceptionFactory,
                                   KeyAccessorTypeInfoFactory keyAccessorTypeInfoFactory,
                                   SecurityManagementService securityManagementService,
                                   PropertyValueInfoService propertyValueInfoService) {
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.keyAccessorTypeInfoFactory = keyAccessorTypeInfoFactory;
        this.securityManagementService = securityManagementService;
        this.propertyValueInfoService = propertyValueInfoService;
    }

    /**
     * Renews the keys for the devices for the given security accessor type.
     *
     * @param mrid mRID of device for which the key will be updated
     * @param keyAccessorTypeId Identifier of the security accessor type
     * @param uriInfo uriInfo
     * @summary renews the key for the device / keyAccessorType
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeId}/renew")
    public Response renewKey(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeId") long keyAccessorTypeId,
                             @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        getSecurityAccessorOrThrowException(keyAccessorTypeId, device).renew();
        return Response.ok().build();
    }

    /**
     * Switch the keys for the devices for the given security accessor type.
     *
     * @param mrid mRID of device for which the key will be updated
     * @param keyAccessorTypeId Identifier of the security accessor type
     * @param uriInfo uriInfo
     * @summary switches the keys for the device / keyAccessorType (temp - actual)
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeId}/switch")
    public Response switchKey(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeId") long keyAccessorTypeId,
                              @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        getSecurityAccessorOrThrowException(keyAccessorTypeId, device).swapValues();
        return Response.ok().build();
    }

    /**
     * Clears the temp value of the keys for the devices for the given security accessor type.
     *
     * @param mrid mRID of device for which the key will be updated
     * @param keyAccessorTypeId Identifier of the security accessor type
     * @param uriInfo uriInfo
     * @summary clears the temp value of the key for the device / keyAccessorType
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeId}/clear")
    public Response clearTempValue(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeId") long keyAccessorTypeId,
                                   @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        getSecurityAccessorOrThrowException(keyAccessorTypeId, device).clearTempValue();
        return Response.ok().build();
    }

    /**
     * Unmark obsolete the actual value of the certificates for the devices for the given security accessor type.
     *
     * @param mrid mRID of device for which the key will be updated
     * @param keyAccessorTypeId Identifier of the security accessor type
     * @param uriInfo uriInfo
     * @summary clears the temp value of the key for the device / keyAccessorType
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeId}/unmarkobsoleteactive")
    public Response unmarkObsoleteActualValue(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeId") long keyAccessorTypeId,
                                              @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        getSecurityAccessorOrThrowException(keyAccessorTypeId, device).getActualValue()
                .filter(CertificateWrapper.class::isInstance)
                .map(CertificateWrapper.class::cast)
                .ifPresent(certificateWrapper -> {
                    certificateWrapper.setWrapperStatus(CertificateWrapperStatus.NATIVE);
                    certificateWrapper.save();
                });
        return Response.ok().build();
    }

    /**
     * Obsolete the temp value of the certificates for the devices for the given security accessor type.
     *
     * @param mrid mRID of device for which the key will be updated
     * @param keyAccessorTypeId Identifier of the security accessor type
     * @param uriInfo uriInfo
     * @summary clears the temp value of the key for the device / keyAccessorType
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeId}/obsolete")
    public Response markObsoleteTempValue(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeId") long keyAccessorTypeId,
                                          @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        getSecurityAccessorOrThrowException(keyAccessorTypeId, device).getTempValue()
                .filter(CertificateWrapper.class::isInstance)
                .map(CertificateWrapper.class::cast)
                .ifPresent(certificateWrapper -> {
                    certificateWrapper.setWrapperStatus(CertificateWrapperStatus.OBSOLETE);
                    certificateWrapper.save();
                });
        return Response.ok().build();
    }

    /**
     * Fetch all defined keyAccessorTypes for a device.
     *
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     *
     * @return Device information and links to related resources
     * @summary View all defined keyAccessortypes for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<KeyAccessorTypeInfo> getKeyAccessorTypes(@PathParam("mrid") String mrid, String name, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType devicetype = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                .getDeviceType();

        List<KeyAccessorTypeInfo> infos = devicetype.getSecurityAccessorTypes().stream()
                .sorted(Comparator.comparing(SecurityAccessorType::getName))
                .map(accessor -> keyAccessorTypeInfoFactory.from(accessor, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(KeyAccessorTypeResource.class)
                .resolveTemplate("mrid", mrid);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * View the contents of a keyAccessorType for a device.
     *
     * @param name The name of the keyAccessorType
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Device information and links to related resources
     * @summary View keyAccessortype identified by name for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{keyAccessorTypeName}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public KeyAccessorTypeInfo getKeyAccessorType(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeName") String name, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceType devicetype = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                .getDeviceType();
        SecurityAccessorType securityAccessorType = getSecurityAccessorTypeOrThrowException(name, devicetype);
        return keyAccessorTypeInfoFactory.from(securityAccessorType, uriInfo, fieldSelection.getFields());
    }

    private SecurityAccessorType getSecurityAccessorTypeOrThrowException(String name, DeviceType devicetype) {
        return devicetype.getSecurityAccessorTypes().stream().filter(keyAccessorType1 -> keyAccessorType1.getName().equals(name)).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSORTYPE_FOR_DEVICE));
    }

    private SecurityAccessorType getSecurityAccessorTypeOrThrowException(long id, DeviceType devicetype) {
        return devicetype.getSecurityAccessorTypes().stream().filter(keyAccessorType1 -> keyAccessorType1.getId() == id).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSORTYPE_FOR_DEVICE));
    }

    private SecurityAccessor<?> getSecurityAccessorOrThrowException(String name, Device device) {
        return device.getSecurityAccessor(getSecurityAccessorTypeOrThrowException(name, device.getDeviceType()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSOR_FOR_DEVICE));
    }

    private SecurityAccessor<?> getSecurityAccessorOrThrowException(long id, Device device) {
        return device.getSecurityAccessor(getSecurityAccessorTypeOrThrowException(id, device.getDeviceType()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSOR_FOR_DEVICE));
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
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     * @summary List the fields available on this type of entity
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return keyAccessorTypeInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeName}/storeTempValue/{value}")
    public Response storeTempValue(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeName") String keyAccessorTypeName,
                                   @PathParam("value") String value,
                                   @Context UriInfo uriInfo) {

        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityAccessor<SecurityValueWrapper> securityAccessor = (SecurityAccessor<SecurityValueWrapper>)getSecurityAccessorOrThrowException(keyAccessorTypeName, device);
        List<PropertyInfo> tempProperties = Arrays.asList(createPropertyInfo(KEY_PROPERTY, value));
        List<PropertySpec> propertySpecs = securityAccessor.getPropertySpecs();
        Map<String, Object> properties = propertyValueInfoService.findPropertyValues(propertySpecs, tempProperties);

        Optional<SecurityValueWrapper> currentTempValue = securityAccessor.getTempValue();

        if (currentTempValue.isPresent()) {
            SecurityValueWrapper tempValueWrapper = currentTempValue.get();
            tempValueWrapper.setProperties(properties);
        } else if (!value.isEmpty()) {
            SecurityValueWrapper securityValueWrapper = securityManagementService.newSymmetricKeyWrapper(securityAccessor.getKeyAccessorType());
            securityValueWrapper.setProperties(properties);
            securityAccessor.setTempValue(securityValueWrapper);
            securityAccessor.save();
        }
        return Response.ok().build();
    }

    /**
     * Mark actual key value as temporary for the device for the given security accessor type.
     *
     * @param mrid mRID of device for which the key will be updated
     * @param keyAccessorTypeName name of the security accessor type
     * @param uriInfo uriInfo
     * @summary Mark actual key value as temporary
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeName}/markKeyTemporary")
    public Response markKeyTemporary(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeName") String keyAccessorTypeName,
                                          @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityAccessor<SecurityValueWrapper> securityAccessor = (SecurityAccessor<SecurityValueWrapper>)getSecurityAccessorOrThrowException(keyAccessorTypeName, device);
        securityAccessor.setTemporary(true);
        securityAccessor.save();
        return Response.ok().build();
    }

    /**
     * Unmark actual key value as temporary for the device for the given security accessor type.
     *
     * @param mrid mRID of device for which the key will be updated
     * @param keyAccessorTypeName name of the security accessor type
     * @param uriInfo uriInfo
     * @summary Unmark actual key value as temporary
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeName}/unmarkKeyTemporary")
    public Response unmarkKeyTemporary(@PathParam("mrid") String mrid, @PathParam("keyAccessorTypeName") String keyAccessorTypeName,
                                          @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityAccessor<SecurityValueWrapper> securityAccessor = (SecurityAccessor<SecurityValueWrapper>)getSecurityAccessorOrThrowException(keyAccessorTypeName, device);
        securityAccessor.setTemporary(false);
        securityAccessor.save();
        return Response.ok().build();
    }

    private PropertyInfo createPropertyInfo(String key, String value) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = key;
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>(value, null);
        return propertyInfo;
    }
}

