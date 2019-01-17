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
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;

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
import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ExecuteMeterReadingsEndpoint implements GetMeterReadingsPort {
    private static final String NOUN = "MeterReadings";
    private static final String GET_METER_READINGS_ITEM = "GetMeterReadings";
    private static final String READING_TYPES_LIST_ITEM = GET_METER_READINGS_ITEM + ".ReadingType";
    private static final String READING_LIST_ITEM = GET_METER_READINGS_ITEM + ".Reading";
    private static final String USAGE_POINTS_LIST_ITEM = GET_METER_READINGS_ITEM + ".UsagePoint";
    private static final String END_DEVICE_LIST_ITEM = GET_METER_READINGS_ITEM + ".EndDevice";
    private static final String USAGE_POINT_ITEM = USAGE_POINTS_LIST_ITEM + "[0]";
    private static final String END_DEVICE_ITEM = END_DEVICE_LIST_ITEM + "[0]";
    private static final String USAGE_POINT_MRID = USAGE_POINT_ITEM + ".mRID";
    private static final String END_DEVICE_MRID = END_DEVICE_ITEM + ".mRID";
    private static final String USAGE_POINT_NAME_ITEMS = USAGE_POINT_ITEM
            + ".Names[?(@.NameType.name=='" + UsagePointNameTypeEnum.USAGE_POINT_NAME.getNameType() + "')]";
    private static final String USAGE_POINT_NAME = USAGE_POINT_NAME_ITEMS + ".name";
    private static final String END_DEVICE_NAME = END_DEVICE_ITEM + ".name";

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

    @Inject
    public ExecuteMeterReadingsEndpoint(Provider<MeterReadingsBuilder> readingBuilderProvider,
                                        ReplyTypeFactory replyTypeFactory,
                                        MeterReadingFaultMessageFactory faultMessageFactory,
                                        EndPointHelper endPointHelper,
                                        TransactionService transactionService,
                                        Clock clock,
                                        ServiceCallCommands serviceCallCommands,
                                        EndPointConfigurationService endPointConfigurationService,
                                        WebServicesService webServicesService) {
        this.readingBuilderProvider = readingBuilderProvider;
        this.replyTypeFactory = replyTypeFactory;
        this.faultMessageFactory = faultMessageFactory;
        this.endPointHelper = endPointHelper;
        this.transactionService = transactionService;
        this.clock = clock;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
    }

    @Override
    public MeterReadingsResponseMessageType getMeterReadings(GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            /// TODO noDevice found WS13005
            /// TODO Reading type(s) haven't been found for device WS13006
            GetMeterReadings getMeterReadings = Optional.ofNullable(getMeterReadingsRequestMessage.getRequest().getGetMeterReadings())
                    .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, GET_METER_READINGS_ITEM));
            checkGetMeterReading(getMeterReadings);

            MeterReadingsBuilder builder = readingBuilderProvider.get();

            // run async
            if (Boolean.TRUE.equals(getMeterReadingsRequestMessage.getHeader().isAsyncReplyFlag())) {
                return runAsyncMode(getMeterReadingsRequestMessage, builder, context);
            } else {
                // run sync
                // -EndDevice
                List<EndDevice> endDevices = getMeterReadings.getEndDevice();
                if (endDevices != null && !endDevices.isEmpty()) {

                    EndDevice endDevice = endDevices.stream().findFirst()
                            .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, END_DEVICE_LIST_ITEM));
                    setEndDeviceInfo(builder, endDevice);
                    setReadingTypesInfo(builder, getMeterReadings.getReadingType());
                    MeterReadings meterReadings = builder
                            .inTimeIntervals(getTimeIntervals(getMeterReadings.getReading()))
                            .build();
                    MeterReadingsResponseMessageType meterReadingsResponseMessageType = createMeterReadingsResponseMessageType(meterReadings);
                    meterReadingsResponseMessageType.setReply(endDevices.size() > 1 ?
                            replyTypeFactory.partialFailureReplyType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, END_DEVICE_LIST_ITEM) :
                            replyTypeFactory.okReplyType());
                    return meterReadingsResponseMessageType;
                }
                // -UsagePoint
                List<UsagePoint> usagePoints = getMeterReadings.getUsagePoint();
                UsagePoint usagePoint = usagePoints.stream().findFirst()
                        .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.EMPTY_LIST, USAGE_POINTS_LIST_ITEM));

                setUsagePointInfo(builder, usagePoint);
                setReadingTypesInfo(builder, getMeterReadings.getReadingType());
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
            }

        } catch (VerboseConstraintViolationException e) {
            throw faultMessageFactory.createMeterReadingFaultMessage(e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw faultMessageFactory.createMeterReadingFaultMessage(e.getLocalizedMessage(), e.getErrorCode());
        }
    }


    MeterReadingsResponseMessageType runAsyncMode(GetMeterReadingsRequestMessageType getMeterReadingsRequestMessage, MeterReadingsBuilder builder, TransactionContext context) throws FaultMessage {
        List<EndDevice> endDevices = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getEndDevice();
        List<Reading> readings = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getReading();
        List<ReadingType> readingTypes = getMeterReadingsRequestMessage.getRequest().getGetMeterReadings().getReadingType();
        String replyAddress = getReplyAddress(getMeterReadingsRequestMessage.getHeader());
        EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(replyAddress);
        for (Reading reading: readings) {
            checkSource(reading);
            String source = reading.getSource();
            DateTimeInterval timePeriod = reading.getTimePeriod();

            createGetMeterReadingsServiceCallAndTransition(source, replyAddress, timePeriod, readingTypes,  endDevices);
        }
        context.commit();

        builder.fromEndDevicesWithMRIDsAndNames(endDevices);
        String warning = combineNotFoundEndDeviceMessage(endDevices);

        /// no readings on sync reply! readings must built in parent service call
        MeterReadings meterReadings = null;
        MeterReadingsResponseMessageType meterReadingsResponseMessageType = createMeterReadingsResponseMessageType(meterReadings);
        meterReadingsResponseMessageType.setReply(!warning.isEmpty() ?
                replyTypeFactory.partialFailureReplyType(MessageSeeds.END_DEVICES_NOT_FOUND, warning) :
                replyTypeFactory.okReplyType());
        return meterReadingsResponseMessageType;
    }

    private ServiceCall createGetMeterReadingsServiceCallAndTransition(String source, String replyAddress, DateTimeInterval timePeriod,
                                                                       List<ReadingType> readingTypes, List<EndDevice> endDevices) throws FaultMessage {
        ServiceCall serviceCall = serviceCallCommands.createParentGetMeterReadingsServiceCall(source, replyAddress, timePeriod, readingTypes, endDevices);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        return serviceCall;
    }

    private EndPointConfiguration getOutboundEndPointConfiguration(String url) throws FaultMessage {
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
        return endPointConfig;
    }

    private String getReplyAddress(HeaderType header) throws FaultMessage {
        String replyAddress = header.getReplyAddress();
        if (Checks.is(replyAddress).emptyOrOnlyWhiteSpace()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, "ReplyAddress").get();
        }
        return replyAddress;
    }

    private void checkGetMeterReading(GetMeterReadings getMeterReadings) throws FaultMessage {
        if (!getMeterReadings.getEndDeviceGroup().isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_ELEMENT, "EndDeviceGroup", GET_METER_READINGS_ITEM)
                    .get();
        }
        if (!getMeterReadings.getUsagePointGroup().isEmpty()) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_ELEMENT, "UsagePointGroup", GET_METER_READINGS_ITEM)
                    .get();
        }
        checkSources(getMeterReadings.getReading());
    }

    private void checkSources(List<Reading> readings) throws FaultMessage {
        for (Reading reading: readings) {
            checkSource(reading);
        }
    }

    private void checkSource(Reading reading) throws FaultMessage {
        String source = reading.getSource();
        /// FIXME https://jira.eict.vpdc/browse/CXO-9423
        if (source != null && !source.equals(ReadingSourceEnum.SYSTEM.getSource())) {
            /// FIXME use WS13004?
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_VALUE, READING_LIST_ITEM + ".source", source,
                    new StringBuilder().append('\'')
                            .append(ReadingSourceEnum.SYSTEM.getSource()).append("\', \'")
                            .append(ReadingSourceEnum.METER.getSource()).append("\' or \'")
                            .append(ReadingSourceEnum.HYBRID.getSource()).append('\'').toString()
             ).get();
        }
    }

    ///TODO separate mRID and names?
    private String combineNotFoundEndDeviceMessage(List<EndDevice> endDevices) {
        StringBuilder message = new StringBuilder();
        endDevices.stream().forEach(dev -> {
            if (!Strings.isNullOrEmpty(dev.getMRID())) {
                message.append(dev.getMRID()).append(",");
                return;
            }
            dev.getNames().stream().findFirst().map(Name :: getName).ifPresent(mes -> message.append(mes).append(", "));
        });
        return message.length() > 0 ? message.deleteCharAt(message.length()-2).toString() : message.toString();
    }

    private void setReadingTypesInfo(MeterReadingsBuilder builder, List<ReadingType> readingTypes) throws FaultMessage {
        Set<String> mRIDs = new HashSet<>();
        Set<String> fullAliasNames = new HashSet<>();
        for (int i = 0; i < readingTypes.size(); ++i) {
            setReadingTypeInfo(readingTypes.get(i), i, mRIDs, fullAliasNames);
        }
        builder.ofReadingTypesWithMRIDs(mRIDs);
        builder.ofReadingTypesWithFullAliasNames(fullAliasNames);
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
            if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.EMPTY_ELEMENT, READING_TYPE_ITEM + ".Names[0].name").get();
            }
            fullAliasNames.add(name);
        } else {
            if (Checks.is(mRID).emptyOrOnlyWhiteSpace()) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.EMPTY_ELEMENT, READING_TYPE_ITEM + ".mRID").get();
            }
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
        /// TODO extract to separate method and run this check in the beginning of process
        String source = reading.getSource();
        /// FIXME https://jira.eict.vpdc/browse/CXO-9423
        if (source != null && !source.equals(ReadingSourceEnum.SYSTEM.getSource())) {
            /// FIXME use WS13004?
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.UNSUPPORTED_VALUE, READING_ITEM + ".source", source,
                    '\'' + ReadingSourceEnum.SYSTEM.getSource() + '\'' +
                    '\'' + ReadingSourceEnum.METER.getSource() + '\''+
                    '\'' + ReadingSourceEnum.HYBRID.getSource() + '\'').get();
        }
        DateTimeInterval interval = reading.getTimePeriod();
        if (interval == null) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                    MessageSeeds.MISSING_ELEMENT, READING_ITEM + ".timePeriod").get();
        }
        Instant start = interval.getStart();
        if (start == null) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                    MessageSeeds.MISSING_ELEMENT, READING_ITEM + ".timePeriod.start").get();
        }
        Instant end = interval.getEnd();
        if (end == null) {
            end = clock.instant();
        }
        if (!end.isAfter(start)) {
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                    MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD,
                    XsdDateTimeConverter.marshalDateTime(start),
                    XsdDateTimeConverter.marshalDateTime(end)).get();
        }
        return Range.openClosed(start, end);
    }

    private void setEndDeviceInfo(MeterReadingsBuilder builder, EndDevice endDevice) throws FaultMessage {
        String mRID = endDevice.getMRID();
        if (mRID == null) {
            List<Name> names = endDevice.getNames();
            if (names.size() > 1) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.UNSUPPORTED_LIST_SIZE, END_DEVICE_ITEM, 1).get();
            }
            String name = names.stream().map(Name::getName).findFirst()
                    .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(
                            MessageSeeds.MISSING_MRID_OR_NAME_WITH_TYPE_FOR_ELEMENT,
                            END_DEVICE_NAME, END_DEVICE_ITEM));
            if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.EMPTY_ELEMENT, END_DEVICE_NAME).get();
            }
            builder.fromEndDeviceWithName(name);
        } else {
            if (Checks.is(mRID).emptyOrOnlyWhiteSpace()) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.EMPTY_ELEMENT, END_DEVICE_MRID).get();
            }
            builder.fromEndDeviceWithMRID(mRID);
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
            if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.EMPTY_ELEMENT, USAGE_POINT_NAME).get();
            }
            builder.fromUsagePointWithName(name);
        } else {
            if (Checks.is(mRID).emptyOrOnlyWhiteSpace()) {
                throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                        MessageSeeds.EMPTY_ELEMENT, USAGE_POINT_MRID).get();
            }
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

    /// TODO avoid code duplication with createMeterReadingsResponseMessageType(MeterReadings meterReadings, boolean bulkRequested, String bulkItem)
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
