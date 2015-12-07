package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.data.UsagePointValidation;
import com.elster.insight.usagepoint.data.UsagePointValidationImpl;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;

public class RegisterResourceHelper {

    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new ChannelComparator();

    private final ResourceHelper resourceHelper;
    private final Clock clock;
    private ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final ValidationService validationService;
    private final ValidationInfoFactory validationInfoFactory;

    @Inject
    public RegisterResourceHelper(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Clock clock, Thesaurus thesaurus, UsagePointConfigurationService usagePointConfigurationService, ValidationService validationService, ValidationInfoFactory validationInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.validationService = validationService;
        this.validationInfoFactory = validationInfoFactory;
    }

    public Response getRegisters(String mrid, JsonQueryParameters queryParameters) {
        List<Channel> irregularChannels = new ArrayList<Channel>();
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        MeterActivation currentActivation = usagepoint.getCurrentMeterActivation().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, mrid));
        List<Channel> channelCandidates = currentActivation.getChannels();
        for (Channel channel : channelCandidates) {
            if (!channel.isRegular())
                irregularChannels.add(channel);
        }
        List<Channel> channelsPage = ListPager.of(irregularChannels, CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();

        List<RegisterInfo> registerInfos = new ArrayList<>();
        for (Channel channel : channelsPage) {
            RegisterInfo registerInfo = RegisterInfo.from(channel);
            addValidationInfo(channel, registerInfo, usagepoint);
            registerInfos.add(registerInfo);
        }
        return Response.ok(PagedInfoList.fromPagedList("registers", registerInfos, queryParameters)).build();
    }
    
    public void addValidationInfo(Channel channel, RegisterInfo registerInfo, UsagePoint usagepoint) {
        UsagePointValidation upv = getUsagePointValidation(usagepoint);
        List<DataValidationStatus> states =
                upv.getValidationStatus(channel, Collections.emptyList(), lastYear());
        registerInfo.validationInfo = validationInfoFactory.createDetailedValidationInfo(isValidationActive(channel, upv), states, upv.getLastChecked(channel));
        if (states.isEmpty()) {
            registerInfo.validationInfo.dataValidated = upv.allDataValidated(channel, clock.instant());
        }
    }
    
    public DetailedValidationInfo getRegisterValidationInfo(UsagePoint usagePoint, Channel channel) {
        UsagePointValidation upv = getUsagePointValidation(usagePoint);
        List<DataValidationStatus> states =
                upv.getValidationStatus(channel, Collections.emptyList(), lastYear());
        return validationInfoFactory.createDetailedValidationInfo(isValidationActive(channel, upv), states, upv.getLastChecked(channel));
    }
    
    public boolean isValidationActive(Channel channel, UsagePointValidation upv) {
        return upv.isValidationActive(channel, clock.instant());
    }
    
    private Range<Instant> lastYear() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusYears(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }

    public Optional<Channel> findRegisterOnUsagePoint(String mrid, String rt_mrid) {
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        MeterActivation currentActivation = usagepoint.getCurrentMeterActivation().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, mrid));

        List<Channel> channelCandidates = currentActivation.getChannels();
        for (Channel channel : channelCandidates) {
            if (rt_mrid.equals(channel.getMainReadingType().getMRID())) {
                return Optional.of(channel);
            }
        }
        return Optional.empty();
    }

    public Response getRegister(Supplier<Channel> channelSupplier, String mrid) {
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        Channel channel = channelSupplier.get();
        RegisterInfo registerInfo = RegisterInfo.from(channel);
        addValidationInfo(channel, registerInfo, usagepoint);
        return Response.ok(registerInfo).build();
    }
    
    public UsagePointValidation getUsagePointValidation(UsagePoint usagePoint) {
        return new UsagePointValidationImpl(validationService, clock, thesaurus, usagePoint, usagePointConfigurationService);
    }

}