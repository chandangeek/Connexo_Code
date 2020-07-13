/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.collections.KPermutation;
import com.energyict.mdc.common.device.config.DeviceConfigConstants;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionBuilder;

import org.glassfish.jersey.media.multipart.FormDataParam;

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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Path("/devicetypes/{deviceTypeId}/firmwares")
public class FirmwareVersionResource {
    private final FirmwareService firmwareService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final FirmwareVersionInfoFactory versionFactory;

    @Inject
    public FirmwareVersionResource(FirmwareService firmwareService, ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, FirmwareVersionInfoFactory versionFactory) {
        this.firmwareService = firmwareService;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.versionFactory = versionFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE, DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public PagedInfoList getFilteredFirmwareVersions(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        Finder<FirmwareVersion> firmwaresFinder = firmwareService.findAllFirmwareVersions(resourceHelper.getFirmwareFilter(filter, deviceType));
        List<FirmwareVersion> foundFirmwares = firmwaresFinder.from(queryParameters).find();
        return PagedInfoList.fromPagedList("firmwares", versionFactory.from(foundFirmwares), queryParameters);
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE, DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response getFirmwareVersionById(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id) {
        FirmwareVersion firmwareVersion = resourceHelper.findFirmwareVersionByIdOrThrowException(id);
        return Response.ok().entity(versionFactory.fullInfo(firmwareVersion)).build();
    }

    @POST
    @Transactional
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response validateFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, FirmwareVersionInfo firmwareVersionInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareVersionBuilder versionToValidate = getFirmwareVersionBuilder(deviceType, firmwareVersionInfo.firmwareVersion,
                firmwareVersionInfo.firmwareStatus.id, firmwareVersionInfo.firmwareType.id, firmwareVersionInfo.imageIdentifier,
                firmwareVersionInfo.meterFirmwareDependency == null ? null : ((Number) firmwareVersionInfo.meterFirmwareDependency.id).longValue(),
                firmwareVersionInfo.communicationFirmwareDependency == null ? null : ((Number) firmwareVersionInfo.communicationFirmwareDependency.id).longValue(),
                firmwareVersionInfo.auxiliaryFirmwareDependency == null ? null : ((Number) firmwareVersionInfo.auxiliaryFirmwareDependency.id).longValue());

        if (firmwareVersionInfo.fileSize != null) {
            versionToValidate.setExpectedFirmwareSize(firmwareVersionInfo.fileSize);
        }
        versionToValidate.validate();
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response saveFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId,
                                        @FormDataParam("firmwareFile") InputStream fileInputStream,
                                        @FormDataParam("firmwareVersion") String firmwareVersion,
                                        @FormDataParam("firmwareType") String typeString,
                                        @FormDataParam("firmwareStatus") String statusString,
                                        @FormDataParam("imageIdentifier") String imageIdentifier,
                                        @FormDataParam("meterFirmwareDependency") Long meterFWDependency,
                                        @FormDataParam("communicationFirmwareDependency") Long comFWDependency,
                                        @FormDataParam("auxiliaryFirmwareDependency") Long auxFWDependency) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        FirmwareType firmwareType = parseFirmwareTypeField(typeString).orElse(null);
        FirmwareStatus firmwareStatus = parseFirmwareStatusField(statusString).orElse(null);

        FirmwareVersionBuilder firmwareVersionBuilder = getFirmwareVersionBuilder(deviceType, firmwareVersion, firmwareStatus, firmwareType, imageIdentifier,
                meterFWDependency, comFWDependency, auxFWDependency);

        byte[] firmwareFile = loadFirmwareFile(fileInputStream);
        resourceHelper.findSecurityAccessorForSignatureValidation(deviceTypeId)
                .ifPresent(securityAccessor -> resourceHelper.checkFirmwareVersion(deviceType, securityAccessor, firmwareFile));
        setExpectedFirmwareSize(firmwareVersionBuilder, firmwareFile);
        FirmwareVersion version = firmwareVersionBuilder.create();
        setFirmwareFile(version, firmwareFile);

        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }

    @PUT
    @Transactional
    @Path("/reorder")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response reorderFirmwareVersions(@PathParam("deviceTypeId") long deviceTypeId, List<FirmwareVersionInfo> firmwareVersionInfoList) {
        if (firmwareVersionInfoList != null && !firmwareVersionInfoList.isEmpty()) {
            DeviceType deviceType = resourceHelper.findAndLockDeviceTypeOrThrowException(deviceTypeId);
            List<? extends FirmwareVersion> sortedFirmwareVersions = firmwareService.getOrderedFirmwareVersions(deviceType);
            long[] current = sortedFirmwareVersions.stream().mapToLong(FirmwareVersion::getId).toArray();
            long[] target = firmwareVersionInfoList.stream().mapToLong(firmwareInfo -> firmwareInfo.id).toArray();
            KPermutation kPermutation = KPermutation.of(current, target);
            if (!kPermutation.isNeutral(sortedFirmwareVersions)) {
                firmwareService.reorderFirmwareVersions(deviceType, kPermutation);
            }
        }
        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Path("/{id}/validate")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response validateEditFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareVersionInfo firmwareVersionInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        FirmwareVersion firmwareVersion = resourceHelper.findFirmwareVersionByIdOrThrowException(id);
        checkIfEditableOrThrowException(firmwareVersion);
        if (!FirmwareStatus.FINAL.equals(firmwareVersion.getFirmwareStatus())
                || !firmwareService.isFirmwareVersionInUse(firmwareVersion.getId())) {

            firmwareVersion.setFirmwareVersion(firmwareVersionInfo.firmwareVersion);
            firmwareVersion.setFirmwareStatus(firmwareVersionInfo.firmwareStatus.id);

            if (firmwareVersionInfo.fileSize != null) {
                firmwareVersion.setExpectedFirmwareSize(firmwareVersionInfo.fileSize);
            }
        }
        if (firmwareService.imageIdentifierExpectedAtFirmwareUpload(deviceType)) {
            firmwareVersion.setImageIdentifier(firmwareVersionInfo.imageIdentifier);
        }
        firmwareVersion.setMeterFirmwareDependency(Optional.ofNullable(firmwareVersionInfo.meterFirmwareDependency)
                .map(idWithName -> idWithName.id) // nullable too
                .map(Number.class::cast)
                .map(Number::longValue)
                .map(resourceHelper::findFirmwareVersionByIdOrThrowException)
                .orElse(null));
        firmwareVersion.setCommunicationFirmwareDependency(Optional.ofNullable(firmwareVersionInfo.communicationFirmwareDependency)
                .map(idWithName -> idWithName.id) // nullable too
                .map(Number.class::cast)
                .map(Number::longValue)
                .map(resourceHelper::findFirmwareVersionByIdOrThrowException)
                .orElse(null));
        firmwareVersion.setAuxiliaryFirmwareDependency(Optional.ofNullable(firmwareVersionInfo.auxiliaryFirmwareDependency)
                .map(idWithName -> idWithName.id) // nullable too
                .map(Number.class::cast)
                .map(Number::longValue)
                .map(resourceHelper::findFirmwareVersionByIdOrThrowException)
                .orElse(null));
        firmwareVersion.validate();
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response editFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId,
                                        @PathParam("id") long id,
                                        @FormDataParam("firmwareFile") InputStream fileInputStream,
                                        @FormDataParam("firmwareVersion") String fwVersion,
                                        @FormDataParam("firmwareStatus") String status,
                                        @FormDataParam("imageIdentifier") String imageId,
                                        @FormDataParam("version") @DefaultValue("0") long version,
                                        @FormDataParam("meterFirmwareDependency") Long meterFWDependency,
                                        @FormDataParam("communicationFirmwareDependency") Long comFWDependency,
                                        @FormDataParam("auxiliaryFirmwareDependency") Long auxFWDependency) {
        DeviceType deviceType = resourceHelper.findAndLockDeviceTypeOrThrowException(deviceTypeId); // to prevent from changing order of firmwares during the operation

        FirmwareVersion firmwareVersion = resourceHelper.lockFirmwareVersionOrThrowException(id, version, fwVersion);
        firmwareVersion.setImageIdentifier(imageId);

        firmwareVersion.setMeterFirmwareDependency(meterFWDependency == null ? null : resourceHelper.findFirmwareVersionByIdOrThrowException(meterFWDependency));
        firmwareVersion.setCommunicationFirmwareDependency(comFWDependency == null ? null : resourceHelper.findFirmwareVersionByIdOrThrowException(comFWDependency));
        firmwareVersion.setAuxiliaryFirmwareDependency(auxFWDependency == null ? null : resourceHelper.findFirmwareVersionByIdOrThrowException(auxFWDependency));

        if (!FirmwareStatus.FINAL.equals(firmwareVersion.getFirmwareStatus())
                || !firmwareService.isFirmwareVersionInUse(firmwareVersion.getId())) {
            firmwareVersion.setFirmwareVersion(fwVersion);
            parseFirmwareStatusField(status).ifPresent(firmwareVersion::setFirmwareStatus);
            byte[] firmwareFile = loadFirmwareFile(fileInputStream);
            resourceHelper.findSecurityAccessorForSignatureValidation(deviceTypeId)
                    .ifPresent(securityAccessor -> resourceHelper.checkFirmwareVersion(deviceType, securityAccessor, firmwareFile));
            setExpectedFirmwareSize(firmwareVersion, firmwareFile);
            firmwareVersion.update();
            setFirmwareFile(firmwareVersion, firmwareFile);
        } else {
            firmwareVersion.update();
        }

        return Response.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response updateStatusOfFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareVersionInfo info) {
        info.id = id;
        FirmwareVersion firmwareVersion = resourceHelper.lockFirmwareVersionOrThrowException(info);
        switch (info.firmwareStatus.id) {
            case DEPRECATED:
                firmwareVersion.deprecate();
                break;
            case FINAL:
                firmwareVersion.setFirmwareStatus(FirmwareStatus.FINAL);
                firmwareVersion.update();
                break;
            default:
        }
        return Response.ok().entity(versionFactory.fullInfo(firmwareVersion)).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public Response deleteFirmwareVersion(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareVersionInfo info) {
        info.id = id;
        if (info.firmwareStatus.id == FirmwareStatus.DEPRECATED) {
            FirmwareVersion firmwareVersion = resourceHelper.lockFirmwareVersionOrThrowException(info);
            firmwareVersion.delete();
            return Response.noContent().build();
        };
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private void checkIfEditableOrThrowException(FirmwareVersion firmwareVersion) {
        if (firmwareVersion.getFirmwareStatus().equals(FirmwareStatus.DEPRECATED)) {
            throw exceptionFactory.newException(MessageSeeds.VERSION_IS_DEPRECATED);
        }
    }

    private byte[] loadFirmwareFile(InputStream fileInputStream) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); InputStream fis = fileInputStream) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                out.write(buffer, 0, length);
                if (out.size() > FirmwareService.MAX_FIRMWARE_FILE_SIZE) {
                    throw exceptionFactory.newException(MessageSeeds.MAX_FILE_SIZE_EXCEEDED);
                }
            }
            return out.toByteArray();
        } catch (IOException ex) {
            throw exceptionFactory.newException(MessageSeeds.FILE_IO);
        }
    }

    private void setExpectedFirmwareSize(Object firmwareFileReceiver, byte[] firmwareFile) {
        if (firmwareFile.length > 0) {
            if (firmwareFileReceiver instanceof FirmwareVersion) {
                ((FirmwareVersion) firmwareFileReceiver).setExpectedFirmwareSize(firmwareFile.length);
            } else if (firmwareFileReceiver instanceof FirmwareVersionBuilder) {
                ((FirmwareVersionBuilder) firmwareFileReceiver).setExpectedFirmwareSize(firmwareFile.length);
            }
        }
    }

    private void setFirmwareFile(FirmwareVersion firmwareVersion, byte[] firmwareFile) {
        if (firmwareFile.length > 0) {
            firmwareVersion.setFirmwareFile(firmwareFile);
        }
    }

    private Optional<FirmwareStatus> parseFirmwareStatusField(String firmwareStatus) {
        if (firmwareStatus == null || firmwareStatus.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(FirmwareStatusFieldAdapter.INSTANCE.unmarshal(firmwareStatus));
    }

    private Optional<FirmwareType> parseFirmwareTypeField(String firmwareType) {
        if (firmwareType == null || firmwareType.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(FirmwareTypeInfo.FIRMWARE_TYPE_ADAPTER.unmarshal(firmwareType));
    }

    private FirmwareVersionBuilder getFirmwareVersionBuilder(DeviceType deviceType, String firmwareVersion, FirmwareStatus firmwareStatus, FirmwareType firmwareType, String imageIdentifier,
                                                             Long meterFWDependency, Long comFWDependency, Long auxFWDependency) {
        FirmwareVersionBuilder builder = firmwareService.newFirmwareVersion(deviceType, firmwareVersion, firmwareStatus, firmwareType,
                FirmwareType.CA_CONFIG_IMAGE.equals(firmwareType) ? firmwareVersion : imageIdentifier);
        if (meterFWDependency != null) {
            builder.setMeterFirmwareDependency(resourceHelper.findFirmwareVersionByIdOrThrowException(meterFWDependency));
        }
        if (comFWDependency != null) {
            builder.setCommunicationFirmwareDependency(resourceHelper.findFirmwareVersionByIdOrThrowException(comFWDependency));
        }
        if (auxFWDependency != null) {
            builder.setAuxiliaryFirmwareDependency(resourceHelper.findFirmwareVersionByIdOrThrowException(auxFWDependency));
        }
        return builder;
    }
}
