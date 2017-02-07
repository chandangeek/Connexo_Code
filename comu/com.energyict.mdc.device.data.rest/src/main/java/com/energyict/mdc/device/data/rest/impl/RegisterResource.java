package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.BillingRegister;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;

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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RegisterResource extends AbstractRegisterResource{

    private final ExceptionFactory exceptionFactory;
    private final ResourceHelper resourceHelper;
    private final Provider<RegisterDataResource> registerDataResourceProvider;
    private final ValidationInfoHelper validationInfoHelper;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final TopologyService topologyService;
    private final MasterDataService masterDataService;
    private final Clock clock;

    @Inject
    public RegisterResource(ExceptionFactory exceptionFactory, ResourceHelper resourceHelper, Provider<RegisterDataResource> registerDataResourceProvider, ValidationInfoHelper validationInfoHelper, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory, TopologyService topologyService, MasterDataService masterDataService) {
        super(clock);
        this.exceptionFactory = exceptionFactory;
        this.resourceHelper = resourceHelper;
        this.registerDataResourceProvider = registerDataResourceProvider;
        this.clock = clock;
        this.validationInfoHelper = validationInfoHelper;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.topologyService = topologyService;
        this.masterDataService = masterDataService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getRegisters(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter jsonQueryFilter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        final List<ReadingType> filteredReadingTypes = getElegibleReadingTypes(jsonQueryFilter, device);
        List<RegisterInfo> registerInfos = ListPager.of(device.getRegisters(), this::compareRegisters).from(queryParameters).stream()
                .filter(register -> filteredReadingTypes.size() == 0 || filteredReadingTypes.contains(register.getReadingType()))
                .map(r -> deviceDataInfoFactory.createRegisterInfo(r, validationInfoHelper.getMinimalRegisterValidationInfo(r), topologyService)).collect(Collectors.toList());
        Collections.sort(registerInfos, this::compareRegisterInfos);
        return PagedInfoList.fromPagedList("data", registerInfos, queryParameters);
    }

    private int compareRegisterInfos(RegisterInfo ri1, RegisterInfo ri2) {
        return ri1.readingType.fullAliasName.compareTo(ri2.readingType.fullAliasName);
    }

    private List<ReadingType> getElegibleReadingTypes(@BeanParam JsonQueryFilter jsonQueryFilter, Device device) {
        List<Register> registers = device.getRegisters()
                .stream()
                .filter(register -> !(jsonQueryFilter.hasProperty("toTimeStart") || jsonQueryFilter.hasProperty("toTimeEnd")) || register instanceof BillingRegister)
                .collect(Collectors.toList());
        if (jsonQueryFilter.hasProperty("registers")) {
            List<Long> registerTypes = jsonQueryFilter.getLongList("registers").stream()
                    .collect(Collectors.toList());
            registers = registers
                    .stream()
                    .filter(register -> registerTypes.contains(register.getRegisterSpecId()))
                    .collect(Collectors.toList());
        } else if (jsonQueryFilter.hasProperty("groups")) {
            final List<Long> finalGroups = jsonQueryFilter.getLongList("groups").stream()
                    .collect(Collectors.toList());
            List<ReadingType> allowedReadingTypes = masterDataService.findAllRegisterGroups().find().stream()
                    .filter(registerGroup -> finalGroups.contains(registerGroup.getId()))
                    .flatMap(registerGroup -> registerGroup.getRegisterTypes().stream())
                    .map(MeasurementType::getReadingType)
                    .distinct()
                    .collect(Collectors.toList());

            registers = registers
                    .stream()
                    .filter(register -> allowedReadingTypes.contains(register.getReadingType()))
                    .collect(Collectors.toList());
        }
        return registers.stream().map(Register::getReadingType).collect(Collectors.toList());
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
    public RegisterInfo getRegister(@PathParam("name") String name, @PathParam("registerId") long registerId) {
        Register<?, ?> register = doGetRegister(name, registerId);
        return deviceDataInfoFactory.createRegisterInfo(register, validationInfoHelper.getRegisterValidationInfo(register), topologyService);
    }

    @PUT
    @Transactional
    @Path("/{registerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE})
    public Response updateRegister(@PathParam("name") String name, @PathParam("registerId") long registerId, RegisterInfo registerInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Register<?, ?> register = doGetRegister(name, registerId);
        Register.RegisterUpdater registerUpdater = device.getRegisterUpdaterFor(register);
        if (register.getRegisterSpec() instanceof NumericalRegisterSpec) {
            NumericalRegisterInfo numericalRegisterInfo = ((NumericalRegisterInfo) registerInfo);
            registerUpdater.setNumberOfFractionDigits(numericalRegisterInfo.overruledNumberOfFractionDigits);
            registerUpdater.setOverflowValue(numericalRegisterInfo.overruledOverflow);
        }
        registerUpdater.setObisCode(registerInfo.overruledObisCode);
        registerUpdater.update();
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/registergroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getRegisterGroups(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<Register> registers = device.getRegisters();
        List<RegisterGroup> allRegisterGroups = masterDataService.findAllRegisterGroups().find();
        List<IdWithNameInfo> filteredRegisterGroups = allRegisterGroups.stream()
                .filter(registerGroup -> registerGroupContainsAtLeastOneReadingType(registerGroup.getRegisterTypes(), registers))
                .map(registerGroup -> new IdWithNameInfo(registerGroup.getId(), registerGroup.getName()))
                .collect(Collectors.toList());

        return Response.ok(filteredRegisterGroups).build();
    }


    @GET
    @Transactional
    @Path("/registersforgroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getRegistersForGroups(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter jsonQueryFilter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        final List<ReadingType> filteredReadingTypes = getElegibleReadingTypes(jsonQueryFilter, device);
        List<SummarizedRegisterInfo> registerInfos = ListPager.of(device.getRegisters(), this::compareRegisters).from(queryParameters).stream()
                .filter(register -> filteredReadingTypes.size() == 0 || filteredReadingTypes.contains(register.getReadingType()))
                .map(register -> new SummarizedRegisterInfo(register.getRegisterSpecId(),
                        register.getReadingType().getFullAliasName(),
                        register instanceof BillingRegister,
                        register.hasEventDate(),
                        register.getReadingType().isCumulative(),
                        register instanceof NumericalRegister && ((NumericalRegister) register).getRegisterSpec()
                                .isUseMultiplier()))
                .collect(Collectors.toList());
        Collections.sort(registerInfos, this::compareSummarizedRegisterInfos);
        return Response.ok(registerInfos).build();
    }

    private int compareSummarizedRegisterInfos(SummarizedRegisterInfo ri1, SummarizedRegisterInfo ri2) {
        return ri1.name.compareTo(ri2.name);
    }

    private boolean registerGroupContainsAtLeastOneReadingType(List<RegisterType> registerTypes, List<Register> registers) {
        List<ReadingType> readingTypesInGroup = registerTypes.stream().map(RegisterType::getReadingType)
                .collect(Collectors.toList());
        List<ReadingType> readingTypesOnDevice = registers.stream()
                .map(Register::getReadingType)
                .collect(Collectors.toList());

        return !Collections.disjoint(readingTypesInGroup, readingTypesOnDevice);
    }

    @GET
    @Transactional
    @Path("/registerreadings")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public PagedInfoList getRegisterReadings(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter jsonQueryFilter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        final List<ReadingType> filteredReadingTypes = getElegibleReadingTypes(jsonQueryFilter, device);
        List<Register> registers = device.getRegisters().stream()
                .filter(register -> filteredReadingTypes.contains(register.getReadingType()))
                .collect(Collectors.toList());

        Instant measurementTimeStart = jsonQueryFilter.getInstant("measurementTimeStart") == null ? Instant.EPOCH : jsonQueryFilter.getInstant("measurementTimeStart");
        Instant measurementTimeEnd = jsonQueryFilter.getInstant("measurementTimeEnd") == null ? null : jsonQueryFilter.getInstant("measurementTimeEnd");
        boolean toTimeFilterAvailable = jsonQueryFilter.getInstant("toTimeStart") != null || jsonQueryFilter.getInstant("toTimeEnd") != null;
        Instant toTimeStart = jsonQueryFilter.getInstant("toTimeStart") == null ? Instant.EPOCH : jsonQueryFilter.getInstant("toTimeStart");
        Instant toTimeEnd = jsonQueryFilter.getInstant("toTimeEnd") == null ? null : jsonQueryFilter.getInstant("toTimeEnd");

        Range<Instant> intervalReg = measurementTimeEnd==null ? Range.atLeast(measurementTimeStart) : Range.openClosed(measurementTimeStart, measurementTimeEnd);
        Range<Instant> toTimeRange = toTimeEnd==null ? Range.atLeast(toTimeStart) : Range.openClosed(toTimeStart, toTimeEnd);

        List<ReadingInfo> readingInfos = registers.stream()
                .map(register -> topologyService.getDataLoggerRegisterTimeLine(register, intervalReg))
                .flatMap(Collection::stream)
                .map(registerRangePair -> {
                    Register<?, ?> register1 = registerRangePair.getFirst();
                    List<? extends Reading> readings = register1.getReadings(Interval.of(registerRangePair.getLast()))
                            .stream()
                            .filter(reading -> {
                                if (toTimeFilterAvailable && !(register1 instanceof NumericalReading)) {
                                     return false;
                                }
                                if (!toTimeFilterAvailable || !(register1 instanceof NumericalReading)) {
                                    return true;
                                }
                                NumericalReading reading1 = (NumericalReading) reading;
                                return reading1.getRange().isPresent() && toTimeRange.contains(reading1.getRange().get().upperEndpoint());
                            })
                            .collect(Collectors.toList());
                    List<ReadingInfo> infoList = deviceDataInfoFactory.asReadingsInfoList(readings, register1, device.forValidation()
                            .isValidationActive(register1, this.clock.instant()), registers.contains(register1) ? null : register1.getDevice());
                    infoList.stream().forEach(readingInfo -> readingInfo.register = new IdWithNameInfo(register1.getRegisterSpecId(), register1.getReadingType().getFullAliasName()));
                    Collections.sort(infoList, (ri1, ri2) -> ri2.timeStamp.compareTo(ri1.timeStamp));
                    addDeltaCalculationIfApplicableAndUpdateInterval(register1, infoList);
                    return infoList;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Collections.sort(readingInfos, this::compareReadingInfos);

        List<ReadingInfo> paginatedReadingInfo = ListPager.of(readingInfos).from(queryParameters).find();
        return PagedInfoList.fromPagedList("data", paginatedReadingInfo, queryParameters);
    }

    private int compareReadingInfos(ReadingInfo ri1, ReadingInfo ri2) {
        int result = ri2.timeStamp.compareTo(ri1.timeStamp);
        return result != 0 ? result : ri1.register.name.compareTo(ri2.register.name);
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public PagedInfoList getDeviceCustomProperties(@PathParam("name") String name, @PathParam("registerId") long registerId, @BeanParam JsonQueryParameters queryParameters) {
        Register<?, ?> register = doGetRegister(name, registerId);
        CustomPropertySetInfo customPropertySetInfo = resourceHelper.getRegisterCustomPropertySetInfo(register, this.clock.instant());
        return PagedInfoList.fromCompleteList("customproperties", customPropertySetInfo != null ? Collections.singletonList(customPropertySetInfo) : new ArrayList<>(), queryParameters);
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public CustomPropertySetInfo getDeviceCustomProperties(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId) {
        Register<?, ?> register = doGetRegister(name, registerId);
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
    public CustomPropertySetInfo getRegisterCustomProperties(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") Long timeStamp) {
        Register<?, ?> register = doGetRegister(name, registerId);
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
    public PagedInfoList getRegisterCustomPropertiesHistory(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @BeanParam JsonQueryParameters queryParameters) {
        Register<?, ?> register = doGetRegister(name, registerId);
        return PagedInfoList.fromCompleteList("versions", resourceHelper.getVersionedCustomPropertySetHistoryInfos(register, cpsId), queryParameters);
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/currentinterval")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public IntervalInfo getCurrentTimeInterval(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId) {
        Register<?, ?> register = doGetRegister(name, registerId);
        Interval interval = Interval.of(resourceHelper.getCurrentTimeInterval(register, cpsId));

        return IntervalInfo.from(interval.toClosedOpenRange());
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/conflicts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Register<?, ?> register = doGetRegister(name, registerId);
        List<CustomPropertySetIntervalConflictInfo> overlapInfos = resourceHelper.getOverlapsWhenCreate(register, cpsId, resourceHelper.getTimeRange(startTime, endTime));
        Collections.sort(overlapInfos, resourceHelper.getConflictInfosComparator());
        return PagedInfoList.fromCompleteList("conflicts", overlapInfos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{registerId}/customproperties/{cpsId}/conflicts/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getOverlaps(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("startTime") long startTime, @QueryParam("endTime") long endTime, @BeanParam JsonQueryParameters queryParameters) {
        Register<?, ?> register = doGetRegister(name, registerId);
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
    public Response changeRegisterCustomProperty(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, CustomPropertySetInfo customPropertySetInfo) {
        Register<?, ?> register = doGetRegister(name, registerId);
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
    public Response addRegisterCustomAttributeVersioned(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Register<?, ?> register = doGetRegister(name, registerId);
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
    public Response editRegisterCustomAttributeVersioned(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("cpsId") long cpsId, @PathParam("timeStamp") long timeStamp, @QueryParam("forced") boolean forced, CustomPropertySetInfo customPropertySetInfo) {
        Register<?, ?> register = doGetRegister(name, registerId);
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
    public Response validateNow(@PathParam("name") String name, @PathParam("registerId") long registerId, RegisterTriggerValidationInfo validationInfo) {
        Device device = resourceHelper.lockDeviceOrThrowException(validationInfo);
        Register<?, ?> register = doGetRegister(name, registerId);
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

    private Register<?, ?> doGetRegister(String deviceName, long registerId) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
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
    public Response getValidationFeatureStatus(@PathParam("name") String name, @PathParam("registerId") long registerId) {
        Register<?, ?> register = doGetRegister(name, registerId);
        ValidationStatusInfo validationStatusInfo = determineStatus(register);
        return Response.status(Response.Status.OK).entity(validationStatusInfo).build();
    }

    @Path("{registerId}/validationpreview")
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationStatusPreview(@PathParam("name") String name, @PathParam("registerId") long registerId) {
        Register<?, ?> register = doGetRegister(name, registerId);
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
    public RegisterHistoryInfos getDataLoggerSlaveRegisterHistory(@PathParam("name") String name, @PathParam("registerId") long registerId) {
        Register register = resourceHelper.findRegisterOnDeviceOrThrowException(name, registerId);
        return RegisterHistoryInfos.from(topologyService.findDataLoggerChannelUsagesForRegisters(register, Range.atMost(clock.instant())));
    }
}