/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.EndPointHelper;
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
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
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

public class ExecuteMeterReadingsEndpoint implements GetMeterReadingsPort {
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
    private final EndPointHelper endPointHelper;
    private final TransactionService transactionService;
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
                                        EndPointHelper endPointHelper,
                                        TransactionService transactionService,
                                        Clock clock,
                                        ServiceCallCommands serviceCallCommands,
                                        EndPointConfigurationService endPointConfigurationService,
                                        WebServicesService webServicesService, MeteringService meteringService,
                                        MetrologyConfigurationService metrologyConfigurationService) {
        this.readingBuilderProvider = readingBuilderProvider;
        this.replyTypeFactory = replyTypeFactory;
        this.faultMessageFactory = faultMessageFactory;
        this.endPointHelper = endPointHelper;
        this.transactionService = transactionService;
        this.clock = clock;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public MeterReadingsResponseMessageType getMeterReadings(GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            GetMeterReadings getMeterReadings = Optional.ofNullable(getMeterReadingsRequestMessage.getRequest().getGetMeterReadings())
                    .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, GET_METER_READINGS_ITEM));
            Boolean async = null;
            if (getMeterReadingsRequestMessage.getHeader() != null) {
                async = getMeterReadingsRequestMessage.getHeader().isAsyncReplyFlag();
            }
            checkGetMeterReading(getMeterReadings, async);

            // run async
            if (Boolean.TRUE.equals(async)) {
                return runAsyncMode(getMeterReadingsRequestMessage, context);
            }
            // run sync
            // -EndDevice
            List<EndDevice> endDevices = getMeterReadings.getEndDevice();
            MeterReadingsBuilder builder = readingBuilderProvider.get();
            if (endDevices != null && !endDevices.isEmpty()) {
                endDevices.stream().findFirst()
                        .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, END_DEVICE_LIST_ITEM));
                Set<String> notFoundEndDevices = new HashSet<>();
                List<com.elster.jupiter.metering.EndDevice> existedEndDevices =
                        setEndDevicesInfo(builder, endDevices.stream().limit(1).collect(Collectors.toList()), notFoundEndDevices);

                Set<String> notFoundReadingTypes = new HashSet<>();
                List<com.elster.jupiter.metering.ReadingType> existedReadingTypes =
                setReadingTypesInfo(builder, getMeterReadings.getReadingType(), notFoundReadingTypes);
                if (existedReadingTypes == null || existedReadingTypes.isEmpty()) {
                    throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES).get();
                }
                Set<ErrorType> errorTypes = getErrorTypes(notFoundEndDevices, existedEndDevices, notFoundReadingTypes, existedReadingTypes);
                if (endDevices.size() > 1) {
                    errorTypes.add(replyTypeFactory.errorType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, END_DEVICE_LIST_ITEM));
                }
                MeterReadings meterReadings = builder
                        .inTimeIntervals(getTimeIntervals(getMeterReadings.getReading()))
                        .build();
                context.commit();
                return getMeterReadingsResponseMessageType(meterReadings, errorTypes);
            }
            // -UsagePoint
            List<UsagePoint> usagePoints = getMeterReadings.getUsagePoint();
            UsagePoint usagePoint = usagePoints.stream().findFirst()
                    .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, USAGE_POINTS_LIST_ITEM));

            setUsagePointInfo(builder, usagePoint);
            setReadingTypesInfo(builder, getMeterReadings.getReadingType(), new HashSet<>());
            MeterReadings meterReadings = builder
                    .fromPurposes(extractNamesWithType(usagePoint.getNames(), UsagePointNameTypeEnum.PURPOSE))
                    .inTimeIntervals(getTimeIntervals(getMeterReadings.getReading()))
                    .build();
            context.commit();
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
    }

    private MeterReadingsResponseMessageType runAsyncMode(GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage,
                                                  TransactionContext context) throws FaultMessage {
        String replyAddress = getMeterReadingsRequestMessage.getHeader().getReplyAddress();
        isMissingElement(replyAddress, GET_METER_READINGS_ITEM +".Header.ReplyAddress");
        List<EndDevice> endDevices = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getEndDevice();
        List<Reading> readings = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getReading();
        List<ReadingType> readingTypes = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getReadingType();

        if (endDevices.isEmpty()) {
            throw(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, END_DEVICE_LIST_ITEM)).get();
        }

        Set<String> notFoundReadingTypes = new HashSet<>();
        List<com.elster.jupiter.metering.ReadingType> existedReadingTypes =
                setReadingTypesInfo(null, readingTypes, notFoundReadingTypes);
        if (existedReadingTypes == null || existedReadingTypes.isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES).get();
        }

        Set<String> notFoundEndDevices = new HashSet<>();
        List<com.elster.jupiter.metering.EndDevice> existedEndDevices = setEndDevicesInfo(null, endDevices,
                notFoundEndDevices);

        Set<ErrorType> errorTypes = getErrorTypes(notFoundEndDevices, existedEndDevices, notFoundReadingTypes, existedReadingTypes);
        publishOutboundEndPointConfiguration(replyAddress);
        for (Reading reading: readings) {
            getTimeInterval(reading, READING_LIST_ITEM);
            serviceCallCommands.createParentGetMeterReadingsServiceCall(reading.getSource(), replyAddress,
                    reading.getTimePeriod(), existedEndDevices, existedReadingTypes);
        }
        context.commit();

        /// no meter readings on sync reply! It's built in parent service call
        MeterReadings meterReadings = null;
        return getMeterReadingsResponseMessageType(meterReadings, errorTypes);
    }

    private MeterReadingsResponseMessageType getMeterReadingsResponseMessageType(MeterReadings meterReadings, Set<ErrorType> errorTypes) {
        MeterReadingsResponseMessageType meterReadingsResponseMessageType = createMeterReadingsResponseMessageType(meterReadings);
        meterReadingsResponseMessageType.setReply(errorTypes.isEmpty() ?
                replyTypeFactory.okReplyType() :
                replyTypeFactory.failureReplyType(ReplyType.Result.PARTIAL, errorTypes.stream().toArray(ErrorType[]::new)));
        return meterReadingsResponseMessageType;
    }

    private Set<ErrorType> getErrorTypes(Set<String> notFoundEndDevices, List<com.elster.jupiter.metering.EndDevice> existedEndDevices,
                                          Set<String> notFoundReadingTypes, List<com.elster.jupiter.metering.ReadingType> existedReadingTypes) {
        Set<ErrorType> errorTypes = new HashSet<>();
        if (!notFoundReadingTypes.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_NOT_FOUND,
                    combineNotFoundElementMessage(notFoundReadingTypes)));
        }
        if (!notFoundEndDevices.isEmpty()) {
            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.END_DEVICES_NOT_FOUND,
                    combineNotFoundElementMessage(notFoundEndDevices)));
        }
        Map<String, String> notFoundReadingTypesOnDevices = getNotFoundReadingTypesOnDevices(existedReadingTypes, existedEndDevices);
        if (!notFoundReadingTypesOnDevices.isEmpty()) {
            notFoundReadingTypesOnDevices
                    .forEach((device, readingTypesMessase) ->
                            errorTypes.add(replyTypeFactory.errorType(MessageSeeds.READING_TYPES_NOT_FOUND_ON_DEVICE, device, readingTypesMessase
                    )));
        }
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

    private void checkGetMeterReading(GetMeterReadings getMeterReadings, Boolean async) throws FaultMessage {
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

    private void checkSources(List<Reading> readings, Boolean async) throws FaultMessage {
        for (Reading reading: readings) {
            if (Boolean.TRUE.equals(async)) {
                checkAsyncSource(reading.getSource());
            } else {
                checkSyncSource(reading.getSource());
            }
        }
    }

    private void checkAsyncSource(String source) throws FaultMessage {
        if (!isApplicableSource(source)) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_VALUE, READING_LIST_ITEM + ".source", source,
                    new StringBuilder().append('\'')
                            .append(ReadingSourceEnum.SYSTEM.getSource()).append("\', \'")
                            .append(ReadingSourceEnum.METER.getSource()).append("\' or \'")
                            .append(ReadingSourceEnum.HYBRID.getSource()).append('\'').toString()
             ).get();
        }
    }

    private void checkSyncSource(String source) throws FaultMessage {
        if (!isMissingElement(source, READING_LIST_ITEM + ".source")
                && !ReadingSourceEnum.SYSTEM.getSource().equals(source)) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_VALUE, READING_LIST_ITEM + ".source", source,
                    ReadingSourceEnum.SYSTEM.getSource()
            ).get();
        }
    }

    private boolean isApplicableSource(String source) throws FaultMessage {
        isMissingElement(source ,READING_LIST_ITEM + ".source");
        return source.equals(ReadingSourceEnum.SYSTEM.getSource())
                || source.equals(ReadingSourceEnum.METER.getSource())
                || source.equals(ReadingSourceEnum.HYBRID.getSource());

    }

    private boolean isMissingElement(String element, String elementName) throws FaultMessage {
        checkElement(element, MessageSeeds.MISSING_ELEMENT, elementName);
        return false;
    }

    private boolean isEmptyElement(String element, String elementName) throws FaultMessage {
        checkElement( element, MessageSeeds.EMPTY_ELEMENT, elementName);
        return false;
    }

    private void checkElement(String element, MessageSeeds messageSeeds, String elementName) throws FaultMessage {
        if (Checks.is(element).emptyOrOnlyWhiteSpace()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                    messageSeeds, elementName).get();
        }
    }

    private Map<String, String> getNotFoundReadingTypesOnDevices(List<com.elster.jupiter.metering.ReadingType> existedReadingTypes,
                                                                 List<com.elster.jupiter.metering.EndDevice> existedEndDevices) {
        Map<String, String> notFoundReadingTypesOnDevices = new HashMap<>();

        for (com.elster.jupiter.metering.EndDevice endDevice: existedEndDevices){
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
                .select(where("MRID").in(mRIDs).or(where("NAME").in(names)));
        if (existedEndDevices == null || existedEndDevices.isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_END_DEVICES).get();
        }
        return existedEndDevices;
    }

    private List<com.elster.jupiter.metering.ReadingType> getReadingTypes(Set<String> readingTypesMRIDs,
                                                                          Set<String> readingTypesNames)throws FaultMessage {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        Condition condition = filter.getCondition();
        condition = condition.and(where("mRID").in(new ArrayList<>(readingTypesMRIDs)))
                             .or(where("fullAliasName").in(new ArrayList<>(readingTypesNames)));
        filter.addCondition(condition);
        List<com.elster.jupiter.metering.ReadingType> readingTypes = meteringService.findReadingTypes(filter).find();
        if (readingTypes == null || readingTypes.isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES).get();
        }
        return readingTypes;
    }

    private String combineNotFoundElementMessage(Set<String> notFoundElements) {
        return notFoundElements.stream().collect(Collectors.joining(", "));
    }

    private List<com.elster.jupiter.metering.EndDevice> setEndDevicesInfo(MeterReadingsBuilder builder,
                                                                              List<EndDevice> endDevices,
                                                                              Set<String> notFoundEndDevices) throws FaultMessage {
        List<String> mRIDs = new ArrayList<>();
        List<String> fullAliasNames =  new ArrayList<>();
        for (int i = 0; i < endDevices.size(); ++i) {
            setEndDeviceInfo(endDevices.get(i), i, mRIDs, fullAliasNames);
        }

        List<com.elster.jupiter.metering.EndDevice> endDeviceList = fromEndDevicesWithMRIDsAndNames(mRIDs, fullAliasNames);
        notFoundEndDevices.addAll(getNotFoundEndDevices(endDeviceList, mRIDs, fullAliasNames));
        if (builder != null) {
            builder.withEndDevices(endDeviceList);
        }
        return endDeviceList;
    }

    private Set<String> getNotFoundEndDevices(List<com.elster.jupiter.metering.EndDevice> endDeviceList,
                                                List<String> mRIDs, List<String>name) {
        Set<String> nameOrMRIDList = new HashSet<>();
        nameOrMRIDList.addAll(name);
        nameOrMRIDList.addAll(mRIDs);
        return nameOrMRIDList.stream()
                .filter(identifier ->
                        !endDeviceList.stream()
                                .map(readingType -> readingType.getName())
                                .collect(Collectors.toList())
                                .contains(identifier)
                )
                .filter(identifier ->
                        !endDeviceList.stream()
                                .map(readingType -> readingType.getMRID())
                                .collect(Collectors.toList())
                                .contains(identifier)
                )
                .collect(Collectors.toSet());
    }

    private List<com.elster.jupiter.metering.ReadingType> setReadingTypesInfo(MeterReadingsBuilder builder,
                                                                              List<ReadingType> readingTypes,
                                                                              Set<String> notFoundReadingTypes) throws FaultMessage {
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
        notFoundReadingTypes.addAll(getNotFoundReadingTypes(readingTypeList, mRIDs, fullAliasNames));
        return readingTypeList;
    }

    private Set<String> getNotFoundReadingTypes(List<com.elster.jupiter.metering.ReadingType> readingTypeList,
                                                Set<String> mRIDs, Set<String>fullAliasNames) {
        Set<String> nameOrMRIDList = new HashSet<>();
        nameOrMRIDList.addAll(fullAliasNames);
        nameOrMRIDList.addAll(mRIDs);
        return nameOrMRIDList.stream()
                .filter(identifier ->
                    !readingTypeList.stream()
                            .map(readingType -> readingType.getFullAliasName())
                            .collect(Collectors.toList())
                            .contains(identifier)
                )
                .filter(identifier ->
                    !readingTypeList.stream()
                            .map(readingType -> readingType.getMRID())
                            .collect(Collectors.toList())
                            .contains(identifier)
                )
                .collect(Collectors.toSet());
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
            isEmptyElement(name, READING_TYPE_ITEM + ".Names[0].name");
            fullAliasNames.add(name);
        } else {
            isEmptyElement(mRID, READING_TYPE_ITEM + ".mRID");
            mRIDs.add(mRID);
        }
    }

    private RangeSet<Instant> getTimeIntervals(List<Reading> readings) throws FaultMessage {
        if (readings.isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, READING_LIST_ITEM).get();
        }
        RangeSet<Instant> result = TreeRangeSet.create();
        for (int i = 0; i < readings.size(); ++i) {
            final String READING_ITEM = READING_LIST_ITEM + '[' + i + ']';
            result.add(getTimeInterval(readings.get(i), READING_ITEM));
        }
        return result;
    }

    private Range<Instant> getTimeInterval(Reading reading, String READING_ITEM) throws FaultMessage {
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

    private void setEndDeviceInfo(EndDevice endDevice, int index, List<String> mRIDs, List<String> deviceNames) throws FaultMessage {
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
            isEmptyElement(name,  END_DEVICES_ITEM + ".Names[0].name");
            deviceNames.add(name);
        } else {
            isEmptyElement(mRID, END_DEVICES_ITEM + ".mRID");
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
            isEmptyElement(name,  USAGE_POINT_NAME);
            builder.fromUsagePointWithName(name);
        } else {
            isEmptyElement(mRID, USAGE_POINT_MRID);
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
}
