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
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment.MeasurementTaskAssignmentChangeRequestMessage;
import com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment.MeasurementTaskAssignmentChangeRequestRole;
import com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkChangeRequestProvider;
import com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkCreateRequestProvider;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Functions.asStream;

@Component(name = MeasurementTaskAssignmentChangeProcessor.NAME,
        service = MeasurementTaskAssignmentChangeProcessor.class, immediate = true,
        property = "name=" + MeasurementTaskAssignmentChangeProcessor.NAME)
public class MeasurementTaskAssignmentChangeProcessor implements TranslationKeyProvider {
    static final String COMPONENT_NAME = "MTA"; // only for translations

    public static final String NAME = "MeasurementTaskAssignmentChangeProcessor";
    public static final String VERSION = "v1.0";
    public static final String GROUP_MRID_PREFIX = "MDC:";

    public static final String DEFAULT_TASK_NAME = "Device data exporter";
    public static final String DEFAULT_GROUP_NAME = "Export device group";

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

    public void process(MeasurementTaskAssignmentChangeRequestMessage message, String selectorName) {
        String profileId = message.getProfileId();
        // parse role infos (lrn, time periods)
        Map<String, RangeSet<Instant>> lrns = new HashMap<>();
        for (MeasurementTaskAssignmentChangeRequestRole role : message.getRoles()) {
            if (WebServiceActivator.getListOfRoleCodes().contains(role.getRoleCode())) {
                // skip role codes from config
                continue;
            }
            String lrn = role.getLrn();
            Instant startDateTime = role.getStartDateTime();
            Instant endDateTime = role.getEndDateTime();
            RangeSet<Instant> rangeSet = lrns.getOrDefault(lrn, TreeRangeSet.create());
            rangeSet.add(Range.closedOpen(startDateTime, endDateTime));
            lrns.put(lrn, rangeSet);
        }

        if (!lrns.isEmpty()) {
            // unset profile id after the end date of the range
            lrns.entrySet().stream().forEach(entry -> {
                Instant endDate = Instant.EPOCH;
                boolean unset = true;
                for (Range<Instant> range : entry.getValue().asRanges()) {
                    if (range.hasUpperBound()) {
                        if (range.upperEndpoint().isAfter(endDate)) {
                            endDate = range.upperEndpoint();
                        }
                    } else {
                        unset = false;
                        break;
                    }
                }
                if (unset) {
                    sapCustomPropertySets.getChannelInfosAfterDate(entry.getKey(), profileId, endDate).stream()
                            .forEach(csi -> {
                                unsetProfileId(csi.getFirst(), csi.getLast(), entry.getKey(), profileId);
                            });
                }

            });

            // set profile id
            Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> profileIntervals = findChannelInfos(lrns);
            for (Map.Entry<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> entry : profileIntervals.entrySet()) {
                entry.getValue().stream().forEach(e -> setProfileId(profileId, entry.getKey().getFirst(),
                        entry.getKey().getLast(), e.getFirst(), e.getLast()));
            }

            // update/create end device group for export task
            Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(WebServiceActivator.getExportTaskDeviceGroupName().orElse(DEFAULT_GROUP_NAME));
            Set<Long> deviceIds = getDeviceIds(profileIntervals);
            if (endDeviceGroup.isPresent()) {
                addDevicesToEnumeratedGroup((EnumeratedEndDeviceGroup) endDeviceGroup.get(), deviceIds);
            } else {
                endDeviceGroup = Optional.of(createEnumeratedEndDeviceGroup(deviceIds));
            }

            if (endDeviceGroup.isPresent()) {
                // update/create export task
                Optional<ExportTask> exportTask = (Optional<ExportTask>) dataExportService
                        .getReadingTypeDataExportTaskByName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME));
                Set<ReadingType> readingTypes = getReadingTypes(profileIntervals);
                if (exportTask.isPresent()) {
                    updateExportTask(exportTask.get(), readingTypes, true);
                } else {
                    createExportTask((EnumeratedEndDeviceGroup) endDeviceGroup.get(), readingTypes, selectorName);
                }
            }
        } else {
            Optional<ExportTask> exportTask = (Optional<ExportTask>) dataExportService.getReadingTypeDataExportTaskByName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME));
            if (exportTask.isPresent()) {
                Set<ReadingType> readingTypes = sapCustomPropertySets.findReadingTypesForProfileId(profileId);
                // remove reading types from the data export task
                updateExportTask(exportTask.get(), readingTypes, false);
                if (getNumberOfReadingTypes(exportTask.get()) == 0) {
                    exportTask.get().delete();
                }
            }
        }
    }

    private Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> findChannelInfos(Map<String, RangeSet<Instant>> lrns) {
        Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> channelsWithIntervals = new HashMap<>();
        for (Map.Entry<String, RangeSet<Instant>> entry : lrns.entrySet()) {
            for (Range<Instant> range : entry.getValue().asRanges()) {
                Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> channelsForLrnWithinInterval =
                        sapCustomPropertySets.getChannelInfos(entry.getKey(), range);
                if (channelsForLrnWithinInterval.isEmpty()) {
                    throw new SAPWebServiceException(thesaurus, MessageSeeds.CHANNEL_IS_NOT_FOUND, entry.getKey());
                }
                List<Range<Instant>> ranges = new ArrayList<>();
                channelsForLrnWithinInterval.values().stream().forEach(listR ->
                        ranges.addAll(listR.stream().map(r -> r.getFirst()).collect(Collectors.toList())));
                if (Ranges.doAnyRangesIntersect(ranges)) {
                    throw new SAPWebServiceException(thesaurus, MessageSeeds.LRN_IS_NOT_UNIQUE, entry.getKey(),
                            range.lowerEndpoint().toString(), range.upperEndpoint().toString());
                }
                channelsWithIntervals = joinChannelInfos(channelsWithIntervals, channelsForLrnWithinInterval);
            }
        }
        return channelsWithIntervals;
    }

    private Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> joinChannelInfos(Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> channelInfos,
                                                                                                      Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> addChannelInfos) {
        for (Map.Entry<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> entry : addChannelInfos.entrySet()) {
            List<Pair<Range<Instant>, Range<Instant>>> intervals = channelInfos.getOrDefault(entry.getKey(), new ArrayList<>());
            intervals.addAll(entry.getValue());
            // need sort profile ranges by start time
            Collections.sort(intervals, Comparator.comparing(o -> o.getFirst().lowerEndpoint()));
            channelInfos.put(entry.getKey(), intervals);
        }
        return channelInfos;
    }

    private void unsetProfileId(Long deviceId, ChannelSpec channelSpec, String lrn, String profileId) {
        Optional<Device> device = deviceService.findDeviceById(deviceId);
        if (device.isPresent()) {
            Optional<CustomPropertySet> customPropertySet = device.get().getDeviceType()
                    .getLoadProfileTypeCustomPropertySet(channelSpec.getLoadProfileSpec().getLoadProfileType())
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .filter(cps -> cps.getId().equals(DeviceChannelSAPInfoCustomPropertySet.CPS_ID));
            if (customPropertySet.isPresent()) {
                String lrnPropertyName = DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName();
                String profileIdPropertyName = DeviceChannelSAPInfoDomainExtension.FieldNames.PROFILE_ID.javaName();
                List<CustomPropertySetValues> valuesList = customPropertySetService.getAllVersionedValuesFor(customPropertySet.get(), channelSpec, deviceId);
                if (!valuesList.isEmpty()) {
                    Range<Instant> leftRange = valuesList.get(0).getEffectiveRange();
                    CustomPropertySetValues oldValues = valuesList.get(0);
                    Optional<String> lrnValue = Optional.ofNullable((String) oldValues.getProperty(lrnPropertyName));
                    Optional<String> profileIdValue = Optional.ofNullable((String) oldValues.getProperty(profileIdPropertyName));
                    if (lrnValue.isPresent() && lrnValue.get().equals(lrn) && profileIdValue.isPresent() &&
                            profileIdValue.get().equals(profileId)) {
                        oldValues.setProperty(profileIdPropertyName, null);
                        customPropertySetService.setValuesVersionFor(customPropertySet.get(),
                                channelSpec, oldValues, oldValues.getEffectiveRange(), deviceId);
                    }

                    for (int i = 1; i < valuesList.size(); i++) {
                        oldValues = valuesList.get(i);
                        lrnValue = Optional.ofNullable((String) oldValues.getProperty(lrnPropertyName));
                        profileIdValue = Optional.ofNullable((String) oldValues.getProperty(profileIdPropertyName));
                        Optional<String> previousLrnValue = Optional.ofNullable((String) valuesList.get(i - 1).getProperty(lrnPropertyName));
                        Optional<String> previousProfileIdValue = Optional.ofNullable((String) valuesList.get(i - 1).getProperty(profileIdPropertyName));
                        if (lrnValue.isPresent() && lrnValue.get().equals(lrn) &&
                                (!profileIdValue.isPresent() || profileIdValue.get().equals(profileId))) {
                            oldValues.setProperty(profileIdPropertyName, null);
                            Range range = oldValues.getEffectiveRange();
                            if (previousLrnValue.isPresent() && previousLrnValue.get().equals(lrn) && !previousProfileIdValue.isPresent()) {
                                range = Ranges.closedOpen(leftRange.lowerEndpoint(), oldValues.getEffectiveRange().upperEndpoint());
                            }
                            customPropertySetService.setValuesVersionFor(customPropertySet.get(),
                                    channelSpec, oldValues, range, deviceId);
                            leftRange = range;
                        } else {
                            leftRange = oldValues.getEffectiveRange();
                        }
                    }
                }
            }
        }
    }

    private void setProfileId(String profileId, Long deviceId, ChannelSpec channelSpec,
                              Range<Instant> profileInterval, Range<Instant> lrnInterval) {
        Optional<Device> device = deviceService.findDeviceById(deviceId);
        if (device.isPresent()) {
            Optional<CustomPropertySet> customPropertySet = device.get().getDeviceType()
                    .getLoadProfileTypeCustomPropertySet(channelSpec.getLoadProfileSpec().getLoadProfileType())
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .filter(cps -> cps.getId().equals(DeviceChannelSAPInfoCustomPropertySet.CPS_ID));
            if (customPropertySet.isPresent()) {
                Optional<ChannelSpec> channelSpecForProfileId = sapCustomPropertySets.getChannelSpecForProfileId(channelSpec, deviceId, profileId, profileInterval);
                if (!channelSpecForProfileId.isPresent()) {
                    CustomPropertySetValues oldValues = customPropertySetService.getUniqueValuesFor(customPropertySet.get(),
                            channelSpec, lrnInterval.hasLowerBound() ? lrnInterval.lowerEndpoint() : Instant.EPOCH, deviceId);
                    Range tailRange = lrnInterval.hasUpperBound() ? Range.closedOpen(profileInterval.upperEndpoint(), lrnInterval.upperEndpoint()) :
                            Range.atLeast(profileInterval.upperEndpoint());
                    if (tailRange != null && !tailRange.isEmpty()) {
                        String profileIdPropertyName = DeviceChannelSAPInfoDomainExtension.FieldNames.PROFILE_ID.javaName();
                        Optional<String> profileIdOld = Optional.ofNullable((String) oldValues.getProperty(profileIdPropertyName));
                        if (profileIdOld.isPresent() && profileIdOld.get().equals(profileId)) {
                            oldValues.setProperty(profileIdPropertyName, null);
                        }
                        customPropertySetService.setValuesVersionFor(customPropertySet.get(),
                                channelSpec, oldValues, tailRange, deviceId);
                    }
                    oldValues.setProperty(DeviceChannelSAPInfoDomainExtension.FieldNames.PROFILE_ID.javaName(), profileId);
                    customPropertySetService.setValuesVersionFor(customPropertySet.get(),
                            channelSpec, oldValues, profileInterval, deviceId);
                } else {
                    throw new SAPWebServiceException(thesaurus, MessageSeeds.PROFILE_ID_IS_ALREADY_SET, profileId, channelSpecForProfileId.get().getReadingType().getFullAliasName());
                }
            }
        }
    }

    private Set<Long> getDeviceIds(Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> channelInfos) {
        List<Long> devices = new ArrayList<>();
        for (Pair<Long, ChannelSpec> pair : channelInfos.keySet()) {
            devices.add(pair.getFirst());
        }
        return devices.stream().collect(Collectors.toSet());
    }

    private Set<ReadingType> getReadingTypes(Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> channelInfos) {
        Set<ReadingType> readingTypes = new HashSet<>();
        for (Pair<Long, ChannelSpec> pair : channelInfos.keySet()) {
            readingTypes.add(pair.getLast().getReadingType());
        }
        return readingTypes.stream().collect(Collectors.toSet());
    }

    private EndDevice[] buildListOfEndDevices(Set<Long> deviceIds) {
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return deviceIds.stream().map(number -> amrSystem.findMeter(number.toString()))
                .flatMap(asStream())
                .toArray(EndDevice[]::new);
    }

    private EnumeratedEndDeviceGroup createEnumeratedEndDeviceGroup(Set<Long> deviceIds) {
        GroupBuilder.GroupCreator<? extends EnumeratedEndDeviceGroup> creator = meteringGroupsService
                .createEnumeratedEndDeviceGroup(buildListOfEndDevices(deviceIds))
                .setName(WebServiceActivator.getExportTaskDeviceGroupName().orElse(DEFAULT_GROUP_NAME))
                .setLabel("MDC")
                .setMRID(GROUP_MRID_PREFIX + WebServiceActivator.getExportTaskDeviceGroupName().orElse(DEFAULT_GROUP_NAME));
        return creator.create();
    }

    private void addDevicesToEnumeratedGroup(EnumeratedEndDeviceGroup endDeviceGroup, Set<Long> deviceIds) {
        EndDevice[] endDevices = buildListOfEndDevices(deviceIds);
        Map<Long, EnumeratedGroup.Entry<EndDevice>> currentEntries = endDeviceGroup.getEntries().stream()
                .collect(Collectors.toMap(entry -> entry.getMember().getId(), Function.identity()));
        // add new ones
        Stream.of(endDevices)
                .filter(device -> !currentEntries.containsKey(device.getId()))
                .forEach(device -> endDeviceGroup.add(device, Range.atLeast(Instant.EPOCH)));
        endDeviceGroup.update();
    }

    private void createExportTask(EnumeratedEndDeviceGroup endDeviceGroup, Set<ReadingType> readingTypes, String selectorName) {
        DataExportTaskBuilder builder = dataExportService.newBuilder()
                .setName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME))
                .setLogLevel(Level.WARNING.intValue())
                .setApplication(WebServiceActivator.APPLICATION_NAME)
                .setDataFormatterFactoryName("No operation data formatter")
                .setScheduleExpression(new TemporalExpression(TimeDuration.TimeUnit.DAYS.during(1), TimeDuration.TimeUnit.HOURS.during(0)))
                .setNextExecution(WebServiceActivator.getExportTaskStartOnDate());

        String name = dataExportService.getAvailableSelectors().stream().filter(s -> s.getDisplayName().equals(selectorName)).findAny().get().getName();
        DataExportTaskBuilder.MeterReadingSelectorBuilder selectorBuilder = builder.selectingMeterReadings(name)
                .fromExportPeriod(WebServiceActivator.getExportTaskExportWindow())
                .fromUpdatePeriod(WebServiceActivator.getExportTaskUpdateWindow())
                .fromEndDeviceGroup(endDeviceGroup)
                .continuousData(true)
                .exportComplete(MissingDataOption.EXCLUDE_INTERVAL)
                .withValidatedDataOption(ValidatedDataOption.INCLUDE_ALL)
                .exportUpdate(true);
        readingTypes.stream().forEach(selectorBuilder::fromReadingType);
        selectorBuilder.endSelection();

        EndPointConfiguration endPointCreate;
        Optional<String> endPointName = WebServiceActivator.getExportTaskNewDataEndpointName();
        if (endPointName.isPresent()) {
            endPointCreate = getEndPointConfiguration(endPointName.get());
        } else {
            endPointCreate = getEndPointConfiguration(Arrays.asList(UtilitiesTimeSeriesBulkCreateRequestProvider.NAME, UtilitiesTimeSeriesBulkChangeRequestProvider.NAME));
        }

        EndPointConfiguration endPointChange;
        endPointName = WebServiceActivator.getExportTaskUpdatedDataEndpointName();
        if (endPointName.isPresent()) {
            endPointChange = getEndPointConfiguration(endPointName.get());
        } else {
            endPointChange = getEndPointConfiguration(Arrays.asList(UtilitiesTimeSeriesBulkChangeRequestProvider.NAME, UtilitiesTimeSeriesBulkCreateRequestProvider.NAME));
        }

        if (endPointCreate != null && endPointChange != null) {
            ExportTask dataExportTask = builder.create();
            // add create and update end points
            dataExportTask.addWebServiceDestination(endPointCreate, endPointChange);
        }
    }

    private EndPointConfiguration getEndPointConfiguration(String name) {
        Optional<EndPointConfiguration> endPointConfig = endPointConfigurationService.getEndPointConfiguration(name);
        if (endPointConfig.isPresent()) {
            return endPointConfig.get();
        }
        throw new SAPWebServiceException(thesaurus, MessageSeeds.ENDPOINT_BY_NAME_NOT_FOUND, name);
    }

    private EndPointConfiguration getEndPointConfiguration(List<String> webservices) {
        for (String webservice : webservices) {
            Optional<EndPointConfiguration> endPointConfig = endPointConfigurationService.getEndPointConfigurationsForWebService(webservice)
                    .stream()
                    .filter(EndPointConfiguration::isActive)
                    .findFirst();
            if (endPointConfig.isPresent()) {
                return endPointConfig.get();
            }
        }

        throw new SAPWebServiceException(thesaurus, MessageSeeds.ENDPOINTS_NOT_FOUND, webservices.toString().join(","));
    }

    private int getNumberOfReadingTypes(ExportTask exportTask) {
        Optional<DataSelectorConfig> selectorConfig = exportTask.getStandardDataSelectorConfig();
        if (selectorConfig.isPresent()) {
            return ((MeterReadingSelectorConfig) selectorConfig.get()).getReadingTypes().size();
        }
        return 0;
    }

    private void updateExportTask(ExportTask exportTask, Set<ReadingType> readingTypes, boolean addReadingTypes) {
        Optional<DataSelectorConfig> selectorConfig = exportTask.getStandardDataSelectorConfig();
        if (selectorConfig.isPresent()) {
            MeterReadingSelectorConfig meterReadingSelectorConfig = (MeterReadingSelectorConfig) selectorConfig.get();
            ReadingDataSelectorConfig.Updater updater = meterReadingSelectorConfig.startUpdate();

            if (addReadingTypes) {
                // update reading types
                readingTypes.stream()
                        .forEach(updater::addReadingType);
            } else {
                // update reading types
                readingTypes.stream()
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
