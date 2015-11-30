package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.data.UsagePointValidation;
import com.elster.insight.usagepoint.data.UsagePointValidationImpl;
import com.elster.insight.usagepoint.data.exceptions.InvalidLastCheckedException;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.security.Privileges;
import com.google.common.collect.Range;

public class UsagePointValidationResource {
    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final ValidationInfoFactory validationInfoFactory;
    private final UsagePointConfigurationService usagePointConfigurationService;

    @Inject
    public UsagePointValidationResource(ResourceHelper resourceHelper, 
            ValidationService validationService, 
            ExceptionFactory exceptionFactory, 
            Clock clock,
            Thesaurus thesaurus,
            ValidationInfoFactory validationInfoFactory, 
            UsagePointConfigurationService usagePointConfigurationService) {
        this.resourceHelper = resourceHelper;
        this.validationService = validationService;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.validationInfoFactory = validationInfoFactory;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationRuleSetsForDevice(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        List<UsagePointValidationRuleSetInfo> result = new ArrayList<>();
        Optional<? extends MeterActivation> activation = usagePoint.getCurrentMeterActivation();
//        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        Optional<MetrologyConfiguration> OPTconfig = usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(usagePoint);
        MetrologyConfiguration config = OPTconfig.get();
        
        List<ValidationRuleSet> linkedRuleSets = config.getValidationRuleSets();
        fillValidationRuleSetStatus(linkedRuleSets, activation, result, usagePoint);
        Collections.sort(result, ValidationRuleSetInfo.VALIDATION_RULESET_NAME_COMPARATOR);
        return Response.ok(PagedInfoList.fromPagedList("rulesets",
                ListPager.of(result).from(queryParameters).find(), queryParameters)).build();
    }

    private void fillValidationRuleSetStatus(List<ValidationRuleSet> linkedRuleSets, Optional<? extends MeterActivation> activation, List<UsagePointValidationRuleSetInfo> result, UsagePoint usagePoint) {
        List<? extends ValidationRuleSet> activeRuleSets = activation.map(validationService::activeRuleSets).orElse(Collections.emptyList());
        for (ValidationRuleSet ruleSet : linkedRuleSets) {
            boolean isActive = (!activation.isPresent()) || activeRuleSets.contains(ruleSet);
            UsagePointValidationRuleSetInfo info = new UsagePointValidationRuleSetInfo(ruleSet, isActive);
            info.usagePoint = new UsagePointInfo(usagePoint, clock);
            result.add(info);
        }
    }

//    @Path("/{validationRuleSetId}/status")
//    @PUT
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
//    @DeviceStatesRestricted({DefaultState.DECOMMISSIONED})
//    public Response setValidationRuleSetStatusOnDevice(@PathParam("mrid") String mrid, @PathParam("validationRuleSetId") long validationRuleSetId, DeviceValidationRuleSetInfo info) {
//        info.device.mRID = mRID;
//        Device device = resourceHelper.lockDeviceOrThrowException(info.device);
//        ValidationRuleSet ruleSet = getValidationRuleSet(validationRuleSetId);
//        Optional<? extends MeterActivation> activation = device.getCurrentMeterActivation();
//        if (activation.isPresent()) {
//            setValidationRuleSetActivationStatus(activation.get(), ruleSet, info.isActive);
//        } else {
//            throw exceptionFactory.newException(MessageSeeds.DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE, ruleSet.getName());
//        }
//        device.save();
//        return Response.status(Response.Status.OK).build();
//    }
//
//    private void setValidationRuleSetActivationStatus(MeterActivation activation, ValidationRuleSet ruleSet, boolean status) {
//      if (status) {
//    	  validationService.activate(activation, ruleSet);
//      } else {
//    	  validationService.deactivate(activation, ruleSet);
//      }
//    }
//
    @Path("/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("mrid") String mRID) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mRID);

        UsagePointValidationStatusInfo deviceValidationStatusInfo = determineStatus(usagePoint);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }
//
//    @Path("/validationmonitoring/configurationview")
//    @GET
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
//    public Response getValidationMonitoringConfigurationView(@PathParam("mRID") String mRID,
//                                                             @QueryParam("filter") JsonQueryFilter filter) {
//
//        List<DataValidationStatus> lpStatuses = new ArrayList<>();
//
//        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
//        DeviceValidation deviceValidation = device.forValidation();
//        ValidationStatusInfo validationStatusInfo =
//                new ValidationStatusInfo(
//                        deviceValidation.isValidationActive(),
//                        deviceValidation.getLastChecked(),
//                        device.hasData());
//        if(filter.hasProperty("intervalRegisterStart")
//                && filter.hasProperty("intervalRegisterEnd")) {
//            ValidationInfoParser parser = new ValidationInfoParser();
//            List<ValidationLoadProfilePeriodInfo> loadProfilePeriodInfos = filter.getPropertyList("intervalLoadProfile", parser::parseFromNode);
//            loadProfilePeriodInfos.stream().forEach(lpPeriod -> {
//
//                Range<Instant> intervalLP = Range.openClosed(Instant.ofEpochMilli(lpPeriod.startInterval), Instant.ofEpochMilli(lpPeriod.endInterval));
//                lpStatuses.addAll(device.getLoadProfiles().stream()
//                        .filter(loadProfile -> lpPeriod.id.equals(loadProfile.getId()))
//                        .flatMap(l -> l.getChannels().stream())
//                        .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), intervalLP).stream())
//                        .filter(s -> (s.getReadingQualities().stream().anyMatch(q -> q.getType().qualityIndex().orElse(QualityCodeIndex.DATAVALID).equals(QualityCodeIndex.SUSPECT))))
//                        .collect(Collectors.toList()));
//            });
//        }
//        Range<Instant> intervalReg = Range.openClosed(Instant.ofEpochMilli(filter.getLong("intervalRegisterStart")), Instant.ofEpochMilli(filter.getLong("intervalRegisterEnd")));
//
//        List<DataValidationStatus> rgStatuses = device.getRegisters().stream()
//                .flatMap(r -> device.forValidation().getValidationStatus(r, Collections.emptyList(), intervalReg).stream())
//                .filter(s -> (s.getReadingQualities().stream().anyMatch(q -> q.getType().qualityIndex().orElse(QualityCodeIndex.DATAVALID).equals(QualityCodeIndex.SUSPECT))))
//                .collect(Collectors.toList());
//
//        validationStatusInfo.allDataValidated = isAllDataValidated(device);
//
//        List<DataValidationStatus> statuses = new ArrayList<>();
//        statuses.addAll(lpStatuses);
//        statuses.addAll(rgStatuses);
//
//        MonitorValidationInfo info = validationInfoFactory.createMonitorValidationInfoForValidationStatues(statuses, validationStatusInfo);
//
//        return Response.status(Response.Status.OK).entity(info).build();
//    }
//
//    @Path("/validationmonitoring/dataview")
//    @GET
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
//    public Response getValidationMonitoringDataView(@PathParam("mRID") String mRID,
//                                                    @QueryParam("filter") JsonQueryFilter filter) {
//
//        Map<LoadProfile, List<DataValidationStatus>> loadProfileStatus = new HashMap<>();
//
//        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
//
//        DeviceValidation deviceValidation = device.forValidation();
//        ValidationStatusInfo validationStatusInfo =
//                new ValidationStatusInfo(
//                        deviceValidation.isValidationActive(),
//                        deviceValidation.getLastChecked(),
//                        device.hasData());
//        if(filter.hasProperty("intervalRegisterStart")
//                && filter.hasProperty("intervalRegisterEnd")) {
//            ValidationInfoParser parser = new ValidationInfoParser();
//            List<ValidationLoadProfilePeriodInfo> loadProfilePeriodInfos = filter.getPropertyList("intervalLoadProfile", parser::parseFromNode);
//            loadProfilePeriodInfos.stream().forEach(lpPeriod -> {
//
//                Range<Instant> intervalLP = Range.openClosed(Instant.ofEpochMilli(lpPeriod.startInterval), Instant.ofEpochMilli(lpPeriod.endInterval));
//
//                loadProfileStatus.putAll(device.getLoadProfiles().stream()
//                        .filter(loadProfile -> lpPeriod.id.equals(loadProfile.getId()))
//                        .collect(Collectors.toMap(
//                                l -> l,
//                                lp ->
//                                        lp.getChannels().stream()
//                                                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), intervalLP).stream())
//                                                .filter(s -> (s.getReadingQualities().stream().anyMatch(q -> q.getType().qualityIndex().orElse(QualityCodeIndex.DATAVALID).equals(QualityCodeIndex.SUSPECT))))
//                                                .collect(Collectors.toList())
//                        )).entrySet().stream().filter(m -> (((List<DataValidationStatus>) m.getValue()).size()) > 0L)
//                        .collect(Collectors.toMap(m -> (LoadProfile) (m.getKey()), m -> (List<DataValidationStatus>) (m.getValue()))));
//            });
//        }
//        Range<Instant> intervalReg = Range.openClosed(Instant.ofEpochMilli(filter.getLong("intervalRegisterStart")), Instant.ofEpochMilli(filter.getLong("intervalRegisterEnd")));
//
//        Map<NumericalRegister, List<DataValidationStatus>> registerStatus = device.getRegisters().stream()
//                .collect(Collectors.toMap(
//                        r -> r,
//                        reg -> (device.forValidation().getValidationStatus(reg, Collections.emptyList(), intervalReg).stream())
//                                .filter(s -> (s.getReadingQualities().stream().anyMatch(q -> q.getType().qualityIndex().orElse(QualityCodeIndex.DATAVALID).equals(QualityCodeIndex.SUSPECT))))
//                                .collect(Collectors.toList())
//                )).entrySet().stream().filter(m -> (((List<DataValidationStatus>) m.getValue()).size()) > 0L)
//                .collect(Collectors.toMap(m -> (NumericalRegister) (m.getKey()), m -> (List<DataValidationStatus>) (m.getValue())));
//
//        validationStatusInfo.allDataValidated = isAllDataValidated(device);
//
//        MonitorValidationInfo info = validationInfoFactory.createMonitorValidationInfoForLoadProfileAndRegister(loadProfileStatus, registerStatus, validationStatusInfo);
//
//        return Response.status(Response.Status.OK).entity(info).build();
//    }
//
//    @Path("/validationmonitoring/register")
//    @GET
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
//    public Response getValidationMonitoringRegister(@PathParam("mRID") String mrid) {
//        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
//        ZonedDateTime end = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.DAYS).plusDays(1);
//
//        ZonedDateTime intervalStart = end.minusYears(1);
//        Range<Instant> interval = Range.openClosed(intervalStart.toInstant(), end.toInstant());
//
//        Map<NumericalRegister, Long> registerStatus = device.getRegisters().stream()
//                .collect(Collectors.toMap(
//                        r -> r,
//                        reg -> (device.forValidation().getValidationStatus(reg, Collections.emptyList(), interval).stream())
//                                .collect(Collectors.counting())
//                )).entrySet().stream().filter(m -> ((Long) m.getValue()) > 0L)
//                .collect(Collectors.toMap(m -> (NumericalRegister) (m.getKey()), m -> (Long) (m.getValue())));
//
//        return Response.status(Response.Status.OK).entity(registerStatus).build();
//    }
    
    public UsagePointValidation getUsagePointValidation(UsagePoint usagePoint) {
        return new UsagePointValidationImpl(validationService, clock, thesaurus, usagePoint, usagePointConfigurationService);
    }
//
    private UsagePointValidationStatusInfo determineStatus(UsagePoint usagePoint) {
        UsagePointValidation usagePointValidation = getUsagePointValidation(usagePoint);
        UsagePointValidationStatusInfo usagePointValidationStatusInfo =
                new UsagePointValidationStatusInfo(
                usagePointValidation.isValidationActive(),
                usagePointValidation.isValidationOnStorage(),
                usagePointValidation.getLastChecked(),
                usagePoint.hasData());

        ZonedDateTime end = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        collectRegisterData(usagePoint, usagePointValidationStatusInfo, end);
        collectChannelData(usagePoint, usagePointValidationStatusInfo, end);
        usagePointValidationStatusInfo.usagePoint = new UsagePointInfo(usagePoint, clock);

        return usagePointValidationStatusInfo;
    }

    private void collectChannelData(UsagePoint usagePoint, UsagePointValidationStatusInfo usagePointValidationStatusInfo, ZonedDateTime end) {
        List<Channel> irregularChannels = new ArrayList<Channel>();
        List<Channel> regularChannels = new ArrayList<Channel>();
        
        ZonedDateTime loadProfileStart = end.minusMonths(1);
        Range<Instant> loadProfileInterval = Range.openClosed(loadProfileStart.toInstant(), end.toInstant());

        MeterActivation currentActivation = usagePoint.getCurrentMeterActivation().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, usagePoint.getMRID()));
        List<Channel> channelCandidates = currentActivation.getChannels();
        for (Channel channel : channelCandidates) {
            if (!channel.isRegular())
                irregularChannels.add(channel);
            else 
                regularChannels.add(channel);
        }
        
        List<DataValidationStatus> statuses = regularChannels.stream()
                .flatMap(c -> getUsagePointValidation(usagePoint).getValidationStatus(c, Collections.emptyList(), loadProfileInterval).stream())
                .collect(Collectors.toList());

        usagePointValidationStatusInfo.channelSuspectCount = statuses.stream()
                .flatMap(d -> d.getReadingQualities().stream())
                .filter(r -> QualityCodeIndex.SUSPECT.equals(r.getType().qualityIndex().orElse(null)))
                .count();
        if (statuses.isEmpty()) {
            usagePointValidationStatusInfo.allDataValidated &= irregularChannels.stream()
                    .allMatch(r -> getUsagePointValidation(usagePoint).allDataValidated(r, clock.instant()));
        } else {
            usagePointValidationStatusInfo.allDataValidated &= statuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);
        }
    }

    private void collectRegisterData(UsagePoint usagePoint, UsagePointValidationStatusInfo usagePointValidationStatusInfo, ZonedDateTime end) {
        ZonedDateTime registerStart = end.minusYears(1);
        Range<Instant> registerRange = Range.openClosed(registerStart.toInstant(), end.toInstant());
        List<Channel> irregularChannels = new ArrayList<Channel>();
        List<Channel> regularChannels = new ArrayList<Channel>();
        MeterActivation currentActivation = usagePoint.getCurrentMeterActivation().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, usagePoint.getMRID()));
        List<Channel> channelCandidates = currentActivation.getChannels();
        for (Channel channel : channelCandidates) {
            if (!channel.isRegular())
                irregularChannels.add(channel);
            else 
                regularChannels.add(channel);
        }
        
        List<DataValidationStatus> statuses = irregularChannels.stream()
                .flatMap(r -> getUsagePointValidation(usagePoint).getValidationStatus(r, Collections.emptyList(), registerRange).stream())
                .collect(Collectors.toList());

        usagePointValidationStatusInfo.registerSuspectCount = statuses.stream()
                .flatMap(d -> d.getReadingQualities().stream())
                .filter(r -> QualityCodeIndex.SUSPECT.equals(r.getType().qualityIndex().orElse(null)))
                .count();
        if (statuses.isEmpty()) {
            usagePointValidationStatusInfo.allDataValidated &= regularChannels.stream()
                    .allMatch(r -> getUsagePointValidation(usagePoint).allDataValidated(r, clock.instant()));
        } else {
            usagePointValidationStatusInfo.allDataValidated = statuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);
        }
    }

    @Path("/validationstatus")
    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response setValidationFeatureStatus(@PathParam("mrid") String mRID, UsagePointValidationStatusInfo info) {
        info.usagePoint.mRID = mRID;
        UsagePoint usagePoint = resourceHelper.lockUsagePointOrThrowException(info.usagePoint);
        try {
            if (info.isActive) {
                if (info.lastChecked == null) {
                    throw new LocalizedFieldValidationException(MessageSeeds.NULL_DATE, "lastChecked");
                }
                if(info.isStorage){
                    getUsagePointValidation(usagePoint).activateValidationOnStorage(Instant.ofEpochMilli(info.lastChecked));
                } else{
                    getUsagePointValidation(usagePoint).activateValidation(Instant.ofEpochMilli(info.lastChecked));
                }
            } else {
                getUsagePointValidation(usagePoint).deactivateValidation();
            }
            usagePoint.update();
        } catch (InvalidLastCheckedException e) {
            throw new LocalizedFieldValidationException(e.getMessageSeed(), "lastChecked", usagePoint.getMRID(), e.getOldLastChecked(), e.getNewLastChecked());
        }
        return Response.status(Response.Status.OK).build();
    }

//    @Path("/validate")
//    @PUT
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL)
//    @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
//    public Response validateDeviceData(@PathParam("mRID") String mRID, DeviceInfo info) {
//        info.mRID = mRID;
//        Device device = resourceHelper.lockDeviceOrThrowException(info);
//        device.forValidation().validateData();
//        device.save();
//        return Response.status(Response.Status.OK).build();
//    }
//
//    private ValidationRuleSet getValidationRuleSet(long validationRuleSetId) {
//        Optional<? extends ValidationRuleSet> ruleSet = validationService.getValidationRuleSet(validationRuleSetId);
//        return ruleSet.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
//    }
//
//    private boolean isAllDataValidated(Device device) {
//        boolean result = true;
//
//        ZonedDateTime end = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.DAYS).plusDays(1);
//
//        Range<Instant> loadProfileRange = Range.openClosed(end.minusMonths(1).toInstant(), end.toInstant());
//
//        List<DataValidationStatus> lpStatuses = device.getLoadProfiles().stream()
//                .flatMap(l -> l.getChannels().stream())
//                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), loadProfileRange).stream())
//                .collect(Collectors.toList());
//
//            result &= device.getLoadProfiles().stream()
//                    .flatMap(l -> l.getChannels().stream())
//                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));
//
//            result &= lpStatuses.stream()
//                    .allMatch(DataValidationStatus::completelyValidated);
//
//        Range<Instant> registerRange = Range.openClosed(end.minusYears(1).toInstant(), end.toInstant());
//
//        List<DataValidationStatus> rgStatuses = device.getRegisters().stream()
//                .flatMap(r -> device.forValidation().getValidationStatus(r, Collections.emptyList(), registerRange).stream())
//                .collect(Collectors.toList());
//
//            result &= device.getRegisters().stream()
//                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));
//
//            result &= rgStatuses.stream()
//                    .allMatch(DataValidationStatus::completelyValidated);
//
//        return result;
//    }

}
