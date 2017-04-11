/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.rest.DeviceStagesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.topology.TopologyService;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@DeviceStagesRestricted(
        value = {EndDeviceStage.POST_OPERATIONAL},
        methods = {HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE},
        ignoredUserRoles = {Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
public class RegisterHistoryDataResource {

    private final ResourceHelper resourceHelper;
    private final Clock clock;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final TopologyService topologyService;

    @Inject
    public RegisterHistoryDataResource(ResourceHelper resourceHelper, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory, TopologyService topologyService) {
        this.resourceHelper = resourceHelper;
        this.clock = clock;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.topologyService = topologyService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getRegisterData(
            @PathParam("name") String name,
            @PathParam("registerId") long registerId,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Register<?, ?> register = resourceHelper.findRegisterOrThrowException(device, registerId);

        Range<Instant> intervalReg = Range.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));
        boolean changedDataOnly = filter.getString("changedDataOnly") != null && (filter.getString("changedDataOnly").compareToIgnoreCase("yes") == 0);
        List<Pair<Register, Range<Instant>>> registerTimeLine = topologyService.getDataLoggerRegisterTimeLine(register, intervalReg);

        List<ReadingInfo> readingHistoryInfos = registerTimeLine.stream().
                flatMap(registerRangePair -> {
                    Register<?, ?> register1 = registerRangePair.getFirst();
                    List<? extends Reading> readings = register1.getHistoryReadings(Interval.of(registerRangePair.getLast()), changedDataOnly);

                    List<ReadingInfo> infoList = deviceDataInfoFactory.asReadingsInfoList(readings, register1, device.forValidation()
                            .isValidationActive(register1, this.clock.instant()), register.equals(register1) ? null : register1.getDevice());

                    // change id value
                    AtomicInteger counter = new AtomicInteger(1);
                    infoList.stream().forEach(r -> r.id = String.valueOf(counter.getAndIncrement()));

                    // sort the list of readings
                    Collections.sort(infoList, (ri1, ri2) -> ri2.timeStamp.compareTo(ri1.timeStamp));
                    return infoList.stream();
                }).collect(Collectors.toList());

        return Response.ok(readingHistoryInfos).build();
    }
}
