/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;

import ch.iec.tc57._2011.getmeterreadings.DataSource;
import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.getmeterreadings.EndDevice;
import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.GetMeterReadings;
import ch.iec.tc57._2011.getmeterreadings.GetMeterReadingsPort;
import ch.iec.tc57._2011.getmeterreadings.Name;
import ch.iec.tc57._2011.getmeterreadings.NameType;
import ch.iec.tc57._2011.getmeterreadings.Reading;
import ch.iec.tc57._2011.getmeterreadings.ReadingType;
import ch.iec.tc57._2011.getmeterreadings.UsagePoint;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestMessageType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsResponseMessageType;
import ch.iec.tc57._2011.getmeterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class ExecuteMeterReadingsEndpoint extends AbstractInboundEndPoint implements GetMeterReadingsPort, ApplicationSpecific {
    private static final String NOUN = "MeterReadings";
    private static final String GET_METER_READINGS_ITEM = "GetMeterReadings";
    private static final String READING_TYPES_LIST_ITEM = GET_METER_READINGS_ITEM + ".ReadingType";
    private static final String READING_LIST_ITEM = GET_METER_READINGS_ITEM + ".Reading";
    private static final String READING_ITEM = READING_LIST_ITEM + "[%d]";
    private static final String USAGE_POINTS_LIST_ITEM = GET_METER_READINGS_ITEM + ".UsagePoint";
    private static final String END_DEVICE_LIST_ITEM = GET_METER_READINGS_ITEM + ".EndDevice";
    private static final String USAGE_POINT_ITEM = USAGE_POINTS_LIST_ITEM + "[0]";
    private static final String USAGE_POINT_MRID = USAGE_POINT_ITEM + ".mRID";
    private static final String USAGE_POINT_NAME_ITEMS = USAGE_POINT_ITEM
            + ".Names[?(@.NameType.name=='" + UsagePointNameTypeEnum.USAGE_POINT_NAME.getNameType() + "')]";
    private static final String USAGE_POINT_NAME = USAGE_POINT_NAME_ITEMS + ".name";
    private static final String DATA_SOURCE = READING_ITEM + ".dataSource";
    private static final String DATA_SOURCE_NAME = DATA_SOURCE + ".name";
    private static final String DATA_SOURCE_NAME_TYPE = DATA_SOURCE + ".NameType";
    private static final String DATA_SOURCE_NAME_TYPE_NAME = DATA_SOURCE_NAME_TYPE + ".name";


    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ObjectFactory getMeterReadingsMessageObjectFactory = new ObjectFactory();

    private final Provider<MeterReadingsBuilder> readingBuilderProvider;
    private final ReplyTypeFactory replyTypeFactory;
    private final MeterReadingFaultMessageFactory faultMessageFactory;
    private final Clock clock;
    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServicesService webServicesService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final MasterDataService masterDataService;
    private SyncReplyIssue syncReplyIssue;

    @Inject
    public ExecuteMeterReadingsEndpoint(Provider<MeterReadingsBuilder> readingBuilderProvider,
                                        ReplyTypeFactory replyTypeFactory,
                                        MeterReadingFaultMessageFactory faultMessageFactory,
                                        Clock clock, ServiceCallCommands serviceCallCommands,
                                        EndPointConfigurationService endPointConfigurationService,
                                        WebServicesService webServicesService, MeteringService meteringService,
                                        DeviceService deviceService, MasterDataService masterDataService) {
        this.readingBuilderProvider = readingBuilderProvider;
        this.replyTypeFactory = replyTypeFactory;
        this.faultMessageFactory = faultMessageFactory;
        this.clock = clock;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.masterDataService = masterDataService;
    }

    @Override
    public MeterReadingsResponseMessageType getMeterReadings(GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage) throws
            FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            try {
            syncReplyIssue = new SyncReplyIssue(replyTypeFactory);
            GetMeterReadings getMeterReadings = Optional.ofNullable(getMeterReadingsRequestMessage.getRequest()
                    .getGetMeterReadings())
                    .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, GET_METER_READINGS_ITEM));
            boolean async = false;
            if (getMeterReadingsRequestMessage.getHeader() != null) {
                async = Optional.ofNullable(getMeterReadingsRequestMessage.getHeader().isAsyncReplyFlag())
                        .orElse(false);
            }
            checkGetMeterReading(getMeterReadings, async);
            // run async
            if (async) {
                return runAsyncMode(getMeterReadingsRequestMessage);
            }
            // run sync
            // -EndDevice
            List<EndDevice> endDevices = getMeterReadings.getEndDevice();
            MeterReadingsBuilder builder = readingBuilderProvider.get();
            if (CollectionUtils.isNotEmpty(endDevices)) {
                fillMetersInfo(endDevices.stream().limit(1).collect(Collectors.toList()));
                builder.withEndDevices(syncReplyIssue.getExistedMeters());
                fillReadingTypesInfo(builder, getMeterReadings.getReadingType(), async);
                if (!async && (CollectionUtils.isEmpty(syncReplyIssue.getExistedReadingTypes()))) {
                    throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES)
                            .get();
                }
                fillNotFoundReadingTypesOnDevices();

                if (endDevices.size() > 1) {
                    syncReplyIssue.addErrorType((replyTypeFactory.errorType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, null, END_DEVICE_LIST_ITEM)));
                }
                MeterReadings meterReadings = builder
                        .inTimeIntervals(getTimeIntervals(getMeterReadings.getReading()))
                        .build();
                /// TODO use correlationid
                return createMeterReadingsResponseMessageType(meterReadings, syncReplyIssue.getResultErrorTypes(), null);
            }
            // -UsagePoint
            List<UsagePoint> usagePoints = getMeterReadings.getUsagePoint();
            UsagePoint usagePoint = usagePoints.stream().findFirst()
                    .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, USAGE_POINTS_LIST_ITEM));

            setUsagePointInfo(builder, usagePoint);
            fillReadingTypesInfo(builder, getMeterReadings.getReadingType(), async);
            MeterReadings meterReadings = builder
                    .fromPurposes(extractNamesWithType(usagePoint.getNames(), UsagePointNameTypeEnum.PURPOSE))
                    .inTimeIntervals(getTimeIntervals(getMeterReadings.getReading()))
                    .build();
            MeterReadingsResponseMessageType meterReadingsResponseMessageType =
                    createMeterReadingsResponseMessageType(meterReadings, null);
            meterReadingsResponseMessageType.setReply(usagePoints.size() > 1 ?
                    replyTypeFactory.partialFailureReplyType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, null, USAGE_POINTS_LIST_ITEM) :
                    replyTypeFactory.okReplyType());
            return meterReadingsResponseMessageType;
        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.createMeterReadingFaultMessage(e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.createMeterReadingFaultMessage(e.getLocalizedMessage(), e.getErrorCode());
        }
        });
    }

    private MeterReadingsResponseMessageType runAsyncMode(GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage) throws FaultMessage {
        String correlationId = getMeterReadingsRequestMessage.getHeader().getCorrelationID();
        if (correlationId != null) {
            checkIsEmpty(correlationId, GET_METER_READINGS_ITEM + ".Header.CorrelationID");
        }
        String replyAddress = getMeterReadingsRequestMessage.getHeader().getReplyAddress();
        checkIfMissingOrIsEmpty(replyAddress, GET_METER_READINGS_ITEM + ".Header.ReplyAddress");
        List<EndDevice> endDevices = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getEndDevice();
        List<Reading> readings = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getReading();
        List<ReadingType> readingTypes = getMeterReadingsRequestMessage.getRequest()
                .getGetMeterReadings()
                .getReadingType();

        if (endDevices.isEmpty()) {
            throw (faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, END_DEVICE_LIST_ITEM))
                    .get();
        }
        fillReadingTypesInfo(null, readingTypes, true);
        fillMetersInfo(endDevices);
        Set<Device> devices = getDevices(syncReplyIssue.getExistedMeters());
        fillNotFoundReadingTypesOnDevices();

        publishOutboundEndPointConfiguration(replyAddress);
        for (int i = 0; i < readings.size(); ++i) {
            final String readingItem = String.format(READING_ITEM, i);
            Reading reading = readings.get(i);
            if (!checkTimeInterval(reading, readingItem, true)) {
                syncReplyIssue.addNotUsedReadingsDueToTimeStamp(i);
                continue;
            }

            if (!checkConnectionMethod(reading.getConnectionMethod(), readingItem, devices)) {
                syncReplyIssue.addNotUsedReadingsDueToConnectionMethod(i);
                continue;
            }

            if (reading.getScheduleStrategy() != null) {
                checkIsEmpty(reading.getScheduleStrategy(), readingItem + ".scheduleStrategy");
            }
            // readingTypes from readingTypes + loadProfiles + registerGroups
            Set<com.elster.jupiter.metering.ReadingType> combinerReadingTypes = new HashSet<>(syncReplyIssue.getExistedReadingTypes());
            if (reading.getDataSource() != null && !reading.getDataSource().isEmpty()) {
                DataSourceTypeNameEnum dsTypeName = getDataSourceNameType(reading.getDataSource(), i);
                List<String> dsNames = getDataSourceNames(reading.getDataSource(), i);
                if (dsTypeName != null && !dsNames.isEmpty()) {
                    fillDataSource(dsTypeName, dsNames, i, combinerReadingTypes);
                } else {
                    syncReplyIssue.addNotUsedReadingsDueToDataSources(i);
                    continue;
                }
            }

            if (!checkDataSources(reading.getTimePeriod(), reading.getSource(), i)) {
                syncReplyIssue.addNotUsedReadingsDueToDataSources(i);
                continue;
            }

            serviceCallCommands.createParentGetMeterReadingsServiceCallWithChilds(getMeterReadingsRequestMessage.getHeader(),
                    reading, i, syncReplyIssue, combinerReadingTypes);
            syncReplyIssue.addExistedReadingsIndexes(i);
        }

        // no meter readings on sync reply! It's built in parent service call
        MeterReadings meterReadings = null;
        return createMeterReadingsResponseMessageType(meterReadings, syncReplyIssue.getResultErrorTypes(), correlationId);
    }

    private void fillDataSource(DataSourceTypeNameEnum dsTypeName, List<String> dsNames, int index,
                                Set<com.elster.jupiter.metering.ReadingType> combinerReadingTypes) throws FaultMessage {
        if (dsTypeName == DataSourceTypeNameEnum.LOAD_PROFILE) {
            Set<LoadProfileType> existedLoadProfiles = getExistedLoadProfiles(dsNames, index);
            syncReplyIssue.addReadingsExistedLoadProfilesMap(index, existedLoadProfiles.stream()
                    .map(lpt -> lpt.getName())
                    .collect(Collectors.toSet()));
            combinerReadingTypes.addAll(existedLoadProfiles.stream()
                    .map(LoadProfileType::getChannelTypes)
                    .flatMap(Collection::stream)
                    .map(channelType -> channelType.getReadingType())
                    .collect(Collectors.toSet()));
        } else if (dsTypeName == DataSourceTypeNameEnum.REGISTER_GROUP) {
            Set<RegisterGroup> existedRegisterGroups = getExistedRegisterGroups(dsNames, index);
            syncReplyIssue.addReadingExistedRegisterGroupMap(index, existedRegisterGroups.stream()
                    .map(rg -> rg.getName())
                    .collect(Collectors.toSet()));
            combinerReadingTypes.addAll(existedRegisterGroups.stream()
                    .map(RegisterGroup::getRegisterTypes)
                    .flatMap(Collection::stream)
                    .map(registerType -> registerType.getReadingType())
                    .collect(Collectors.toSet()));
        }
    }

    private boolean checkConnectionMethod(String connectionMethod, String readingItem, Set<Device> devices) throws
            FaultMessage {
        if (connectionMethod != null) {
            checkIsEmpty(connectionMethod, readingItem + ".connectionMethod");
            int numberOfDevicesWithConnection = 0;
            for (Device device : devices) {
                if (!checkConnectionMethodExists(device, connectionMethod)) {
                    syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.CONNECTION_METHOD_NOT_FOUND_ON_DEVICE, null,
                            connectionMethod, device.getName()));
                } else {
                    numberOfDevicesWithConnection++;
                }
            }
            if (numberOfDevicesWithConnection == 0) {
                return false;
            }
        }
        return true;
    }

    private Set<Device> getDevices(Set<com.elster.jupiter.metering.Meter> existedMeters) {
        return existedMeters.stream()
                .map(meter -> {
                    Long deviceId = Long.parseLong(meter.getAmrId());
                    return deviceService.findDeviceById(deviceId).get();
                })
                .collect(Collectors.toSet());
    }

    private boolean checkDataSources(DateTimeInterval timePeriod, String source, int index) {
        Set<String> existedLoadProfiles = syncReplyIssue.getReadingExistedLoadProfilesMap().get(index);
        Set<String> existedRegisterGroups = syncReplyIssue.getReadingExistedRegisterGroupsMap().get(index);

        boolean hasLoadProfiles = CollectionUtils.isNotEmpty(existedLoadProfiles);
        boolean hasRegisterGroups = CollectionUtils.isNotEmpty(existedRegisterGroups);
        boolean hasReadingTypes = CollectionUtils.isNotEmpty(syncReplyIssue.getExistedReadingTypes());

        if (hasLoadProfiles) {
            if (source.equals(ReadingSourceEnum.SYSTEM.getSource())) {
                syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.SYSTEM_SOURCE_DOESNT_SUPPORT_LOAD_PROFILES, null,
                        String.format(READING_ITEM, index)));
                return false;
            }
        } else if (hasRegisterGroups) {
            if (timePeriod == null) {
                syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.REGISTER_GROUP_EMPTY_TIME_PERIOD, null,
                        String.format(READING_ITEM, index)));
                return false;
            } else if (timePeriod.getStart() == null/* || timePeriod.getEnd() == null*/) {
                syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.REGISTER_GROUP_WRONG_TIME_PERIOD, null,
                        String.format(READING_ITEM, index), timePeriod.getStart(), timePeriod.getEnd()));
                return false;
            }

        }
        if (!hasLoadProfiles && !hasRegisterGroups && !hasReadingTypes) {
            syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.NO_DATA_SOURCES, null,
                    String.format(READING_ITEM, index)));
            return false;
        }
        if (hasLoadProfiles && hasRegisterGroups
                || hasLoadProfiles && hasReadingTypes
                || hasRegisterGroups && hasReadingTypes) {
            syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.DIFFERENT_DATA_SOURCES, null,
                    String.format(READING_ITEM, index)));
            return false;
        }
        return true;
    }

    private boolean checkConnectionMethodExists(Device device, String connectionMethod) throws FaultMessage {
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
        for (ComTaskExecution cte : comTaskExecutions) { // foreach is used due to avoid exception handling inside lambda
            if (checkConnectionMethodForComTaskExecution(cte, connectionMethod)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkConnectionMethodForComTaskExecution(ComTaskExecution comTaskExecution, String connectionMethod) throws
            FaultMessage {
        return comTaskExecution.getConnectionTask()
                .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_CONNECTION_TASK,
                        comTaskExecution.getComTask().getName()))
                .getPartialConnectionTask()
                .getName()
                .equalsIgnoreCase(connectionMethod);
    }

    private Set<LoadProfileType> getExistedLoadProfiles(List<String> loadProfileNames, int index) throws FaultMessage {
        Set<LoadProfileType> existedLoadProfiles = new HashSet<>();
        if (loadProfileNames != null) {
            Map<String, LoadProfileType> lpNameLoadProfileMap = masterDataService.findAllLoadProfileTypes().stream()
                    .collect(Collectors.toMap(LoadProfileType::getName, lp -> lp, (a, b) -> a));
            loadProfileNames.forEach(lpName -> {
                if (lpNameLoadProfileMap.containsKey(lpName)) {
                    existedLoadProfiles.add(lpNameLoadProfileMap.get(lpName));
                } else {
                    syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.LOAD_PROFILE_NOT_FOUND, null,
                            lpName, String.format(READING_ITEM, index)));
                }
            });
        }
        return existedLoadProfiles;
    }

    private Set<RegisterGroup> getExistedRegisterGroups(List<String> existedRegisterGroupsNames, int index) {
        Set<RegisterGroup> registerGroups = new HashSet<>();
        if (existedRegisterGroupsNames != null) {
            Map<String, RegisterGroup> rgNameRegisterGroupMap = masterDataService.findAllRegisterGroups().stream()
                    .collect(Collectors.toMap(RegisterGroup::getName, rg -> rg));
            existedRegisterGroupsNames.forEach(rgName -> {
                if (rgNameRegisterGroupMap.containsKey(rgName)) {
                    registerGroups.add(rgNameRegisterGroupMap.get(rgName));
                } else {
                    syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.REGISTER_GROUP_NOT_FOUND, null,
                            rgName, String.format(READING_ITEM, index)));
                }
            });
        }
        return registerGroups;
    }

    // LoadProfile or RegisterGroup
    private DataSourceTypeNameEnum getDataSourceNameType(List<DataSource> dataSources, int index) throws
            ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        DataSourceTypeNameEnum dsNameType = null;
        for (DataSource dataSource : dataSources) {
            if (dataSource.getNameType() == null) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT,
                        String.format(DATA_SOURCE_NAME_TYPE, index)).get();
            } else if (Strings.isNullOrEmpty(dataSource.getNameType().getName())) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT,
                        String.format(DATA_SOURCE_NAME_TYPE_NAME, index)).get();
            }
            if (dsNameType != null && dsNameType != DataSourceTypeNameEnum.getByName(dataSource.getNameType()
                    .getName())) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.DIFFERENT_DATA_SOURCES,
                        String.format(READING_ITEM, index)).get();
            } else {
                dsNameType = DataSourceTypeNameEnum.getByName(dataSource.getNameType().getName());
                if (dsNameType == null) {
                    throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.DATA_SOURCE_NAME_TYPE_NOT_FOUND,
                            dataSource.getNameType().getName(), String.format(READING_ITEM, index)).get();
                }
            }
        }
        return dsNameType;
    }

    private List<String> getDataSourceNames(List<DataSource> dataSources, int index) {
        List<String> dsNames = new ArrayList<>();
        for (DataSource dataSource : dataSources) {
            String dsName = dataSource.getName(); // e.g. 15min Electricity A+
            if (!Strings.isNullOrEmpty(dataSource.getName())) {
                dsNames.add(dsName);
            } else {
                syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.MISSING_ELEMENT, null,
                        String.format(DATA_SOURCE_NAME, index)));
            }
        }
        return dsNames;
    }

    private MeterReadingsResponseMessageType createMeterReadingsResponseMessageType(MeterReadings meterReadings,
                                                                                    List<ErrorType> errorTypes,
                                                                                    String correlationId) {
        MeterReadingsResponseMessageType meterReadingsResponseMessageType =
                createMeterReadingsResponseMessageType(meterReadings, correlationId);
        ReplyType replyType;
        if (errorTypes.isEmpty()) {
            replyType = replyTypeFactory.okReplyType();
        } else {
            ReplyType.Result replyTypeRes = ReplyType.Result.PARTIAL;
            if (syncReplyIssue.getExistedReadingsIndexes().isEmpty()) {
                replyTypeRes = ReplyType.Result.FAILED;
            }
            replyType = replyTypeFactory.failureReplyType(replyTypeRes, errorTypes.stream()
                    .toArray(ErrorType[]::new));
        }
        meterReadingsResponseMessageType.setReply(replyType);
        return meterReadingsResponseMessageType;
    }

    private void publishOutboundEndPointConfiguration(String url) throws FaultMessage {
        EndPointConfiguration endPointConfig = endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst()
                .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_END_POINT_WITH_URL, url));
        if (!webServicesService.isPublished(endPointConfig)) {
            webServicesService.publishEndPoint(endPointConfig);
        }
        if (!webServicesService.isPublished(endPointConfig)) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL, url)
                    .get();
        }
    }

    private void checkGetMeterReading(GetMeterReadings getMeterReadings, boolean async) throws FaultMessage {
        if (!getMeterReadings.getEndDeviceGroup().isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_ELEMENT, "EndDeviceGroup", GET_METER_READINGS_ITEM)
                    .get();
        }
        if (!getMeterReadings.getUsagePointGroup().isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_ELEMENT, "UsagePointGroup", GET_METER_READINGS_ITEM)
                    .get();
        }
        if (getMeterReadings.getReading().isEmpty()) {
            throw (faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, READING_LIST_ITEM)
                    .get());
        }
        if (getMeterReadings.getReadingType().isEmpty() && !async) {
            throw (faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, READING_TYPES_LIST_ITEM)
                    .get());
        }
        checkSources(getMeterReadings.getReading(), async);
    }

    private void checkSources(List<Reading> readings, boolean async) throws FaultMessage {
        for (int i = 0; i < readings.size(); ++i) {
            final String readingItem = String.format(READING_ITEM, i);
            if (async) {
                checkAsyncSource(readings.get(i).getSource(), readingItem);
            } else {
                checkSyncSource(readings.get(i).getSource(), i, readingItem);
            }
        }
    }

    private void checkAsyncSource(String source, String readingItem) throws FaultMessage {
        if (!isApplicableSource(source)) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_VALUE, readingItem + ".source", source,
                    new StringBuilder().append('\'')
                            .append(ReadingSourceEnum.SYSTEM.getSource()).append("\', \'")
                            .append(ReadingSourceEnum.METER.getSource()).append("\' or \'")
                            .append(ReadingSourceEnum.HYBRID.getSource()).append('\'').toString()
            ).get();
        }
    }

    private void checkSyncSource(String source, int index, String readingItem) throws FaultMessage {
        checkIfMissingOrIsEmpty(source, READING_LIST_ITEM + ".source");
        if (!ReadingSourceEnum.SYSTEM.getSource().equals(source)) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_VALUE, readingItem + ".source", source,
                    ReadingSourceEnum.SYSTEM.getSource()
            ).get();
        }
    }

    private boolean isApplicableSource(String source) throws FaultMessage {
        checkIfMissingOrIsEmpty(source, READING_LIST_ITEM + ".source");
        return source.equals(ReadingSourceEnum.SYSTEM.getSource())
                || source.equals(ReadingSourceEnum.METER.getSource())
                || source.equals(ReadingSourceEnum.HYBRID.getSource());

    }

    private void checkIfMissingOrIsEmpty(String element, String elementName) throws FaultMessage {
        checkIfMissing(element, elementName);
        checkIsEmpty(element, elementName);
    }

    private void checkIfMissing(String element, String elementName) throws FaultMessage {
        if (element == null) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                    MessageSeeds.MISSING_ELEMENT, elementName).get();
        }
    }

    private void checkIsEmpty(String element, String elementName) throws FaultMessage {
        if (Checks.is(element).emptyOrOnlyWhiteSpace()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                    MessageSeeds.EMPTY_ELEMENT, elementName).get();
        }
    }

    private void fillNotFoundReadingTypesOnDevices() {
        Map<String, Set<String>> notFoundReadingTypesOnDevices = new HashMap<>();

        for (com.elster.jupiter.metering.EndDevice endDevice : syncReplyIssue.getExistedMeters()) {
            Set<String> notFoundReadingTypes = new HashSet<>();
            syncReplyIssue.getExistedReadingTypes().forEach(readingType -> {
                Meter meter = (Meter) endDevice;
                boolean isReadingTypePresent = false;
                for (ChannelsContainer channelsContainer : meter.getChannelsContainers()) {
                    if (channelsContainer.getChannel(readingType).isPresent()) {
                        isReadingTypePresent = true;
                    }
                }
                if (!isReadingTypePresent) {
                    notFoundReadingTypes.add(readingType.getMRID());
                }
            });
            if (!notFoundReadingTypes.isEmpty()) {
                notFoundReadingTypesOnDevices.put(endDevice.getName(), notFoundReadingTypes);
            }
        }
        syncReplyIssue.setNotFoundReadingTypesOnDevices(notFoundReadingTypesOnDevices);
    }

    private Set<com.elster.jupiter.metering.Meter> fromEndDevicesWithMRIDsAndNames(Set<String> mRIDs, Set<String> names) throws
            FaultMessage {
        List<com.elster.jupiter.metering.EndDevice> existedEndDevices = meteringService.getEndDeviceQuery()
                .select(where("mRID").in(new ArrayList<>(mRIDs)).or(where("name").in(new ArrayList<>(names))));
        if (CollectionUtils.isEmpty(existedEndDevices)) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_END_DEVICES).get();
        }
        Set<com.elster.jupiter.metering.Meter> existedMeters = new HashSet<>();
        for (com.elster.jupiter.metering.EndDevice endDevice : existedEndDevices) {
            if (endDevice instanceof com.elster.jupiter.metering.Meter) {
                existedMeters.add((com.elster.jupiter.metering.Meter) endDevice);
            }
        }
        return existedMeters;
    }

    private Set<com.elster.jupiter.metering.ReadingType> getReadingTypes(Set<String> readingTypesMRIDs,
                                                                         Set<String> readingTypesNames, boolean asyncFlag) throws
            FaultMessage {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        Condition condition = filter.getCondition().and(where("mRID").in(new ArrayList<>(readingTypesMRIDs)))
                .or(where("fullAliasName").in(new ArrayList<>(readingTypesNames)));
        filter.addCondition(condition);
        Set<com.elster.jupiter.metering.ReadingType> readingTypes = meteringService.findReadingTypes(filter).stream()
                .collect(Collectors.toSet());
        if (!asyncFlag && (CollectionUtils.isEmpty(readingTypes))) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES).get();
        }
        return readingTypes;
    }

    private Set<com.elster.jupiter.metering.Meter> fillMetersInfo(List<EndDevice> endDevices) throws FaultMessage {
        Set<String> mRIDs = new HashSet<>();
        Set<String> fullAliasNames = new HashSet<>();
        for (int i = 0; i < endDevices.size(); ++i) {
            collectDeviceMridsAndNames(endDevices.get(i), i, mRIDs, fullAliasNames);
        }

        Set<com.elster.jupiter.metering.Meter> meterList = fromEndDevicesWithMRIDsAndNames(mRIDs, fullAliasNames);
        syncReplyIssue.setExistedMeters(meterList);
        fillNotFoundEndDevicesMRIDsAndNames(meterList, mRIDs, fullAliasNames);
        return meterList;
    }

    private void fillNotFoundEndDevicesMRIDsAndNames(Set<com.elster.jupiter.metering.Meter> MeterList,
                                                     Set<String> requiredMRIDs, Set<String> requiredNames) {
        Set<String> existedNames = MeterList.stream()
                .map(endDevice -> endDevice.getName())
                .collect(Collectors.toSet());
        Set<String> existedmRIDs = MeterList.stream()
                .map(endDevice -> endDevice.getMRID())
                .collect(Collectors.toSet());
        syncReplyIssue.setNotFoundMRIDs(requiredMRIDs.stream()
                .filter(mrid -> !existedmRIDs.contains(mrid))
                .collect(Collectors.toSet()));
        syncReplyIssue.setNotFoundNames(requiredNames.stream()
                .filter(name -> !existedNames.contains(name))
                .collect(Collectors.toSet()));

    }

    private void fillReadingTypesInfo(MeterReadingsBuilder builder, List<ReadingType> readingTypes,
                                      boolean asyncFlag) throws FaultMessage {
        Set<String> mRIDs = new HashSet<>();
        Set<String> fullAliasNames = new HashSet<>();
        for (int i = 0; i < readingTypes.size(); ++i) {
            setReadingTypeInfo(readingTypes.get(i), i, mRIDs, fullAliasNames);
        }
        if (builder != null) {
            builder.ofReadingTypesWithMRIDs(mRIDs);
            builder.ofReadingTypesWithFullAliasNames(fullAliasNames);
        }
        syncReplyIssue.setExistedReadingTypes(getReadingTypes(mRIDs, fullAliasNames, asyncFlag));
        fillNotFoundReadingTypesMRIDsAndNames(mRIDs, fullAliasNames);
    }

    private void fillNotFoundReadingTypesMRIDsAndNames(Set<String> requiredMRIDs, Set<String> requiredNames) {
        Set<String> existedmRIDs = syncReplyIssue.getExistedReadingTypes().stream()
                .map(readingType -> readingType.getMRID())
                .collect(Collectors.toSet());
        Set<String> existedNames = syncReplyIssue.getExistedReadingTypes().stream()
                .map(readingType -> readingType.getFullAliasName())
                .collect(Collectors.toSet());
        syncReplyIssue.setNotFoundRTMRIDs(requiredMRIDs.stream()
                .filter(mrid -> !existedmRIDs.contains(mrid))
                .collect(Collectors.toSet()));
        syncReplyIssue.setNotFoundRTNames(requiredNames.stream()
                .filter(name -> !existedNames.contains(name))
                .collect(Collectors.toSet()));
    }

    private void setReadingTypeInfo(ReadingType readingType, int index, Set<String> mRIDs, Set<String> fullAliasNames)
            throws FaultMessage {
        final String READING_TYPE_ITEM = READING_TYPES_LIST_ITEM + '[' + index + ']';
        String mRID = readingType.getMRID();
        if (mRID == null) {
            List<Name> names = readingType.getNames();
            if (names.size() > 1) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.UNSUPPORTED_LIST_SIZE, READING_TYPE_ITEM + ".Names", 1).get();
            }

            String name = names.stream()
                    .findFirst()
                    .map(Name::getName)
                    .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(
                            MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, READING_TYPE_ITEM));
            checkIsEmpty(name, READING_TYPE_ITEM + ".Names[0].name");
            fullAliasNames.add(name);
        } else {
            checkIsEmpty(mRID, READING_TYPE_ITEM + ".mRID");
            mRIDs.add(mRID);
        }
    }

    private RangeSet<Instant> getTimeIntervals(List<Reading> readings) throws FaultMessage {
        if (readings.isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, READING_LIST_ITEM)
                    .get();
        }
        RangeSet<Instant> result = TreeRangeSet.create();
        for (int i = 0; i < readings.size(); ++i) {
            if (!checkTimeInterval(readings.get(i), String.format(READING_ITEM, i), false)) {
                syncReplyIssue.addNotUsedReadingsDueToTimeStamp(i);
                continue;
            }
            result.add(getTimeInterval(readings.get(i)));
        }
        return result;
    }

    private Range<Instant> getTimeInterval(Reading reading) {
        return Range.openClosed(reading.getTimePeriod().getStart(), reading.getTimePeriod().getEnd());
    }

    private boolean checkTimeInterval(Reading reading, String readingItem, boolean asyncFlag) {
        DateTimeInterval interval = reading.getTimePeriod();
        if (interval == null) {
            if (reading.getSource().equals(ReadingSourceEnum.SYSTEM.getSource())) {
                syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.SYSTEM_SOURCE_EMPTY_TIME_PERIOD, null));
                return false;
            }
            if (hasIrregularReadingTypes()) {
                syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.REGISTER_EMPTY_TIME_PERIOD, null, readingItem));
            }
        }
        if (interval != null) { //optional
            Instant start = interval.getStart();
            Instant end = interval.getEnd();
            if (!asyncFlag) {
                if (start == null) {
                    syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.MISSING_ELEMENT, null,
                            readingItem + ".timePeriod.start"));
                    return false;
                }
                if (end == null) {
                    end = clock.instant();
                    interval.setEnd(end);
                }
            }
            if (start == null && end == null) {
                if (reading.getSource().equals(ReadingSourceEnum.SYSTEM.getSource())) {
                    syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.SYSTEM_SOURCE_EMPTY_TIME_PERIOD, null));
                    return false;
                }
                if (hasIrregularReadingTypes()) {
                    syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.REGISTER_EMPTY_TIME_PERIOD, null, readingItem));
                }
            }
            if (start == null && end != null) {
                syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.WRONG_TIME_PERIOD_COMBINATION, null,
                        null, XsdDateTimeConverter.marshalDateTime(end)));
                return false;
            }
            if (start != null && end != null && !end.isAfter(start)) {
                syncReplyIssue.addErrorType(replyTypeFactory.errorType(MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD, null,
                        XsdDateTimeConverter.marshalDateTime(start), XsdDateTimeConverter.marshalDateTime(end)));
                return false;
            }
        }
        return true;
    }

    private boolean hasIrregularReadingTypes() {
        return syncReplyIssue.getExistedReadingTypes().stream()
                .anyMatch(readingType -> !readingType.isRegular());
    }

    private void collectDeviceMridsAndNames(EndDevice endDevice, int index, Set<String> mRIDs, Set<String> deviceNames) throws
            FaultMessage {
        final String END_DEVICES_ITEM = END_DEVICE_LIST_ITEM + '[' + index + ']';
        String mRID = endDevice.getMRID();
        if (mRID == null) {
            List<Name> names = endDevice.getNames();
            if (names.size() > 1) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.UNSUPPORTED_LIST_SIZE, END_DEVICES_ITEM + ".Names", 1).get();
            }
            String name = names.stream()
                    .findFirst()
                    .map(Name::getName)
                    .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(
                            MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, END_DEVICES_ITEM));
            checkIsEmpty(name, END_DEVICES_ITEM + ".Names[0].name");
            deviceNames.add(name);
        } else {
            checkIsEmpty(mRID, END_DEVICES_ITEM + ".mRID");
            mRIDs.add(mRID);
        }
    }

    private void setUsagePointInfo(MeterReadingsBuilder builder, UsagePoint usagePoint) throws FaultMessage {
        String mRID = usagePoint.getMRID();
        if (mRID == null) {
            Set<String> names = extractNamesWithType(usagePoint.getNames(), UsagePointNameTypeEnum.USAGE_POINT_NAME);
            if (names.size() > 1) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.UNSUPPORTED_LIST_SIZE, USAGE_POINT_NAME_ITEMS, 1).get();
            }
            String name = names.stream()
                    .findFirst()
                    .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(
                            MessageSeeds.MISSING_MRID_OR_NAME_WITH_TYPE_FOR_ELEMENT,
                            UsagePointNameTypeEnum.USAGE_POINT_NAME.getNameType(), USAGE_POINT_ITEM));
            checkIsEmpty(name, USAGE_POINT_NAME);
            builder.fromUsagePointWithName(name);
        } else {
            checkIsEmpty(mRID, USAGE_POINT_MRID);
            builder.fromUsagePointWithMRID(mRID);
        }
    }

    private Set<String> extractNamesWithType(List<Name> names, UsagePointNameTypeEnum type) {
        return names.stream()
                .filter(name -> Optional.ofNullable(name.getNameType())
                        .map(NameType::getName)
                        .flatMap(Optional::ofNullable)
                        .map(String::trim)
                        .filter(type.getNameType()::equals)
                        .isPresent())
                .map(Name::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private MeterReadingsResponseMessageType createMeterReadingsResponseMessageType(MeterReadings meterReadings, String correlationId) {
        MeterReadingsResponseMessageType meterReadingsResponseMessageType = getMeterReadingsMessageObjectFactory.createMeterReadingsResponseMessageType();
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun(NOUN);
        header.setCorrelationID(correlationId);
        meterReadingsResponseMessageType.setHeader(header);
        MeterReadingsPayloadType meterReadingsPayloadType = getMeterReadingsMessageObjectFactory.createMeterReadingsPayloadType();
        meterReadingsPayloadType.setMeterReadings(meterReadings);
        meterReadingsResponseMessageType.setPayload(meterReadingsPayloadType);
        return meterReadingsResponseMessageType;
    }

    @Override
    public String getApplication(){
        return WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
    }
}
