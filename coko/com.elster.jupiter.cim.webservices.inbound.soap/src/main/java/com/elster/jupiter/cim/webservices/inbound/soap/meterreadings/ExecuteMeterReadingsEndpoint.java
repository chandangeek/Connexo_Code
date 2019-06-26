/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;

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
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
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
    private static final String USAGE_POINTS_LIST_ITEM = GET_METER_READINGS_ITEM + ".UsagePoint";
    private static final String END_DEVICE_LIST_ITEM = GET_METER_READINGS_ITEM + ".EndDevice";
    private static final String USAGE_POINT_ITEM = USAGE_POINTS_LIST_ITEM + "[0]";
    private static final String USAGE_POINT_MRID = USAGE_POINT_ITEM + ".mRID";
    private static final String USAGE_POINT_NAME_ITEMS = USAGE_POINT_ITEM
            + ".Names[?(@.NameType.name=='" + UsagePointNameTypeEnum.USAGE_POINT_NAME.getNameType() + "')]";
    private static final String USAGE_POINT_NAME = USAGE_POINT_NAME_ITEMS + ".name";

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
    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public ExecuteMeterReadingsEndpoint(Provider<MeterReadingsBuilder> readingBuilderProvider,
                                        ReplyTypeFactory replyTypeFactory,
                                        MeterReadingFaultMessageFactory faultMessageFactory,
                                        Clock clock,
                                        ServiceCallCommands serviceCallCommands,
                                        EndPointConfigurationService endPointConfigurationService,
                                        WebServicesService webServicesService, MeteringService meteringService,
                                        MetrologyConfigurationService metrologyConfigurationService) {
        this.readingBuilderProvider = readingBuilderProvider;
        this.replyTypeFactory = replyTypeFactory;
        this.faultMessageFactory = faultMessageFactory;
        this.clock = clock;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public MeterReadingsResponseMessageType getMeterReadings(GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage) throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            try {
                GetMeterReadings getMeterReadings = Optional.ofNullable(getMeterReadingsRequestMessage.getRequest().getGetMeterReadings())
                        .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, GET_METER_READINGS_ITEM));
                boolean async = false;
                if (getMeterReadingsRequestMessage.getHeader() != null) {
                    async = getMeterReadingsRequestMessage.getHeader().isAsyncReplyFlag();
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
                if (endDevices != null && !endDevices.isEmpty()) {
                    Set<String> notFoundDeviceMRIDs = new HashSet<>();
                    Set<String> notFoundDeviceNames = new HashSet<>();
                    List<com.elster.jupiter.metering.EndDevice> existedEndDevices =
                            getExistedEndDevices(endDevices.stream().limit(1).collect(Collectors.toList()),
                                    notFoundDeviceMRIDs, notFoundDeviceNames);
                    builder.withEndDevices(existedEndDevices);

                    Set<String> notFoundRTMRIDs = new HashSet<>();
                    Set<String> notFoundRTNames = new HashSet<>();
                    List<com.elster.jupiter.metering.ReadingType> existedReadingTypes =
                            setReadingTypesInfo(builder, getMeterReadings.getReadingType(), notFoundRTMRIDs, notFoundRTNames);
                    if (existedReadingTypes == null || existedReadingTypes.isEmpty()) {
                        throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES).get();
                    }
                    Set<ErrorType> errorTypes = getErrorTypes(notFoundDeviceMRIDs, notFoundDeviceNames, existedEndDevices,
                            notFoundRTMRIDs, notFoundRTNames, existedReadingTypes);
                    if (endDevices.size() > 1) {
                        errorTypes.add(replyTypeFactory.errorType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, END_DEVICE_LIST_ITEM));
                    }
                    MeterReadings meterReadings = builder
                            .inTimeIntervals(getTimeIntervals(getMeterReadings.getReading()))
                            .build();
                    return createMeterReadingsResponseMessageType(meterReadings, errorTypes);
                }
                // -UsagePoint
                List<UsagePoint> usagePoints = getMeterReadings.getUsagePoint();
                UsagePoint usagePoint = usagePoints.stream().findFirst()
                        .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, USAGE_POINTS_LIST_ITEM));

                setUsagePointInfo(builder, usagePoint);
                setReadingTypesInfo(builder, getMeterReadings.getReadingType(), new HashSet<>(), new HashSet<>());
                MeterReadings meterReadings = builder
                        .fromPurposes(extractNamesWithType(usagePoint.getNames(), UsagePointNameTypeEnum.PURPOSE))
                        .inTimeIntervals(getTimeIntervals(getMeterReadings.getReading()))
                        .build();
                MeterReadingsResponseMessageType meterReadingsResponseMessageType = createMeterReadingsResponseMessageType(meterReadings);
                meterReadingsResponseMessageType.setReply(usagePoints.size() > 1 ?
                        replyTypeFactory.partialFailureReplyType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, USAGE_POINTS_LIST_ITEM) :
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
        String replyAddress = getMeterReadingsRequestMessage.getHeader().getReplyAddress();
        checkIfMissingOrIsEmpty(replyAddress, GET_METER_READINGS_ITEM +".Header.ReplyAddress");
        List<EndDevice> endDevices = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getEndDevice();
        List<Reading> readings = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getReading();
        List<ReadingType> readingTypes = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getReadingType();

        if (endDevices.isEmpty()) {
            throw(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, END_DEVICE_LIST_ITEM)).get();
        }
        Set<String> notFoundRTMRIDs = new HashSet<>();
        Set<String> notFoundRTNames = new HashSet<>();
        List<com.elster.jupiter.metering.ReadingType> existedReadingTypes =
                setReadingTypesInfo(null, readingTypes, notFoundRTMRIDs, notFoundRTNames);
        if (existedReadingTypes == null || existedReadingTypes.isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES).get();
        }

        Set<String> notFoundDevicesMRIDs = new HashSet<>();
        Set<String> notFoundDevicesNames = new HashSet<>();
        List<com.elster.jupiter.metering.EndDevice> existedEndDevices = getExistedEndDevices(endDevices, notFoundDevicesMRIDs,
                                                                                             notFoundDevicesNames);

        Set<ErrorType> errorTypes = getErrorTypes(notFoundDevicesMRIDs, notFoundDevicesNames, existedEndDevices,
                                                  notFoundRTMRIDs, notFoundRTNames, existedReadingTypes);
        publishOutboundEndPointConfiguration(replyAddress);
        for (int i = 0; i < readings.size(); ++i) {
            Reading reading = readings.get(i);
            getTimeInterval(reading, i);
            serviceCallCommands.createParentGetMeterReadingsServiceCall(reading.getSource(), replyAddress,
                    reading.getTimePeriod(), existedEndDevices, existedReadingTypes);
        }

        /// no meter readings on sync reply! It's built in parent service call
        MeterReadings meterReadings = null;
        return createMeterReadingsResponseMessageType(meterReadings, errorTypes);
    }

    private MeterReadingsResponseMessageType createMeterReadingsResponseMessageType(MeterReadings meterReadings, Set<ErrorType> errorTypes) {
        MeterReadingsResponseMessageType meterReadingsResponseMessageType = createMeterReadingsResponseMessageType(meterReadings);
        meterReadingsResponseMessageType.setReply(errorTypes.isEmpty() ?
                replyTypeFactory.okReplyType() :
                replyTypeFactory.failureReplyType(ReplyType.Result.PARTIAL, errorTypes.stream().toArray(ErrorType[]::new)));
        return meterReadingsResponseMessageType;
    }

    private Set<ErrorType> getErrorTypes(Set<String> notFoundMRIDs, Set<String> notFoundNames,
                                         List<com.elster.jupiter.metering.EndDevice> existedEndDevices,
                                         Set<String> notFoundRTMRIDs, Set<String> notFoundRTNames,
                                         List<com.elster.jupiter.metering.ReadingType> existedReadingTypes) {
        Set<ErrorType> errorTypes = new HashSet<>();
        // reading types issue
        if (!notFoundRTMRIDs.isEmpty() && !notFoundRTNames.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_NOT_FOUND_IN_THE_SYSTEM,
                    combineNotFoundElementMessage(notFoundRTMRIDs),
                    combineNotFoundElementMessage(notFoundRTNames)));
        } else if (!notFoundRTMRIDs.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_WITH_MRID_NOT_FOUND,
                    combineNotFoundElementMessage(notFoundRTMRIDs)));
        } else if (!notFoundRTNames.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_WITH_NAME_NOT_FOUND,
                    combineNotFoundElementMessage(notFoundRTNames)));
        }

        // devices issue
        if (!notFoundMRIDs.isEmpty() && !notFoundNames.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.END_DEVICES_NOT_FOUND,
                    combineNotFoundElementMessage(notFoundMRIDs),
                    combineNotFoundElementMessage(notFoundNames)));
        } else if(!notFoundMRIDs.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.END_DEVICES_WITH_MRID_NOT_FOUND,
                    combineNotFoundElementMessage(notFoundMRIDs)));
        } else if(!notFoundNames.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.END_DEVICES_WITH_NAME_NOT_FOUND,
                    combineNotFoundElementMessage(notFoundNames)));
        }
        Map<String, String> notFoundReadingTypesOnDevices = getNotFoundReadingTypesOnDevices(existedReadingTypes, existedEndDevices);
        notFoundReadingTypesOnDevices
                .forEach((device, readingTypesMessage) ->
                        errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_NOT_FOUND_ON_DEVICE, device, readingTypesMessage
                )));
        return errorTypes;
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
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL, url).get();
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
            throw(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, READING_LIST_ITEM).get());
        }
        if (getMeterReadings.getReadingType().isEmpty()) {
            throw(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, READING_TYPES_LIST_ITEM).get());
        }
        checkSources(getMeterReadings.getReading(), async);
    }

    private void checkSources(List<Reading> readings, boolean async) throws FaultMessage {
        for (int i = 0; i < readings.size(); ++i) {
            if (async) {
                checkAsyncSource(readings.get(i).getSource(), i);
            } else {
                checkSyncSource(readings.get(i).getSource(), i);
            }
        }
    }

    private void checkAsyncSource(String source, int index) throws FaultMessage {
        if (!isApplicableSource(source)) {
            final String READING_ITEM = READING_LIST_ITEM + '[' + index + ']';
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_VALUE, READING_ITEM + ".source", source,
                    new StringBuilder().append('\'')
                            .append(ReadingSourceEnum.SYSTEM.getSource()).append("\', \'")
                            .append(ReadingSourceEnum.METER.getSource()).append("\' or \'")
                            .append(ReadingSourceEnum.HYBRID.getSource()).append('\'').toString()
             ).get();
        }
    }

    private void checkSyncSource(String source, int index) throws FaultMessage {
        checkIfMissingOrIsEmpty(source, READING_LIST_ITEM + ".source");
        if (!ReadingSourceEnum.SYSTEM.getSource().equals(source)) {
            final String READING_ITEM = READING_LIST_ITEM + '[' + index + ']';
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_VALUE, READING_ITEM + ".source", source,
                    ReadingSourceEnum.SYSTEM.getSource()
            ).get();
        }
    }

    private boolean isApplicableSource(String source) throws FaultMessage {
        checkIfMissingOrIsEmpty(source ,READING_LIST_ITEM + ".source");
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

    private Map<String, String> getNotFoundReadingTypesOnDevices(List<com.elster.jupiter.metering.ReadingType> existedReadingTypes,
                                                                 List<com.elster.jupiter.metering.EndDevice> existedEndDevices) {
        Map<String, String> notFoundReadingTypesOnDevices = new HashMap<>();

        for (com.elster.jupiter.metering.EndDevice endDevice: existedEndDevices) {
            Set<String> notFoundReadingTypes = new HashSet<>();
            existedReadingTypes.forEach(readingType -> {
                Meter meter = (Meter) endDevice;
                boolean isReadingTypePresent = false;
                for (ChannelsContainer channelsContainer: meter.getChannelsContainers()) {
                    if (channelsContainer.getChannel(readingType).isPresent()) {
                        isReadingTypePresent = true;
                    }
                }
                if (!isReadingTypePresent) {
                    notFoundReadingTypes.add(readingType.getMRID());
                }
            });
            if (!notFoundReadingTypes.isEmpty()) {
                notFoundReadingTypesOnDevices.put(endDevice.getName(), combineNotFoundElementMessage(notFoundReadingTypes));
            }

        }
        return notFoundReadingTypesOnDevices;
    }

    private List<com.elster.jupiter.metering.EndDevice> fromEndDevicesWithMRIDsAndNames(List<String> mRIDs, List<String> names) throws FaultMessage {
        List<com.elster.jupiter.metering.EndDevice> existedEndDevices = meteringService.getEndDeviceQuery()
                .select(where("mRID").in(mRIDs).or(where("name").in(names)));
        if (existedEndDevices == null || existedEndDevices.isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_END_DEVICES).get();
        }
        return existedEndDevices;
    }

    private List<com.elster.jupiter.metering.ReadingType> getReadingTypes(Set<String> readingTypesMRIDs,
                                                                          Set<String> readingTypesNames)throws FaultMessage {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        Condition condition = filter.getCondition().and(where("mRID").in(new ArrayList<>(readingTypesMRIDs)))
                .or(where("fullAliasName").in(new ArrayList<>(readingTypesNames)));
        filter.addCondition(condition);
        List<com.elster.jupiter.metering.ReadingType> readingTypes = meteringService.findReadingTypes(filter).find();
        if (readingTypes == null || readingTypes.isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES).get();
        }
        return readingTypes;
    }

    private String combineNotFoundElementMessage(Set<String> notFoundElements) {
        return notFoundElements.stream().sorted().collect(Collectors.joining(", "));
    }

    private List<com.elster.jupiter.metering.EndDevice> getExistedEndDevices(List<EndDevice> endDevices, Set<String> notFoundMRIDs,
                                                                             Set<String> notFoundNames) throws FaultMessage {
        List<String> mRIDs = new ArrayList<>();
        List<String> fullAliasNames = new ArrayList<>();
        for (int i = 0; i < endDevices.size(); ++i) {
            collectDeviceMridsAndNames(endDevices.get(i), i, mRIDs, fullAliasNames);
        }

        List<com.elster.jupiter.metering.EndDevice> endDeviceList = fromEndDevicesWithMRIDsAndNames(mRIDs, fullAliasNames);
        fillNotFoundEndDevicesMRIDsAndNames(endDeviceList, mRIDs, fullAliasNames, notFoundMRIDs, notFoundNames);
        return endDeviceList;
    }

    private void fillNotFoundEndDevicesMRIDsAndNames(List<com.elster.jupiter.metering.EndDevice> endDeviceList,
                                                     List<String> requiredMRIDs, List<String> requiredNames,
                                                     Set<String> notFoundMRIDs, Set<String> notFoundNames) {
        Set<String> existedNames = endDeviceList.stream()
                .map(endDevice -> endDevice.getName())
                .collect(Collectors.toSet());
        Set<String> existedmRIDs = endDeviceList.stream()
                .map(endDevice -> endDevice.getMRID())
                .collect(Collectors.toSet());
        notFoundMRIDs.addAll(requiredMRIDs.stream()
                .filter(mrid -> !existedmRIDs.contains(mrid))
                .collect(Collectors.toSet()));
        notFoundNames.addAll(requiredNames.stream()
                .filter(name -> !existedNames.contains(name))
                .collect(Collectors.toSet()));
    }

    private List<com.elster.jupiter.metering.ReadingType> setReadingTypesInfo(MeterReadingsBuilder builder,
                                                                              List<ReadingType> readingTypes,
                                                                              Set<String> notFoundMRIDs,
                                                                              Set<String> notFoundNames) throws FaultMessage {
        Set<String> mRIDs = new HashSet<>();
        Set<String> fullAliasNames = new HashSet<>();
        for (int i = 0; i < readingTypes.size(); ++i) {
            setReadingTypeInfo(readingTypes.get(i), i, mRIDs, fullAliasNames);
        }
        if (builder != null) {
            builder.ofReadingTypesWithMRIDs(mRIDs);
            builder.ofReadingTypesWithFullAliasNames(fullAliasNames);
        }
        List<com.elster.jupiter.metering.ReadingType> readingTypeList = getReadingTypes(mRIDs, fullAliasNames);
        fillNotFoundReadingTypesMRIDsAndNames(readingTypeList, mRIDs, fullAliasNames, notFoundMRIDs, notFoundNames);
        return readingTypeList;
    }

    private void fillNotFoundReadingTypesMRIDsAndNames(List<com.elster.jupiter.metering.ReadingType> readingTypeList,
                                                       Set<String> requiredMRIDs, Set<String>requiredNames,
                                                       Set<String> notFoundMRIDs, Set<String> notFoundNames) {
        Set<String> existedmRIDs = readingTypeList.stream()
                .map(readingType -> readingType.getMRID())
                .collect(Collectors.toSet());
        Set<String> existedNames = readingTypeList.stream()
                .map(readingType -> readingType.getFullAliasName())
                .collect(Collectors.toSet());
        notFoundMRIDs.addAll(requiredMRIDs.stream()
                .filter(mrid -> !existedmRIDs.contains(mrid))
                .collect(Collectors.toSet()));
        notFoundNames.addAll(requiredNames.stream()
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
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, READING_LIST_ITEM).get();
        }
        RangeSet<Instant> result = TreeRangeSet.create();
        for (int i = 0; i < readings.size(); ++i) {
            result.add(getTimeInterval(readings.get(i), i));
        }
        return result;
    }

    private Range<Instant> getTimeInterval(Reading reading, int index) throws FaultMessage {
        final String READING_ITEM = READING_LIST_ITEM + '[' + index + ']';
        DateTimeInterval interval = Optional.ofNullable(reading.getTimePeriod())
                .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT,READING_ITEM + ".timePeriod"));
        Instant start = Optional.ofNullable(interval.getStart())
                .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT,READING_ITEM + ".timePeriod.start"));
        Instant end = interval.getEnd();
        if (end == null) {
            end = clock.instant();
            interval.setEnd(end);
        }
        if (!end.isAfter(start)) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                    MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD,
                    XsdDateTimeConverter.marshalDateTime(start),
                    XsdDateTimeConverter.marshalDateTime(end)).get();
        }
        return Range.openClosed(start, end);
    }

    private void collectDeviceMridsAndNames(EndDevice endDevice, int index, List<String> mRIDs, List<String> deviceNames) throws FaultMessage {
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
            checkIsEmpty(name,  END_DEVICES_ITEM + ".Names[0].name");
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
            checkIsEmpty(name,  USAGE_POINT_NAME);
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

    private MeterReadingsResponseMessageType createMeterReadingsResponseMessageType(MeterReadings meterReadings) {
        MeterReadingsResponseMessageType meterReadingsResponseMessageType = getMeterReadingsMessageObjectFactory.createMeterReadingsResponseMessageType();
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun(NOUN);
        meterReadingsResponseMessageType.setHeader(header);
        MeterReadingsPayloadType meterReadingsPayloadType = getMeterReadingsMessageObjectFactory.createMeterReadingsPayloadType();
        meterReadingsPayloadType.setMeterReadings(meterReadings);
        meterReadingsResponseMessageType.setPayload(meterReadingsPayloadType);
        return meterReadingsResponseMessageType;
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

}
