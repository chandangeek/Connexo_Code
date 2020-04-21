/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.device.config.DeviceConfigConstants;
import com.energyict.mdc.common.device.config.DeviceSecurityAccessorType;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.energyict.mdc.device.configuration.rest.impl.DeviceMessageSpecInfo.NOT_SET;

public class SecurityAccessorTypeOnDeviceTypeResource {
    private final ResourceHelper resourceHelper;
    private final SecurityManagementService securityManagementService;
    private final SecurityAccessorTypeInfoFactory keyFunctionTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceMessageService deviceMessageService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final Thesaurus thesaurus;
    protected static final String MASTERKEY = "MasterKey";

    @Inject
    public SecurityAccessorTypeOnDeviceTypeResource(ResourceHelper resourceHelper, SecurityManagementService securityManagementService,
                                                    SecurityAccessorTypeInfoFactory keyFunctionTypeInfoFactory, ExceptionFactory exceptionFactory,
                                                    DeviceMessageSpecificationService deviceMessageSpecificationService, MdcPropertyUtils mdcPropertyUtils,
                                                    DeviceMessageService deviceMessageService, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.securityManagementService = securityManagementService;
        this.keyFunctionTypeInfoFactory = keyFunctionTypeInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.deviceMessageService = deviceMessageService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE, DeviceConfigConstants.VIEW_DEVICE_TYPE})
    public PagedInfoList getDeviceTypeSecurityAccessors(@PathParam("deviceTypeId") long id, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<SecurityAccessorTypeInfo> infos = deviceType.getSecurityAccessors().stream()
                .map(keyFunctionTypeInfoFactory::from)
                .sorted(Comparator.comparing(k -> k.name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        for (SecurityAccessorTypeInfo info : infos) {
            Optional<SecurityAccessorType> securityAccessorType = deviceType.getSecurityAccessorTypes().stream()
                    .filter(s -> s.getId() == info.id).findFirst();
            if (securityAccessorType.isPresent()) {
                deviceType.getDefaultKeyOfSecurityAccessorType(securityAccessorType.get())
                        .ifPresent(v -> info.defaultServiceKey = v);
            }
        }
        return PagedInfoList.fromCompleteList("securityaccessors", infos, queryParameters);
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE)
    @Path("/unassigned")
    public PagedInfoList getSecurityAccessorsUnassignedToDeviceType(@PathParam("deviceTypeId") long id, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<SecurityAccessorTypeInfo> infos = subtract(securityManagementService.getSecurityAccessorTypes(SecurityAccessorType.Purpose.DEVICE_OPERATIONS), deviceType.getSecurityAccessorTypes())
                .map(keyFunctionTypeInfoFactory::from)
                .sorted(Comparator.comparing(k -> k.name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("securityaccessors", infos, queryParameters);
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE)
    @Path("/{securityAccessorId}/wrappers")
    public PagedInfoList getDeviceSecurityAccessorsWrappers(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("securityAccessorId") long securityAccessorId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        List<DeviceSecurityAccessorType> deviceSecurityAccessor = deviceType.getDeviceSecurityAccessorType();

        List<SecurityAccessorTypeInfo> infos = new ArrayList<>();
        infos.add(keyFunctionTypeInfoFactory.getNotSetSecurityAccessorWrapper());

        List<SecurityAccessorTypeInfo> availableWrappers = deviceSecurityAccessor.stream()
                .map(f -> f.getSecurityAccessor())
                .filter(f -> f.isWrapper())
                .map(keyFunctionTypeInfoFactory::from)
                .collect(Collectors.toList());
        availableWrappers.sort(Comparator.comparing(f -> f.name, String.CASE_INSENSITIVE_ORDER));
        infos.addAll(availableWrappers);

        return PagedInfoList.fromCompleteList("securityaccessors", infos, queryParameters);
    }

    private static <T> Stream<T> subtract(Collection<T> minuend, Collection<T> subtrahend) {
        Set<T> toRemove = new HashSet<>(subtrahend);
        return minuend.stream()
                .filter(item -> !toRemove.contains(item));
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE, DeviceConfigConstants.VIEW_DEVICE_TYPE})
    @Path("/{securityAccessorId}")
    public SecurityAccessorTypeInfo getDeviceTypeSecurityAccessor(@PathParam("deviceTypeId") long id, @PathParam("securityAccessorId") long securityAccessorId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<SecurityAccessorTypeOnDeviceType> securityAccessors = deviceType.getSecurityAccessors();
        SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType = securityAccessors.stream()
                .filter(sa -> sa.getSecurityAccessorType().getId() == securityAccessorId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));
        SecurityAccessorTypeInfo accessorTypeInfo = keyFunctionTypeInfoFactory.withSecurityLevels(securityAccessorTypeOnDeviceType);
        deviceType.getDefaultKeyOfSecurityAccessorType(securityAccessorTypeOnDeviceType.getSecurityAccessorType()).ifPresent(v -> accessorTypeInfo.defaultServiceKey = v);
        return accessorTypeInfo;
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE)
    public Response addSecurityAccessorTypesToDeviceType(@PathParam("deviceTypeId") long deviceId, SecurityAccessorsForDeviceTypeInfo info) {
        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(deviceId, info.version, info.name);
        SecurityAccessorType[] securityAccessorTypes = info.securityAccessors.stream()
                .map(securityAccessorInfo -> resourceHelper.lockSecurityAccessorTypeOrThrowException(securityAccessorInfo.id, securityAccessorInfo.version, securityAccessorInfo.name))
                .toArray(SecurityAccessorType[]::new);

        for (SecurityAccessorType securityAccessorType : securityAccessorTypes) {
            deviceType.addDeviceSecurityAccessorType(new DeviceSecurityAccessorType(Optional.empty(), securityAccessorType));
        }
        deviceType.update();
        return Response.ok().build();
    }

    @PUT
    @Path("/{securityAccessorId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE)
    public Response editKeyRenewalOnSecurityAccDeviceType(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("securityAccessorId") long securityAccessorId, KeyRenewalInfo keyRenewalInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        SecurityAccessorTypeOnDeviceType securityAccessorOnDeviceType = findSecAccessorOnDeviceTypeOrThrowException(deviceType, securityAccessorId);

        deviceType.setWrappingSecurityAccessor(securityAccessorOnDeviceType.getDeviceSecurityAccessorType(), getWrappingAccessor(keyRenewalInfo.wrapperAccessorId, deviceType));

        DeviceMessageId deviceMessageId = (keyRenewalInfo.keyRenewalCommandSpecification != null &&
                keyRenewalInfo.keyRenewalCommandSpecification.id != null &&
                keyRenewalInfo.keyRenewalCommandSpecification.id != NOT_SET) ?
                DeviceMessageId.valueOf(keyRenewalInfo.keyRenewalCommandSpecification.id.toString()) : null;
        DeviceMessageId serviceDeviceMessageId = (keyRenewalInfo.serviceKeyRenewalCommandSpecification != null &&
                keyRenewalInfo.serviceKeyRenewalCommandSpecification.id != null &&
                keyRenewalInfo.serviceKeyRenewalCommandSpecification.id != NOT_SET) ?
                DeviceMessageId.valueOf(keyRenewalInfo.serviceKeyRenewalCommandSpecification.id.toString()) : null;
        if (deviceMessageId != null || serviceDeviceMessageId != null) {
            SecurityAccessorTypeOnDeviceType.KeyRenewalBuilder keyRenewAlBuilder = securityAccessorOnDeviceType.newKeyRenewalBuilder(deviceMessageId, serviceDeviceMessageId);
            if (deviceMessageId != null && keyRenewalInfo.properties != null) {
                addProperties(keyRenewAlBuilder, deviceMessageId.dbValue(), keyRenewalInfo.properties, false);
            }
            if (serviceDeviceMessageId != null && keyRenewalInfo.serviceProperties != null) {
                addProperties(keyRenewAlBuilder, serviceDeviceMessageId.dbValue(), keyRenewalInfo.serviceProperties, true);
            }
            keyRenewAlBuilder.add();
        } else {
            securityAccessorOnDeviceType.resetKeyRenewal();
        }
        return Response.ok().build();
    }

    private void addProperties(SecurityAccessorTypeOnDeviceType.KeyRenewalBuilder builder, long deviceMessageIdValue, List<PropertyInfo> properties, boolean isServiceKey) {
        DeviceMessageSpec deviceMessageSpec = deviceMessageSpecificationService.findMessageSpecById(deviceMessageIdValue).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_SPEC));
        try {
            for (PropertySpec propertySpec : deviceMessageSpec.getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, properties);
                if (propertyValue != null) {
                    builder.addProperty(propertySpec.getName(), propertyValue, isServiceKey);
                }
            }
        } catch (LocalizedFieldValidationException e) {
            throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties." + e.getViolatingProperty());
        }
    }

    private Optional<SecurityAccessorType> getWrappingAccessor(Long wrapperAccessorId, DeviceType deviceType) {
        if (wrapperAccessorId == null || wrapperAccessorId == keyFunctionTypeInfoFactory.getNotSetSecurityAccessorWrapper().id) {
            return Optional.empty();
        }
        return Optional.of(findSecAccessorOnDeviceTypeOrThrowException(deviceType, wrapperAccessorId).getSecurityAccessorType());
    }

    @DELETE
    @Transactional
    @Path("/{securityAccessorId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed(DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE)
    public Response removeSecurityAccessorTypeFromDeviceType(@PathParam("deviceTypeId") long id, @PathParam("securityAccessorId") long securityAccessorId, SecurityAccessorsForDeviceTypeInfo info) {

        DeviceType deviceType = resourceHelper.lockDeviceTypeOrThrowException(id, info.version, info.name);
        deviceType.getDeviceSecurityAccessorType()
                .stream()
                .filter(f -> f.getWrappingSecurityAccessor().isPresent() && f.getWrappingSecurityAccessor().get().getId() == securityAccessorId)
                .map(f -> f.getSecurityAccessor())
                .findAny()
                .ifPresent(securityAccessor -> {
                    throw exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.SECACC_WRAPPER_IN_USE, securityAccessor.getName()).get();
                });

        DeviceSecurityAccessorType securityAccessorType = deviceType.getDeviceSecurityAccessorType().stream()
                .filter(kFType -> kFType.getSecurityAccessor().getId() == securityAccessorId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));

        deviceType.getConfigurations().stream()
                .filter(deviceConfiguration -> securityAccessorType.getSecurityAccessor().equals(deviceConfiguration.getDeviceProtocolProperties().getProperty(MASTERKEY)))
                .findAny()
                .ifPresent(deviceConfiguration -> {
                    throw exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.SECACC_MASTER_KEY_IN_USE, deviceConfiguration.getName()).get();
                });

        if (deviceType.removeDeviceSecurityAccessorType(securityAccessorType)) {
            deviceType.update();
        }
        return Response.noContent().build();
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE, DeviceConfigConstants.VIEW_DEVICE_TYPE})
    @Path("/securitycategorycommands")
    public List<DeviceMessageSpecInfo> getCommandsForSecurityCategory(@PathParam("deviceTypeId") long id) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        DeviceMessageCategory securityCategory = deviceMessageSpecificationService.getSecurityCategory();
        List<DeviceMessageSpec> deviceMessageSpecs = getEnabledAndAuthorizedDeviceMessageSpecsIn(securityCategory, deviceType);
        List<DeviceMessageId> deviceMessageIds = deviceMessageService.findKeyRenewalMessages();
        List<DeviceMessageSpecInfo> infos = new ArrayList<>();
        infos.add(DeviceMessageSpecInfo.getNotSetDeviceMessageSpecInfo());
        infos.addAll(deviceMessageSpecs.stream()
                .filter(deviceMessageSpec -> deviceMessageIds.contains(deviceMessageSpec.getId()))
                .map(deviceMessageSpec -> DeviceMessageSpecInfo.from(deviceMessageSpec, mdcPropertyUtils))
                .collect(Collectors.toList()));
        return infos;
    }

    public List<DeviceMessageSpec> getEnabledAndAuthorizedDeviceMessageSpecsIn(DeviceMessageCategory category, DeviceType deviceType) {
        List<Long> ids = deviceType.getDeviceProtocolPluggableClass()
                .map(pluggableClass -> pluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getId)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        return category.getMessageSpecifications()
                .stream()
                .filter(deviceMessageSpec -> ids.contains(deviceMessageSpec.getId().dbValue())) // limit to device message specs supported by the protocol
                .collect(Collectors.toList());
    }

    private SecurityAccessorTypeOnDeviceType findSecAccessorOnDeviceTypeOrThrowException(DeviceType deviceType, long securityAccessorId) {
        return deviceType.getSecurityAccessors().stream()
                .filter(securityAccessorTypeOnDeviceType -> securityAccessorTypeOnDeviceType.getSecurityAccessorType().getId() == securityAccessorId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));
    }

    /**
     * Sets the default key value for the device type for the given security accessor type.
     *
     * @param deviceTypeId       Identifier of the device type for which the default key will be updated
     * @param securityAccessorId Identifier of the security accessor for which the key will be updated
     * @param info               Contains default key value for the security accessor type
     * @summary sets/updates the default key value for the device type for the given security accessor type
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    @Path("/{securityAccessorId}/defaultkey")
    public Response setDefaultKeySecurityAccessorTypeValue(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("securityAccessorId") long securityAccessorId, ServiceKeyDefultValueInfo info) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        SecurityAccessorType keyFunctionType = deviceType.getSecurityAccessorTypes().stream()
                .filter(kFType -> kFType.getId() == securityAccessorId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE));

        deviceType.updateDefaultKeyOfSecurityAccessorType(keyFunctionType, info.value);
        return Response.ok().build();
    }

}