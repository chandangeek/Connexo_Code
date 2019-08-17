/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedGroup;
import com.elster.jupiter.metering.groups.GroupBuilder;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment.MeasurementTaskAssignmentChangeConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment.MeasurementTaskAssignmentChangeRequestMessage;
import com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment.MeasurementTaskAssignmentChangeRequestRole;
import com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkChangeRequestProvider;
import com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkCreateRequestProvider;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static com.energyict.mdc.sap.soap.webservices.impl.InboundServices.SAP_MEASUREMENT_TASK_ASSIGNMENT_CHANGE_REQUEST;
import static com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys.EXPORTER;
import static java.time.temporal.ChronoUnit.DAYS;

@Component(name = MeasurementTaskAssignmentChangeFactory.NAME,
        service = MeasurementTaskAssignmentChangeFactory.class, immediate = true,
        property = "name=" + MeasurementTaskAssignmentChangeFactory.NAME)
public class MeasurementTaskAssignmentChangeFactory implements TranslationKeyProvider {
    static final String COMPONENT_NAME = "MTA"; // only for translations

    public static final String NAME = "MeasurementTaskAssignmentChangeFactory";
    public static final String VERSION = "v1.0";
    public static final String GROUP_MRID_PREFIX = "MDC:";

    private static final String DEFAULT_TASK_NAME = "Device data exporter";
    private static final String DEFAULT_GROUP_NAME = "Export device group";
    private static final String DEFAULT_EXPORT_WINDOW = "Yesterday";
    private static final String DEFAULT_UPDATE_WINDOW = "Previous month";

    private volatile Clock clock;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile DataExportService dataExportService;
    private volatile DeviceService deviceService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MeteringService meteringService;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile TimeService timeService;
    private volatile Thesaurus thesaurus;

    public void sendMessage(String id) {
        MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage =
                MeasurementTaskAssignmentChangeConfirmationMessage.builder(clock.instant(), id)
                        .from()
                        .build();
        WebServiceActivator.MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATIONS
                .forEach(service -> service.call(confirmationMessage));
    }

    public void sendErrorMessage(String id, String level, String typeId, String errorMessage) {
        MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage =
                MeasurementTaskAssignmentChangeConfirmationMessage.builder(clock.instant(), id)
                        .from(level, typeId, errorMessage)
                        .build();
        WebServiceActivator.MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATIONS
                .forEach(service -> service.call(confirmationMessage));
    }

    public void processServiceCall(MeasurementTaskAssignmentChangeRequestMessage message) {
        String profileId = message.getProfileId();
        // parse role infos (lrn, time periods)
        Map<String, List<Range<Instant>>> lrns = new HashMap<>();
        for (MeasurementTaskAssignmentChangeRequestRole role : message.getRoles()) {
            if (WebServiceActivator.getListOfRoleCodes().contains(role.getRoleCode())) {
                // skip role codes from config
                continue;
            }
            String lrn = role.getLrn();
            Instant startDateTime = role.getStartDateTime();
            Instant endDateTime = role.getEndDateTime();
            List<Range<Instant>> listRanges = lrns.getOrDefault(lrn, new ArrayList<>());
            listRanges.add(Range.closedOpen(startDateTime, endDateTime));
            lrns.put(lrn, listRanges);
        }

        if (!lrns.isEmpty()) {
            // set profile ID
            Map<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> profileIntervals = findChannelInfos(lrns);
            for (Map.Entry<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> entry : profileIntervals.entrySet()) {
                entry.getValue().stream().forEach(e -> setProfileId(profileId, entry.getKey().getFirst(),
                        entry.getKey().getLast(), e.getFirst(), e.getLast()));
            }

            // update/create end device group for export task
            Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroup(GROUP_MRID_PREFIX + WebServiceActivator.getExportTaskDeviceGroupName().orElse(DEFAULT_GROUP_NAME));
            List<Long> deviceIds = getDeviceIds(profileIntervals);
            if (endDeviceGroup.isPresent()) {
                updateEnumeratedEndDeviceGroup((EnumeratedEndDeviceGroup) endDeviceGroup.get(), deviceIds);
            } else {
                endDeviceGroup = Optional.of(createEnumeratedEndDeviceGroup(deviceIds));
            }

            if (endDeviceGroup.isPresent()) {
                // update/create export task
                Optional<ExportTask> exportTask = (Optional<ExportTask>) dataExportService
                        .getReadingTypeDataExportTaskByName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME));
                List<ReadingType> readingTypes = getReadingTypes(profileIntervals);
                if (exportTask.isPresent()) {
                    updateExportTask(exportTask.get(), readingTypes, true);
                } else {
                    createExportTask((EnumeratedEndDeviceGroup) endDeviceGroup.get(), readingTypes);
                }
            }
        } else {
            Optional<ExportTask> exportTask = (Optional<ExportTask>) dataExportService.getReadingTypeDataExportTaskByName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME));
            if (exportTask.isPresent()) {
                List<ReadingType> readingTypes = sapCustomPropertySets.findReadingTypesForProfileId(profileId);
                // remove reading types from the data export task
                updateExportTask(exportTask.get(), readingTypes, false);
                if (getNumberOfReadingTypes(exportTask.get()) == 0) {
                    exportTask.get().delete();
                }
            }
        }
    }

    private Map<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> findChannelInfos(Map<String, List<Range<Instant>>> lrns) {
        Map<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> channelsWithIntervals = new HashMap<>();
        for (Map.Entry<String, List<Range<Instant>>> entry : lrns.entrySet()) {
            for (Range<Instant> range : entry.getValue()) {
                Map<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> channelsForLrnWithinInterval =
                        sapCustomPropertySets.getChannelInfos(entry.getKey(), range);
                if (channelsForLrnWithinInterval.isEmpty()) {
                    throw new SAPWebServiceException(thesaurus, MessageSeeds.CHANNEL_IS_NOT_FOUND, entry.getKey());
                }
                List<Range<Instant>> ranges = new ArrayList<>();
                channelsForLrnWithinInterval.values().stream().forEach(listR ->
                        ranges.addAll(listR.stream().map(r -> r.getFirst()).collect(Collectors.toList())));
                if (sapCustomPropertySets.isRangesIntersected(ranges)) {
                    throw new SAPWebServiceException(thesaurus, MessageSeeds.LRN_IS_NOT_UNIQUE, entry.getKey(),
                            range.lowerEndpoint().toString(), range.upperEndpoint().toString());
                }
                channelsWithIntervals = joinChannelInfos(channelsWithIntervals, channelsForLrnWithinInterval);
            }
        }
        return channelsWithIntervals;
    }

    private Map<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> joinChannelInfos(Map<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> channelInfos,
                                                                                                      Map<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> addChannelInfos) {
        for (Map.Entry<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> entry : addChannelInfos.entrySet()) {
            List<Pair<Range<Instant>, Range<Instant>>> intervals = channelInfos.getOrDefault(entry.getKey(), new ArrayList<>());
            intervals.addAll(entry.getValue());
            // need sort profile ranges by start time
            Collections.sort(intervals, Comparator.comparing(o -> o.getFirst().lowerEndpoint()));
            channelInfos.put(entry.getKey(), intervals);
        }
        return channelInfos;
    }

    private void setProfileId(String profileId, Long deviceId, ReadingType readingType,
                              Range<Instant> profileInterval, Range<Instant> lrnInterval) {
        Optional<Device> device = deviceService.findDeviceById(deviceId);
        if (device.isPresent()) {
            Optional<Channel> channel = device.get().getChannels().stream().filter(c -> c.getReadingType().equals(readingType)).findFirst();
            if (channel.isPresent()) {
                Optional<CustomPropertySet> customPropertySet = device.get().getDeviceType()
                        .getLoadProfileTypeCustomPropertySet(channel.get().getLoadProfile().getLoadProfileSpec().getLoadProfileType())
                        .map(RegisteredCustomPropertySet::getCustomPropertySet)
                        .filter(cps -> cps.getId().equals(DeviceChannelSAPInfoCustomPropertySet.CPS_ID));
                if (customPropertySet.isPresent()) {
                    if (!sapCustomPropertySets.isProfileIdAlreadyExists(channel.get(), profileId, profileInterval)) {
                        CustomPropertySetValues oldValues = customPropertySetService.getUniqueValuesFor(customPropertySet.get(),
                                channel.get().getChannelSpec(), lrnInterval.lowerEndpoint(), deviceId);
                        Range tailRange = Range.closedOpen(profileInterval.upperEndpoint(), lrnInterval.upperEndpoint());
                        if (tailRange != null && !tailRange.isEmpty()) {
                            customPropertySetService.setValuesVersionFor(customPropertySet.get(),
                                    channel.get().getChannelSpec(), oldValues, tailRange, deviceId);
                        }
                        oldValues.setProperty(DeviceChannelSAPInfoDomainExtension.FieldNames.PROFILE_ID.javaName(), profileId);
                        customPropertySetService.setValuesVersionFor(customPropertySet.get(),
                                channel.get().getChannelSpec(), oldValues, profileInterval, deviceId);
                    } else {
                        throw new SAPWebServiceException(thesaurus, MessageSeeds.PROFILE_ID_IS_ALREADY_SET, profileId, channel.get().getName());
                    }
                }
            }
        }
    }

    private List<Long> getDeviceIds(Map<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> channelInfos) {
        List<Long> devices = new ArrayList<>();
        for (Pair<Long, ReadingType> pair : channelInfos.keySet()) {
            devices.add(pair.getFirst());
        }
        return devices.stream().distinct().collect(Collectors.toList());
    }

    private List<ReadingType> getReadingTypes(Map<Pair<Long, ReadingType>, List<Pair<Range<Instant>, Range<Instant>>>> channelInfos) {
        List<ReadingType> readingTypes = new ArrayList<>();
        for (Pair<Long, ReadingType> pair : channelInfos.keySet()) {
            readingTypes.add(pair.getLast());
        }
        return readingTypes.stream().distinct().collect(Collectors.toList());
    }

    private EndDevice[] buildListOfEndDevices(List<Long> deviceIds) {
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return deviceIds.stream().map(number -> amrSystem.findMeter(number.toString()))
                .flatMap(asStream())
                .toArray(EndDevice[]::new);
    }

    private EnumeratedEndDeviceGroup createEnumeratedEndDeviceGroup(List<Long> deviceIds) {
        GroupBuilder.GroupCreator<? extends EnumeratedEndDeviceGroup> creator = meteringGroupsService
                .createEnumeratedEndDeviceGroup(buildListOfEndDevices(deviceIds))
                .setName(WebServiceActivator.getExportTaskDeviceGroupName().orElse(DEFAULT_GROUP_NAME))
                .setLabel("MDC")
                .setMRID(GROUP_MRID_PREFIX + WebServiceActivator.getExportTaskDeviceGroupName().orElse(DEFAULT_GROUP_NAME));
        return creator.create();
    }

    private void updateEnumeratedEndDeviceGroup(EnumeratedEndDeviceGroup endDeviceGroup, List<Long> deviceIds) {
        EndDevice[] endDevices = buildListOfEndDevices(deviceIds);
        Map<Long, EnumeratedGroup.Entry<EndDevice>> currentEntries = endDeviceGroup.getEntries().stream()
                .collect(Collectors.toMap(entry -> entry.getMember().getId(), Function.identity()));
        // add new ones
        Stream.of(endDevices)
                .filter(device -> !currentEntries.containsKey(device.getId()))
                .forEach(device -> endDeviceGroup.add(device, Range.atLeast(Instant.EPOCH)));
        endDeviceGroup.update();
    }

    private void createExportTask(EnumeratedEndDeviceGroup endDeviceGroup, List<ReadingType> readingTypes) {
        RelativePeriod exportWindow = findRelativePeriodOrThrowException(WebServiceActivator.getExportTaskExportWindow().orElse(DEFAULT_EXPORT_WINDOW));
        RelativePeriod updateWindow = findRelativePeriodOrThrowException(WebServiceActivator.getExportTaskUpdateWindow().orElse(DEFAULT_UPDATE_WINDOW));
        Instant startOn = clock.instant().plus(1, DAYS);
        if (WebServiceActivator.getExportTaskStartOnDate().isPresent()) {
            LocalDateTime startOnDate = LocalDateTime.parse(WebServiceActivator.getExportTaskStartOnDate().get(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH));
            startOn = startOnDate.atZone(ZoneId.systemDefault()).toInstant();
        }

        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME))
                .setLogLevel(Level.WARNING.intValue())
                .setApplication(WebServiceActivator.APPLICATION_NAME)
                .setDataFormatterFactoryName("No operation data formatter")
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .setNextExecution(startOn);

        EndPointConfiguration endPointChangeTask = getEndPointConfiguration(SAP_MEASUREMENT_TASK_ASSIGNMENT_CHANGE_REQUEST.getName());
        boolean custom = (Boolean) endPointChangeTask.getPropertiesWithValue().get(EXPORTER.getKey());
        String selector = dataExportService.STANDARD_READINGTYPE_DATA_SELECTOR;
        if (custom) {
            selector = dataExportService.CUSTOM_READINGTYPE_DATA_SELECTOR;
        }
        DataExportTaskBuilder.MeterReadingSelectorBuilder selectorBuilder = builder.selectingMeterReadings(selector)
                .fromExportPeriod(exportWindow)
                .fromUpdatePeriod(updateWindow)
                .fromEndDeviceGroup(endDeviceGroup)
                .continuousData(true)
                .exportComplete(MissingDataOption.EXCLUDE_INTERVAL)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true);
        readingTypes.stream().forEach(selectorBuilder::fromReadingType);
        selectorBuilder.endSelection();

        EndPointConfiguration endPointCreate = getEndPointConfiguration(UtilitiesTimeSeriesBulkCreateRequestProvider.NAME);
        EndPointConfiguration endPointChange = getEndPointConfiguration(UtilitiesTimeSeriesBulkChangeRequestProvider.NAME);
        if (endPointCreate != null && endPointChange != null) {
            ExportTask dataExportTask = builder.create();

            // add create and update end points
            dataExportTask.addWebServiceDestination(endPointCreate, endPointChange);
        }
    }

    private EndPointConfiguration getEndPointConfiguration(String webServiceName) {
        Optional<EndPointConfiguration> endPointConfig = endPointConfigurationService.findEndPointConfigurations().stream()
                .filter(endPointConfiguration -> endPointConfiguration.getWebServiceName().equals(webServiceName)).findFirst();
        if (endPointConfig.isPresent()) {
            return endPointConfig.get();
        }
        return null;
    }

    private RelativePeriod findRelativePeriodOrThrowException(String name) {
        return timeService.findRelativePeriodByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find relative period '" + name + "'."));
    }

    private int getNumberOfReadingTypes(ExportTask exportTask) {
        Optional<DataSelectorConfig> selectorConfig = exportTask.getStandardDataSelectorConfig();
        if (exportTask.getStandardDataSelectorConfig().isPresent()) {
            return ((MeterReadingSelectorConfig) selectorConfig.get()).getReadingTypes().size();
        }
        return 0;
    }

    private void updateExportTask(ExportTask exportTask, List<ReadingType> readingTypes, boolean addReadingTypes) {
        Optional<DataSelectorConfig> selectorConfig = exportTask.getStandardDataSelectorConfig();
        if (exportTask.getStandardDataSelectorConfig().isPresent()) {
            MeterReadingSelectorConfig meterReadingSelectorConfig = (MeterReadingSelectorConfig) selectorConfig.get();
            ReadingDataSelectorConfig.Updater updater = meterReadingSelectorConfig.startUpdate();

            if (addReadingTypes) {
                // update reading types
                readingTypes.stream()
                        .filter(r -> meterReadingSelectorConfig.getReadingTypes()
                                .stream()
                                .noneMatch(r::equals))
                        .forEach(updater::addReadingType);
            } else {
                // update reading types
                readingTypes.stream()
                        .filter(r -> meterReadingSelectorConfig.getReadingTypes()
                                .stream()
                                .noneMatch(r::equals))
                        .forEach(updater::removeReadingType);
            }
            exportTask.update();
        }
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(getComponentName(), getLayer());
    }
}
