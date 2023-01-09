/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.response.ServiceKeyInjectionResponse;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory;
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
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/devices/{mrid}/keyAccessors")
public class KeyAccessorResource {

    private static final String KEY_PROPERTY = "key";
    private static final String LABEL_PROPERTY = "label";

    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final HsmEnergyService hsmEnergyService;
    private final KeyAccessorInfoFactory keyAccessorInfoFactory;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;
    private final SecurityManagementService securityManagementService;

    @Inject
    public KeyAccessorResource(DeviceService deviceService, ExceptionFactory exceptionFactory,
                               HsmEnergyService hsmEnergyService, KeyAccessorInfoFactory keyAccessorInfoFactory,
                               SecurityAccessorInfoFactory securityAccessorInfoFactory, MdcPropertyUtils mdcPropertyUtils,
                               SecurityManagementService securityManagementService) {
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.hsmEnergyService = hsmEnergyService;
        this.keyAccessorInfoFactory = keyAccessorInfoFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
        this.securityManagementService = securityManagementService;
    }

    /**
     * Fetch all defined keyAccessors for a device.
     *
     * @param uriInfo         uriInfo
     * @param fieldSelection  field selection
     * @param queryParameters queryParameters
     * @return Device information and links to related resources
     * @summary View all defined keyAccessors for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<KeyAccessorInfo> getKeyAccessors(@PathParam("mrid") String mrid, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));

        List<KeyAccessorInfo> infos = device.getSecurityAccessors().stream()
                .sorted(Comparator.comparing(accessor -> accessor.getSecurityAccessorType().getName()))
                .map(accessor -> keyAccessorInfoFactory.from(accessor, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(KeyAccessorResource.class)
                .resolveTemplate("mrid", device.getmRID());
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * View the contents of a keyAccessor for a device.
     *
     * @param name           The name of the keyAccessor
     * @param uriInfo        uriInfo
     * @param fieldSelection field selection
     * @return Device information and links to related resources
     * @summary View keyAccessor identified by name for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{keyAccessorName}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public KeyAccessorInfo getKeyAccessor(@PathParam("mrid") String mrid, @PathParam("keyAccessorName") String name, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityAccessor securityAccessor = getSecurityAccessor(name, device);
        return keyAccessorInfoFactory.from(securityAccessor, uriInfo, fieldSelection.getFields());
    }

    /**
     * Wraps the service key value by master key for service key injection.
     *
     * @param mrid                  mRID of device for which the key injection will be prepared
     * @param masterKeyAccessorName Identifier of the security accessor type for master key
     * @param uriInfo               uriInfo
     * @return Wrapped key for service key injection
     * @summary Wraps the key value by master key identified by master key accessor name
     */
    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{masterKeyAccessorName}/wrapServiceKeyValue")
    public Response wrapKeyForServiceKeyInjection(@PathParam("mrid") String mrid, @PathParam("masterKeyAccessorName") String masterKeyAccessorName,
                                                  HashMap<String, String> serviceKeyParam, @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND,
                        MessageSeeds.NO_SUCH_DEVICE));
        String serviceKeyValue = serviceKeyParam.get("value");
        Map<String, String> masterKeyAccessor = getProperties(getSecurityAccessor(masterKeyAccessorName, device));
        try {
            HardwareSecurityModuleInfo hsmInfo = new HardwareSecurityModuleInfo();
            hsmInfo.preparedServiceKey = hsmEnergyService.prepareServiceKey(serviceKeyValue,
                    masterKeyAccessor.get(LABEL_PROPERTY), masterKeyAccessor.get(KEY_PROPERTY)).toHex();
            return Response.ok(hsmInfo).build();
        } catch (HsmBaseException e) {
            throw exceptionFactory.newException(Response.Status.INTERNAL_SERVER_ERROR, MessageSeeds.HSM_EXCEPTION, e.getMessage());
        }
    }

    /**
     * Renews the keys for the devices for the given security accessor type.
     *
     * @param mrid    mRID of device for which the key will be updated
     * @param info    Identifier of the security accessor type
     * @param uriInfo uriInfo
     * @summary renews the key for the device / keyAccessorType
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/serviceKeyInjection")
    public Response executeServiceKeyInjection(@PathParam("mrid") String mrid, HardwareSecurityModuleInfo info, @Context UriInfo uriInfo) {
        deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        try {
            ServiceKeyInjectionResponse response = hsmEnergyService.serviceKeyInjection(info.preparedServiceKey,
                    info.signature, info.verifyKey);
            info.injectedServiceKey = Base64.getEncoder().encodeToString(((Message)response).getBytes());
            info.warning = response.getWarning();
            return Response.ok(info).build();
        } catch (HsmBaseException e) {
            throw exceptionFactory.newException(Response.Status.INTERNAL_SERVER_ERROR, MessageSeeds.HSM_EXCEPTION, e.getMessage());
        }
    }

    /**
     * Mark/Unmark key as service key for the device for the given security accessor type.
     *
     * @param mrid mRID of device for which the key will be marked/unmarked
     * @param keyAccessorName Identifier of the security accessor type for key
     * @param uriInfo uriInfo
     * @summary Mark/Unmark key as service key for the device / keyAccessor
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorName}/servicekey")
    public Response markServiceKey(@PathParam("mrid") String mrid, @PathParam("keyAccessorName") String keyAccessorName,
                                   HashMap<String, Boolean> serviceKeyParam,
                                   @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityAccessor securityAccessor = getSecurityAccessor(keyAccessorName, device);
        securityAccessor.setServiceKey(serviceKeyParam.get("serviceKey"));
        securityAccessor.save();
        return Response.ok().build();
    }

    /**
     * Set the passive/temp key for the device for the given security accessor type.
     *
     * @param mrid mRID of device for which the key will be updated
     * @param keyAccessorName Identifier of the security accessor type for key
     * @param uriInfo uriInfo
     * @summary sets the passive/temp key for the device / keyAccessor
     *          (this request is applicable only for HSM key accessors)
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorName}/tempvalue")
    public Response storeTempValue(@PathParam("mrid") String mrid, @PathParam("keyAccessorName") String keyAccessorName, HashMap<String, String> info,
                                   @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityAccessor securityAccessor = getSecurityAccessor(keyAccessorName, device);
        if (securityAccessor.getSecurityAccessorType().keyTypeIsHSM()) {
            String value = info.get("value");
            String injectedValue = info.get("injectedValue");
            boolean serviceKey = Boolean.valueOf(info.get("serviceKey"));
            String wrappedValue = info.get("wrappedValue");

            Map<String, Object> properties = new HashMap<>();
            properties.put(KEY_PROPERTY, value);
            properties.put(LABEL_PROPERTY, securityAccessor.getSecurityAccessorType().getHsmKeyType().getLabel());

            Optional<SecurityValueWrapper> currentTempValue = securityAccessor.getTempValue();
            if (currentTempValue.isPresent()) {
                SecurityValueWrapper tempValueWrapper = currentTempValue.get();
                ((HsmKey)tempValueWrapper).setSmartMeterKey(injectedValue);
                ((HsmKey)tempValueWrapper).setServiceKey(serviceKey);
                ((HsmKey)tempValueWrapper).setWrappedKey(wrappedValue);
                tempValueWrapper.setProperties(properties);
            } else if (!value.isEmpty()) {
                SecurityValueWrapper securityValueWrapper = securityManagementService.newSymmetricKeyWrapper(securityAccessor
                        .getSecurityAccessorType());
                ((HsmKey)securityValueWrapper).setSmartMeterKey(injectedValue);
                ((HsmKey)securityValueWrapper).setServiceKey(serviceKey);
                ((HsmKey)securityValueWrapper).setWrappedKey(wrappedValue);
                securityValueWrapper.setProperties(properties);
                securityAccessor.setTempValue(securityValueWrapper);
                securityAccessor.save();
            }
            return Response.ok().build();
        } else {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.SECURITY_ACCESSOR_TYPE_IS_NOT_HSM);
        }
    }

    /**
     * Validates if key accessor exists and has value for a device.
     *
     * @param mrid mRID of device for which the key will be validated
     * @param name Name of the key accessor
     * @param uriInfo uriInfo
     * @return Device information and links to related resources
     * @summary Validates key accessor identified by name for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{keyAccessorName}/validate")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response validateKeyAccessorType(@PathParam("mrid") String mrid, @PathParam("keyAccessorName") String name, @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityAccessor securityAccessor = getSecurityAccessor(name, device);
        if (!securityAccessor.getActualValue().isPresent()) {
            return Response.noContent().build();
        }
        return Response.ok().build();
    }

    private SecurityAccessor getSecurityAccessor(String name, Device device) {
        return device.getSecurityAccessors().stream()
                .filter(keyAccessor -> keyAccessor.getSecurityAccessorType().getName().equals(name))
                .findAny()
                .orElseThrow(() -> device.getDeviceType().getSecurityAccessorTypes().stream().anyMatch(sat -> sat.getName().equals(name)) ?
                        exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSOR_FOR_DEVICE) :
                        exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSORTYPE_FOR_DEVICE));
    }

    private Map<String, String> getProperties(SecurityAccessor<?> securityAccessor) {
        return mdcPropertyUtils.convertPropertySpecsToPropertyInfos(securityAccessor.getPropertySpecs(),
                securityAccessorInfoFactory.getPropertiesActualValue(securityAccessor))
                .stream()
                .collect(Collectors.toMap(info -> info.key, info -> getPropertyValue(info.propertyValueInfo),
                        (oldValue, newValue) -> oldValue));
    }

    private String getPropertyValue(PropertyValueInfo<?> propertyValueInfo) {
        return Optional.ofNullable(propertyValueInfo)
                .map(PropertyValueInfo::getValue)
                .map(String.class::cast)
                .orElseThrow(() -> exceptionFactory.newException(Response.Status.NOT_FOUND,
                        MessageSeeds.NO_SUCH_KEYACCESSOR_FOR_DEVICE));
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
        return keyAccessorInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }
}
