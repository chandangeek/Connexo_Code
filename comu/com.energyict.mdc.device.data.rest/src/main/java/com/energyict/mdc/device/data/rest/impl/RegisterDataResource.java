package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

public class RegisterDataResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;

    @Inject
    public RegisterDataResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        Interval interval = Interval.sinceEpoch();
        Range<Instant> range = interval.toOpenClosedRange();
        List<? extends Reading> readings = register.getReadings(interval);
        List<ReadingRecord> readingRecords = readings.stream().map(Reading::getActualReading).collect(Collectors.toList());
        Optional<Channel> channelRef = resourceHelper.getRegisterChannel(register, meter);
        List<DataValidationStatus> dataValidationStatuses = new ArrayList<>();
        Boolean validationStatusForRegister = false;
        if(channelRef.isPresent()) {
            validationStatusForRegister = device.forValidation().isValidationActive(register, clock.instant());
            dataValidationStatuses = device.forValidation().getValidationStatus(register, readingRecords, range);
        }
        List<ReadingInfo> readingInfos = ReadingInfoFactory.asInfoList(readings, register.getRegisterSpec(),
                validationStatusForRegister, dataValidationStatuses);
        // sort the list of readings
        Collections.sort(readingInfos, (ri1, ri2) -> ri2.timeStamp.compareTo(ri1.timeStamp));
        /* And fill a delta value for cumulative reading type. The delta is the difference with the previous record.
           The Delta value won't be stored in the database yet, as it has a performance impact */
        if (register.getReadingType().isCumulative()){
            for (int i = 0; i < readingInfos.size() - 1; i++){
                calculateDeltaForNumericalReading(readingInfos.get(i + 1), readingInfos.get(i));
                calculateDeltaForBillingReading(readingInfos.get(i + 1), readingInfos.get(i));
            }
        }
        // filter the list of readings based on user parameters
        readingInfos = filter(readingInfos, uriInfo.getQueryParameters());
        List<ReadingInfo> paginatedReadingInfo = ListPager.of(readingInfos).from(queryParameters).find();
        return PagedInfoList.fromPagedList("data", paginatedReadingInfo, queryParameters);
    }

    private void calculateDeltaForNumericalReading(ReadingInfo previous, ReadingInfo current){
        if (previous != null && current != null && current instanceof NumericalReadingInfo){
            NumericalReadingInfo prevCasted = (NumericalReadingInfo) previous;
            NumericalReadingInfo curCasted = (NumericalReadingInfo) current;
            if (prevCasted.value != null && curCasted.value != null) {
                curCasted.deltaValue = curCasted.value.subtract(prevCasted.value);
                curCasted.deltaValue = curCasted.deltaValue.setScale(curCasted.value.scale(), BigDecimal.ROUND_UP);
            }
        }
    }

    private void calculateDeltaForBillingReading(ReadingInfo previous, ReadingInfo current){
        if (previous != null && current != null && current instanceof BillingReadingInfo){
            BillingReadingInfo prevCasted = (BillingReadingInfo) previous;
            BillingReadingInfo curCasted = (BillingReadingInfo) current;
            if (prevCasted.value != null && curCasted.value != null) {
                curCasted.deltaValue = curCasted.value.subtract(prevCasted.value);
            }
        }
    }
    @GET
    @Path("/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public ReadingInfo getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("timeStamp") long timeStamp) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        Reading reading = register.getReading(Instant.ofEpochMilli(timeStamp)).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_READING_ON_REGISTER, registerId, timeStamp));
        List<ReadingRecord> readingRecords = new ArrayList<>(Arrays.asList(reading.getActualReading()));
        Optional<Channel> channelRef = resourceHelper.getRegisterChannel(register, meter);
        List<DataValidationStatus> dataValidationStatuses = new ArrayList<>();
        Boolean validationStatusForRegister = false;
        if(channelRef.isPresent()) {
            validationStatusForRegister = device.forValidation().isValidationActive(register, clock.instant());
            dataValidationStatuses =
                    device.forValidation().getValidationStatus(
                            register,
                            readingRecords,
                            Range.closedOpen(
                                    Instant.ofEpochMilli(timeStamp),
                                    Instant.ofEpochMilli(timeStamp)));
        }

        return ReadingInfoFactory.asInfo(
                reading,
                register.getRegisterSpec(),
                validationStatusForRegister,
                !dataValidationStatuses.isEmpty() ? dataValidationStatuses.get(0) : null);
    }

    @PUT
    @Path("/{timeStamp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_DATA)
    public Response editRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        return editOrAddRegisterData(readingInfo, register, meter);
    }

    private Response editOrAddRegisterData(ReadingInfo readingInfo, Register<?> register, Meter meter) {
        Channel channel = resourceHelper.getRegisterChannel(register, meter).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_CHANNELS_ON_REGISTER, register.getRegisterSpec().getRegisterType().getReadingType().getAliasName()));
        List<BaseReading> readings = new ArrayList<>();
        readings.add(readingInfo.createNew(register));
        channel.editReadings(readings);

        return Response.status(Response.Status.OK).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_DATA)
    public Response addRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        if (meter.getMeterActivations().isEmpty()) {
            meter.activate(readingInfo.timeStamp);
        }
        List<MeterActivation> activations = resourceHelper.getMeterActivationsMostCurrentFirst(meter);
        findChannel(register, resourceHelper.getMeterActivationsMostCurrentFirst(meter), readingInfo.timeStamp).orElseGet(() ->{
            MeterActivation meterActivation = activations.stream().filter(a -> a.getInterval().toClosedRange().contains(readingInfo.timeStamp)).findFirst().orElse(activations.get(0));
            return meterActivation.createChannel(register.getReadingType());
        });
        return editOrAddRegisterData(readingInfo, register, meter);
    }

    private Optional<Channel> findChannel(Register<?> register, List<MeterActivation> activations, Instant when) {
        for (MeterActivation activation : activations) {
            if (activation.getInterval().toClosedRange().contains(when)) {
                for (Channel channel : activation.getChannels()) {
                    if (channel.getReadingTypes().contains(register.getReadingType())) {
                        return Optional.of(channel);
                    }
                }
            }
        }
        return Optional.empty();
    }

    @DELETE
    @Path("/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_DATA)
    public Response deleteRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("timeStamp") long timeStamp, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        Channel channel = resourceHelper.getRegisterChannel(register, meter).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_CHANNELS_ON_REGISTER, registerId));

        BaseReadingRecord reading =
                channel
                    .getReading(Instant.ofEpochMilli(timeStamp))
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_READING_ON_REGISTER, registerId, timeStamp));
        channel.removeReadings(Arrays.asList(reading));

        return Response.status(Response.Status.OK).build();
    }

    private boolean hasSuspects(ReadingInfo info) {
        boolean result = true;
        if (info instanceof BillingReadingInfo) {
            BillingReadingInfo billingReadingInfo = (BillingReadingInfo)info;
            result = ValidationStatus.SUSPECT.equals(billingReadingInfo.validationResult);
        } else if (info instanceof NumericalReadingInfo) {
            NumericalReadingInfo numericalReadingInfo = (NumericalReadingInfo)info;
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
        boolean onlySuspect = filterActive(queryParameters, "onlySuspect");
        boolean onlyNonSuspect = filterActive(queryParameters, "onlyNonSuspect");
        if (onlySuspect ^ onlyNonSuspect) {
            if (onlySuspect) {
                list.add(this::hasSuspects);
            } else {
                list.add(not(this::hasSuspects));
            }
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
