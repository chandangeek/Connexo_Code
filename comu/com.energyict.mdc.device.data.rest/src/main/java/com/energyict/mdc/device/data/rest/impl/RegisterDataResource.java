package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;
import com.google.common.base.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterDataResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final ValidationService validationService;
    private final ValidationInfoHelper validationInfoHelper;

    @Inject
    public RegisterDataResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, ValidationService validationService, ValidationInfoHelper validationInfoHelper) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.validationService = validationService;
        this.validationInfoHelper = validationInfoHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public PagedInfoList getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        List<Reading> readings = ListPager.of(register.getReadings(Interval.sinceEpoch()), new Comparator<Reading>() {
            @Override
            public int compare(Reading o1, Reading o2) {
                return o2.getTimeStamp().compareTo(o1.getTimeStamp());
            }
        }).from(queryParameters).find();
        List<ReadingRecord> readingRecords = readings.stream().map(r -> r.getActualReading()).collect(Collectors.toList());
        Optional<Channel> channelRef = resourceHelper.getRegisterChannel(register, meter);
        List<DataValidationStatus> dataValidationStatuses = new ArrayList<>();
        Boolean validationStatusForRegister = false;
        if(channelRef.isPresent()) {
            validationStatusForRegister = validationInfoHelper.getValidationStatus(channelRef, meter);
            dataValidationStatuses = validationService.getEvaluator().getValidationStatus(channelRef.get(), readingRecords);
        }
        List<ReadingInfo> readingInfos = ReadingInfoFactory.asInfoList(readings, register.getRegisterSpec(),
                validationStatusForRegister, dataValidationStatuses, validationService.getEvaluator());
        return PagedInfoList.asJson("data", readingInfos, queryParameters);
    }
}
