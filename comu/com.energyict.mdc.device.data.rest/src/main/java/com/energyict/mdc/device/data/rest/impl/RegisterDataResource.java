package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationService;
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
import java.util.*;

public class RegisterDataResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final ValidationService validationService;
    private final MeteringService meteringService;

    @Inject
    public RegisterDataResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, ValidationService validationService, MeteringService meteringService) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.validationService = validationService;
        this.meteringService = meteringService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = getMeterFor(device);
        List<Reading> readings = ListPager.of(register.getReadings(Interval.sinceEpoch()), new Comparator<Reading>() {
            @Override
            public int compare(Reading o1, Reading o2) {
                return o2.getTimeStamp().compareTo(o1.getTimeStamp());
            }
        }).from(queryParameters).find();
        List<ReadingRecord> readingRecords = getReadingRecords(readings);
        Optional<Channel> channelRef = getRegisterChannel(register, meter);
        List<DataValidationStatus> dataValidationStatuses = new ArrayList<>();
        Boolean validationStatusForRegister = false;
        if(channelRef.isPresent()) {
            validationStatusForRegister = getValidationStatus(channelRef.get(), meter);
            dataValidationStatuses = validationService.getValidationStatus(channelRef.get(), readingRecords);
        }
        List<ReadingInfo> readingInfos = ReadingInfoFactory.asInfoList(readings, register.getRegisterSpec(),
                validationStatusForRegister, dataValidationStatuses);
        return PagedInfoList.asJson("data", readingInfos, queryParameters);
    }

    private List<ReadingRecord> getReadingRecords(List<Reading> readings) {
        List<ReadingRecord> readingRecords = new ArrayList<>(readings.size());
        for(Reading reading : readings) {
            readingRecords.add(reading.getActualReading());
        }
        return readingRecords;
    }

    private Optional<Channel> getRegisterChannel(Register register, Meter meter) {
        for (MeterActivation meterActivation : getMeterActivationsMostCurrentFirst(meter)) {
            Optional<Channel> channelRef = getChannel(meterActivation, register.getRegisterSpec().getRegisterType().getReadingType());
            if(channelRef.isPresent()) {
                return channelRef;
            }
        }
        return Optional.absent();
    }

    private MeterActivation getMeterActivationFor(Channel channel, Meter meter) {
        for (MeterActivation meterActivation : getMeterActivationsMostCurrentFirst(meter)) {
            if(meterActivation.getChannels().contains(channel)) {
                return meterActivation;
            }
        }
        return null;
    }

    private Boolean getValidationStatus(Channel channel, Meter meter) {
        MeterActivation meterActivation = getMeterActivationFor(channel, meter);
        List<? extends MeterActivationValidation> activeValidations = validationService.getActiveMeterActivationValidations(meterActivation);
        return (!validationService.validationEnabled(meter) || activeValidations.isEmpty()) ? false : getValidationStatusForChannel(activeValidations, channel) ;
    }

    private Meter getMeterFor(Device device) {
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(1);
        Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(device.getId()));
        if (!meterRef.isPresent()) {
            throw new IllegalArgumentException("Validation feature on device " + device.getmRID() +
                    " wasn't configured.");
        }
        return meterRef.get();
    }

    private List<MeterActivation> getMeterActivationsMostCurrentFirst(Meter meter) {
        List<MeterActivation> activations = new ArrayList<>(meter.getMeterActivations());
        Collections.reverse(activations);
        return activations;
    }

    private Optional<com.elster.jupiter.metering.Channel> getChannel(MeterActivation meterActivation, ReadingType readingType) {
        for (com.elster.jupiter.metering.Channel channel : meterActivation.getChannels()) {
            if (channel.getReadingTypes().contains(readingType)) {
                return Optional.of(channel);
            }
        }
        return Optional.absent();
    }

    private Boolean getValidationStatusForChannel(List<? extends MeterActivationValidation> activeValidations, Channel channel) {
        for (MeterActivationValidation meterActivationValidation : activeValidations) {
            if (isThereChannelValidationForChannel(meterActivationValidation, channel)) {
                return true;
            }
        }
        return false;
    }

    private boolean isThereChannelValidationForChannel(MeterActivationValidation validation, Channel channel) {
        for(ChannelValidation channelValidation : validation.getChannelValidations()) {
            if(channelValidation.getChannel().equals(channel)) {
                return true;
            }
        }
        return false;
    }
}
