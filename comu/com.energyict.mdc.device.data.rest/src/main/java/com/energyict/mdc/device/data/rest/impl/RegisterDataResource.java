package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
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
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.security.Privileges;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.isNull;

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
    public PagedInfoList getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        List<Reading> readings = register.getReadings(Interval.sinceEpoch());
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
        readingInfos = filter(readingInfos, uriInfo.getQueryParameters());

        List<ReadingInfo> paginatedReadingInfo = ListPager.of(readingInfos, ((ri1, ri2) -> ri1.timeStamp.compareTo(ri2.timeStamp))).from(queryParameters).find();
        return PagedInfoList.asJson("data", paginatedReadingInfo, queryParameters);
    }

    private boolean hasSuspects(ReadingInfo info) {
        boolean result = true;
        if(info.reading instanceof BillingReading) {
            BillingReadingInfo billingReadingInfo = BillingReadingInfo.class.cast(info);
            result = ValidationStatus.SUSPECT.equals(billingReadingInfo.validationResult);
        } else if (info.reading instanceof NumericalReading) {
            NumericalReadingInfo numericalReadingInfo = NumericalReadingInfo.class.cast(info);
            result = ValidationStatus.SUSPECT.equals(numericalReadingInfo.validationResult);
        }
        return result;
    }

    private boolean hideSuspects(ReadingInfo info) {
        return !hasSuspects(info);
    }

    private List<ReadingInfo> filter(List<ReadingInfo> infos, MultivaluedMap<String, String> queryParameters) {
        Predicate<ReadingInfo> fromParams = getFilter(queryParameters);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private Predicate<ReadingInfo> getFilter(MultivaluedMap<String, String> queryParameters) {
        ImmutableList.Builder<Predicate<ReadingInfo>> list = ImmutableList.builder();
        if (filterActive(queryParameters, "onlySuspect")) {
            list.add(this::hasSuspects);
        }
        if (filterActive(queryParameters, "hideSuspects")) {
            list.add(this::hideSuspects);
        }
        return lpi -> list.build().stream().allMatch(p -> p.test(lpi));
    }

    private boolean filterActive(MultivaluedMap<String, String> queryParameters, String key) {
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }
}
