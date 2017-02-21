/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.NoMeterActivationAt;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.topology.TopologyService;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DeviceStatesRestricted(
        value = {DefaultState.DECOMMISSIONED},
        methods = {HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE},
        ignoredUserRoles = {Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
public class RegisterDataResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final TopologyService topologyService;

    @Inject
    public RegisterDataResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory, TopologyService topologyService) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.topologyService = topologyService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getRegisterData(
            @PathParam("name") String name,
            @PathParam("registerId") long registerId,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Register<?, ?> register = resourceHelper.findRegisterOrThrowException(device, registerId);

        Range<Instant> intervalReg = Range.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));

        List<Pair<Register, Range<Instant>>> registerTimeLine = topologyService.getDataLoggerRegisterTimeLine(register, intervalReg);

        List<ReadingInfo> readingInfos = registerTimeLine.stream().
                flatMap(registerRangePair -> {
                    Register<?, ?> register1 = registerRangePair.getFirst();
                    List<? extends Reading> readings = register1.getReadings(Interval.of(registerRangePair.getLast()));
                    List<ReadingInfo> infoList = deviceDataInfoFactory.asReadingsInfoList(readings, register1, device.forValidation()
                            .isValidationActive(register1, this.clock.instant()), register.equals(register1) ? null : register1.getDevice());
                    // sort the list of readings
                    Collections.sort(infoList, (ri1, ri2) -> ri2.timeStamp.compareTo(ri1.timeStamp));
                    return infoList.stream()
                            // filter the list of readings based on user parameters
                            .filter(resourceHelper.getSuspectsFilter(filter, this::hasSuspects));
                }).collect(Collectors.toList());


        List<ReadingInfo> paginatedReadingInfo = ListPager.of(readingInfos).from(queryParameters).find();
        return PagedInfoList.fromPagedList("data", paginatedReadingInfo, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public ReadingInfo getRegisterData(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("timeStamp") long timeStamp) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Register<?, ?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Reading reading = register.getReading(Instant.ofEpochMilli(timeStamp)).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_READING_ON_REGISTER, registerId, timeStamp));
        return deviceDataInfoFactory.createReadingInfo(
                reading,
                register,
                //TODO do we need to add the datalogger here?
                device.forValidation().isValidationActive(register, this.clock.instant()), null);
    }

    @PUT
    @Transactional
    @Path("/{timeStamp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response editRegisterData(@PathParam("name") String name, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Register<?, ?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        BaseReading reading = readingInfo.createNew(register);
        validateLinkedToSlave(register, reading.getTimeStamp());
        validateManualAddedEditValueForOverflow(register, reading);
        if (readingInfo instanceof NumericalReadingInfo && NumericalReadingInfo.class.cast(readingInfo).isConfirmed != null && NumericalReadingInfo.class.cast(readingInfo).isConfirmed) {
            register.startEditingData().confirmReading(reading, readingInfo.timeStamp).complete();
        } else {
            register.startEditingData().editReading(reading, readingInfo.timeStamp).complete();
        }
        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response addRegisterData(@PathParam("name") String name, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Register<?, ?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        if(readingInfo instanceof NumericalReadingInfo && ((NumericalReadingInfo) readingInfo).interval != null &&
                ((NumericalReadingInfo) readingInfo).interval.start > ((NumericalReadingInfo) readingInfo).interval.end){
            throw new LocalizedFieldValidationException(MessageSeeds.INTERVAL_END_BEFORE_START, "interval.end");
        }
        try {
            BaseReading reading = readingInfo.createNew(register);
            validateLinkedToSlave(register, reading.getTimeStamp());
            validateManualAddedEditValueForOverflow(register, reading);
            register.startEditingData().editReading(reading, readingInfo.timeStamp).complete();
        } catch (NoMeterActivationAt e) {
            Instant time = (Instant) e.get("time");
            throw this.exceptionFactory.newExceptionSupplier(MessageSeeds.CANT_ADD_READINGS_FOR_STATE, Date.from(time)).get();
        }
        return Response.status(Response.Status.OK).build();
    }

    private void validateLinkedToSlave(Register<?, ?> register, Instant readingTimeStamp) {
        Optional<Register> slaveRegister = topologyService.getSlaveRegister(register, readingTimeStamp);
        if (slaveRegister.isPresent()) {
            throw this.exceptionFactory.newException(MessageSeeds.CANNOT_ADDEDITREMOVE_REGISTER_VALUE_WHEN_LINKED_TO_SLAVE);
        }
    }

    private void validateManualAddedEditValueForOverflow(Register<?, ?> register, BaseReading reading) {
        if (register instanceof NumericalRegister && (!((NumericalRegister) register).getRegisterSpec().isUseMultiplier() || !register.getDevice()
                .getMultiplierAt(reading.getTimeStamp())
                .isPresent())) {
            Optional<BigDecimal> optionalOverflow = ((NumericalRegister) register).getRegisterSpec().getOverflowValue();
            if (optionalOverflow.isPresent() && optionalOverflow.get().compareTo(reading.getValue()) < 0) {
                throw this.exceptionFactory.newExceptionSupplier(MessageSeeds.VALUE_MAY_NOT_EXCEED_OVERFLOW_VALUE, reading.getValue(), optionalOverflow.get()).get();
            }
        }
    }

    @DELETE
    @Transactional
    @Path("/{timeStampId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response deleteRegisterData(@PathParam("name") String name, @PathParam("registerId") long registerId, @PathParam("timeStampId") long timeStamp, @BeanParam JsonQueryParameters queryParameters, ReadingInfo info) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Register<?, ?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        try {
            Instant removalDate = info.timeStamp;
            validateLinkedToSlave(register, removalDate);
            register.startEditingData().removeReading(removalDate).complete();
        } catch (IllegalArgumentException e) {
            throw this.exceptionFactory.newExceptionSupplier(MessageSeeds.NO_CHANNELS_ON_REGISTER, register.getReadingType().getName()).get();
        }
        return Response.status(Response.Status.OK).build();
    }

    private boolean hasSuspects(ReadingInfo info) {
        boolean result = true;
        if (info instanceof NumericalReadingInfo) {
            NumericalReadingInfo numericalReadingInfo = (NumericalReadingInfo) info;
            result = ValidationStatus.SUSPECT.equals(numericalReadingInfo.validationResult);
        }
        return result;
    }
}
