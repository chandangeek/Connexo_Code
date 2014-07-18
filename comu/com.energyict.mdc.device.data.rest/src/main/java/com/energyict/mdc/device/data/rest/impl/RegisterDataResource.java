package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class RegisterDataResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public RegisterDataResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register register = resourceHelper.findRegisterOrThrowException(device, registerId);
        List<Reading> readings = ListPager.of(register.getReadings(Interval.sinceEpoch()), new Comparator<Reading>() {
            @Override
            public int compare(Reading o1, Reading o2) {
                return o1.getTimeStamp().compareTo(o2.getTimeStamp());
            }
        }).from(queryParameters).find();
        List<ReadingInfo> readingInfos = ReadingInfoFactory.asInfoList(readings, register);
        return PagedInfoList.asJson("data", readingInfos, queryParameters);
    }

    @GET
    @Path("/{readingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingInfo getRegisterDataInfo(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("readingId") long readingId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Optional<Reading> reading = register.getReading(new Date(readingId));
        if(reading.isPresent()) {
            return ReadingInfoFactory.asInfo(reading.get(), register);
        }

        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_READING, registerId, readingId);
    }
}
