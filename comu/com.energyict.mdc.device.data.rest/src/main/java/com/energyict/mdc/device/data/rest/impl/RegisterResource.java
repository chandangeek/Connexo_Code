package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.topology.TopologyService;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RegisterResource {

    private final ExceptionFactory exceptionFactory;
    private final ResourceHelper resourceHelper;
    private final Provider<RegisterDataResource> registerDataResourceProvider;
    private final ValidationInfoHelper validationInfoHelper;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final TopologyService topologyService;
    private final Clock clock;

    @Inject
    public RegisterResource(ExceptionFactory exceptionFactory, ResourceHelper resourceHelper, Provider<RegisterDataResource> registerDataResourceProvider, ValidationInfoHelper validationInfoHelper, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory, TopologyService topologyService) {
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
        this.registerDataResourceProvider = registerDataResourceProvider;
        this.clock = clock;
        this.validationInfoHelper = validationInfoHelper;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.topologyService = topologyService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getRegisters(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<RegisterInfo> registerInfos = ListPager.of(device.getRegisters(), this::compareRegisters).from(queryParameters).stream()
                .map(r -> deviceDataInfoFactory.createRegisterInfo(r, validationInfoHelper.getMinimalRegisterValidationInfo(r), topologyService)).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", registerInfos, queryParameters);
    }

    private int compareRegisters(Register r1, Register r2) {
        ReadingType readingType1 = r1.getRegisterSpec().getRegisterType().getReadingType();
        ReadingType readingType2 = r2.getRegisterSpec().getRegisterType().getReadingType();
        return readingType1.getAliasName().compareToIgnoreCase(readingType2.getAliasName());
    }

    @GET
    @Transactional
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public RegisterInfo getRegister(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        return deviceDataInfoFactory.createRegisterInfo(register, validationInfoHelper.getRegisterValidationInfo(register), topologyService);
    }

    @PUT
    @Transactional
    @Path("/{registerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE})
    public Response updateRegister(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, RegisterInfo registerInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?, ?> register = doGetRegister(mRID, registerId);
        Register.RegisterUpdater registerUpdater = device.getRegisterUpdaterFor(register);
        if (register.getRegisterSpec() instanceof NumericalRegisterSpec) {
            NumericalRegister numericalRegister = (NumericalRegister) register;
            NumericalRegisterInfo numericalRegisterInfo = ((NumericalRegisterInfo) registerInfo);
            if (!Objects.equals(numericalRegister.getNumberOfFractionDigits(), numericalRegisterInfo.overruledNumberOfFractionDigits)) {
                registerUpdater.setNumberOfFractionDigits(numericalRegisterInfo.overruledNumberOfFractionDigits);
            }
            if (numericalRegister.getOverflow().isPresent() && (numericalRegisterInfo.overruledOverflow == null)
                    || !Objects.equals(numericalRegisterInfo.overruledOverflow, numericalRegister.getOverflow().get())) {
                registerUpdater.setOverflowValue(numericalRegisterInfo.overruledOverflow);
            }
        }
        if (!register.getDeviceObisCode().equals(registerInfo.overruledObisCode)) {
            registerUpdater.setObisCode(registerInfo.overruledObisCode);
        }
        registerUpdater.update();
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public PagedInfoList getDeviceCustomProperties(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam JsonQueryParameters queryParameters) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        CustomPropertySetInfo customPropertySetInfo = resourceHelper.getRegisterCustomPropertySetInfo(register, this.clock.instant());
        return PagedInfoList.fromCompleteList("customproperties", customPropertySetInfo != null ? Collections.singletonList(customPropertySetInfo) : new ArrayList<>(), queryParameters);
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public CustomPropertySetInfo getDeviceCustomProperties(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        CustomPropertySetInfo customPropertySetInfo = resourceHelper.getRegisterCustomPropertySetInfo(register, this.clock.instant());
        if (customPropertySetInfo.id != cpsId) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        return customPropertySetInfo;
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/versions/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public CustomPropertySetInfo getRegisterCustomProperties(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") Long timeStamp) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        CustomPropertySetInfo customPropertySetInfo = resourceHelper.getRegisterCustomPropertySetInfo(register, Instant.ofEpochMilli(timeStamp));
        if (customPropertySetInfo.id != cpsId) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOMPROPERTYSET, cpsId);
        }
        return customPropertySetInfo;
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getRegisterCustomPropertiesHistory(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @BeanParam JsonQueryParameters queryParameters) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        return PagedInfoList.fromCompleteList("versions", resourceHelper.getVersionedCustomPropertySetHistoryInfos(register, cpsId), queryParameters);
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/currentinterval")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public IntervalInfo getCurrentTimeInterval(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        Interval interval = Interval.of(resourceHelper.getCurrentTimeInterval(register, cpsId));

        return IntervalInfo.from(interval.toClosedOpenRange());
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/conflicts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenCreate(register, cpsId, resourceHelper.getTimeRange(startTime, endTime));
        Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/conflicts/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenUpdate(register, cpsId, resourceHelper.getTimeRange(startTime, endTime), Instant.ofEpochMilli(timeStamp));
        Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response changeRegisterCustomProperty(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, CustomPropertySetInfo customPropertySetInfo) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        resourceHelper.lockRegisterTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        resourceHelper.lockRegisterSpecOrThrowException(customPropertySetInfo.parent, customPropertySetInfo.version, register);
        resourceHelper.setRegisterCustomPropertySet(register, customPropertySetInfo);
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/versions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response addRegisterCustomAttributeVersioned(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        resourceHelper.lockRegisterTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        resourceHelper.lockRegisterSpecOrThrowException(customPropertySetInfo.parent, customPropertySetInfo.version, register);
        Optional<IntervalErrorInfos> intervalErrors = resourceHelper.verifyTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime);
        if (intervalErrors.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(intervalErrors.get()).build();
        }
        List<CustomPropertySetIntervalConflictInfo> overlapInfos =
                resourceHelper.getOverlapsWhenCreate(register, cpsId, resourceHelper.getTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime))
                        .stream()
                        .filter(e -> !e.conflictType.equals(ValuesRangeConflictType.RANGE_INSERTED.name()))
                        .filter(resourceHelper.filterGaps(forced))
                        .collect(Collectors.toList());
        if (!overlapInfos.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CustomPropertySetIntervalConflictErrorInfo(overlapInfos.stream().collect(Collectors.toList())))
                    .build();
        }
        resourceHelper.addRegisterCustomPropertySetVersioned(register, cpsId, customPropertySetInfo);
        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/versions/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response editRegisterCustomAttributeVersioned(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Register<?, ?> register = doGetRegister(mRID, registerId);
        resourceHelper.lockRegisterTypeOrThrowException(customPropertySetInfo.objectTypeId, customPropertySetInfo.objectTypeVersion);
        resourceHelper.lockRegisterSpecOrThrowException(customPropertySetInfo.parent, customPropertySetInfo.version, register);
        Optional<IntervalErrorInfos> intervalErrors = resourceHelper.verifyTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime);
        if (intervalErrors.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(intervalErrors.get()).build();
        }
        List<CustomPropertySetIntervalConflictInfo> overlapInfos =
                resourceHelper.getOverlapsWhenUpdate(register, cpsId, resourceHelper.getTimeRange(customPropertySetInfo.startTime, customPropertySetInfo.endTime), Instant.ofEpochMilli(timeStamp))
                        .stream()
                        .filter(e -> !e.conflictType.equals(ValuesRangeConflictType.RANGE_INSERTED.name()))
                        .filter(resourceHelper.filterGaps(forced))
                        .collect(Collectors.toList());
        if (!overlapInfos.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CustomPropertySetIntervalConflictErrorInfo(overlapInfos.stream().collect(Collectors.toList())))
                    .build();
        }
        resourceHelper.setRegisterCustomPropertySetVersioned(register, cpsId, customPropertySetInfo, Instant.ofEpochMilli(timeStamp));
        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Path("/{registerId}/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response validateNow(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, RegisterTriggerValidationInfo validationInfo) {
        Device device = resourceHelper.lockDeviceOrThrowException(validationInfo);
        Register<?, ?> register = doGetRegister(mRID, registerId);
        if (validationInfo.lastChecked == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.NULL_DATE, "lastChecked");
        }
        Instant newDate = Instant.ofEpochMilli(validationInfo.lastChecked);
        Optional<Instant> lastChecked = register.getDevice().forValidation().getLastChecked(register);
        if (lastChecked.isPresent() && newDate.isAfter(lastChecked.get())) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_DATE, "lastChecked", lastChecked);
        }
        validateRegister(register, newDate);
        device.save();
        return Response.status(Response.Status.OK).build();
    }

    private void validateRegister(Register<?, ?> register, Instant start) {
        if (start != null) {
            register.getDevice().forValidation().setLastChecked(register, start);
        }
        register.getDevice().forValidation().validateRegister(register);
    }

    private Register<?, ?> doGetRegister(String mRID, long registerId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        return resourceHelper.findRegisterOrThrowException(device, registerId);
    }

    @Path("/{registerId}/data")
    public RegisterDataResource getRegisterDataResource() {
        return registerDataResourceProvider.get();
    }

    @Path("{registerId}/validationstatus")
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("registerId") long registerId) {
        Register<?, ?> register = doGetRegister(mrid, registerId);
        ValidationStatusInfo validationStatusInfo = determineStatus(register);
        return Response.status(Response.Status.OK).entity(validationStatusInfo).build();
    }

    @Path("{registerId}/validationpreview")
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationStatusPreview(@PathParam("mRID") String mrid, @PathParam("registerId") long registerId) {
        Register<?, ?> register = doGetRegister(mrid, registerId);
        DetailedValidationInfo detailedValidationInfo = validationInfoHelper.getRegisterValidationInfo(register);
        return Response.status(Response.Status.OK).entity(detailedValidationInfo).build();
    }

    private ValidationStatusInfo determineStatus(Register<?, ?> register) {
        return new ValidationStatusInfo(isValidationActive(register), register.getDevice().forValidation().getLastChecked(register), hasData(register));
    }

    private boolean isValidationActive(Register<?, ?> register) {
        return register.getDevice().forValidation().isValidationActive(register, clock.instant());
    }

    private boolean hasData(Register<?, ?> register) {
        return register.hasData();
    }

    @GET
    @Transactional
    @Path("/{registerId}/history")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public RegisterHistoryInfos getDataLoggerSlaveRegisterHistory(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId) {
        Register register = resourceHelper.findRegisterOnDeviceOrThrowException(mRID, registerId);
        return RegisterHistoryInfos.from(topologyService.findDataLoggerChannelUsagesForRegisters(register, Range.atMost(clock.instant())));
    }
}