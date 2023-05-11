/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.BulkWebServiceCallResult;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractUtilitiesTimeSeriesBulkRequestProvider<EP, MSG, TS> extends AbstractOutboundEndPointProvider<EP> implements DataExportWebService, OutboundSoapEndPointProvider {
    private volatile PropertySpecService propertySpecService;
    private volatile DataExportServiceCallType dataExportServiceCallType;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    volatile WebServiceActivator webServiceActivator;
    private volatile DeviceService deviceService;

    private int numberOfReadingsPerMsg;

    AbstractUtilitiesTimeSeriesBulkRequestProvider() {
        // for OSGi
    }

    @Inject
        // for tests
    AbstractUtilitiesTimeSeriesBulkRequestProvider(PropertySpecService propertySpecService,
                                                   DataExportServiceCallType dataExportServiceCallType, Thesaurus thesaurus, Clock clock,
                                                   SAPCustomPropertySets sapCustomPropertySets,
                                                   ReadingNumberPerMessageProvider readingNumberPerMessageProvider,
                                                   WebServiceActivator webServiceActivator, DeviceService deviceService) {
        setPropertySpecService(propertySpecService);
        setDataExportServiceCallType(dataExportServiceCallType);
        setThesaurus(thesaurus);
        setClock(clock);
        setSapCustomPropertySets(sapCustomPropertySets);
        setReadingNumberPerMessageProvider(readingNumberPerMessageProvider);
        setWebServiceActivator(webServiceActivator);
        setDeviceService(deviceService);
    }


    void setReadingNumberPerMessageProvider(ReadingNumberPerMessageProvider readingNumberPerMessageProvider) {
        numberOfReadingsPerMsg = readingNumberPerMessageProvider.getNumberOfReadingsPerMsg();
    }

    void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    void setDataExportServiceCallType(DataExportServiceCallType dataExportServiceCallType) {
        this.dataExportServiceCallType = dataExportServiceCallType;
    }

    void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    Clock getClock() {
        return clock;
    }

    void setSapCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Override
    public String getSupportedDataType() {
        return DataExportService.STANDARD_READING_DATA_TYPE;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(
                propertySpecService.timeDurationSpec()
                        .named(TranslationKeys.TIMEOUT_PROPERTY)
                        .describedAs(TranslationKeys.TIMEOUT_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .setDefaultValue(TimeDuration.minutes(2))
                        .finish()
        );
    }

    abstract List<TS> prepareTimeSeries(ReadingTypeDataExportItem item, List<MeterReadingData> readingList, Instant now);

    abstract MSG createMessageFromTimeSeries(List<TS> list, String uuid, SetMultimap<String, String> attributes, Instant now);

    abstract String createCustomInfo(List<TS> list);

    abstract long calculateNumberOfReadingsInTimeSeries(List<TS> list);

    OptionalInt getNumberOfFractionDigits(ReadingTypeDataExportItem item) {
        Optional<Integer> numberOfFractionDigits = Optional.empty();
        if (item.getReadingContainer() instanceof Meter) {
            numberOfFractionDigits = deviceService.findDeviceByMrid(((Meter) item.getReadingContainer()).getMRID())
                    .flatMap(device -> device.getChannels().stream().filter(c -> c.getReadingType().equals(item.getReadingType()))
                            .findFirst()
                            .map(com.energyict.mdc.common.device.data.Channel::getNrOfFractionDigits));
        }
        return numberOfFractionDigits.isPresent() ? OptionalInt.of(numberOfFractionDigits.get()) : OptionalInt.empty();
    }

    BigDecimal getRoundedBigDecimal(BigDecimal value, OptionalInt numberOfFractionDigits) {
        return numberOfFractionDigits.isPresent() ? value.setScale(numberOfFractionDigits.getAsInt(), BigDecimal.ROUND_UP) : value;
    }

    @Override
    public void call(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data, ExportContext context) {
        Instant now = getClock().instant();
        TimeDuration timeout = getTimeout(endPointConfiguration).filter(tout -> !tout.isEmpty()).orElse(null);
        LinkedListMultimap<Long, TimeSeriesWrapper<TS>> seriesMultimap = LinkedListMultimap.create();
        List<MeterReadingData> readingDataList = data.filter(MeterReadingData.class::isInstance)
                .map(MeterReadingData.class::cast)
                .collect(Collectors.toList());

        Map<ReadingTypeDataExportItem, List<MeterReadingData>> dataMap = readingDataList.stream()
                .collect(Collectors.groupingBy(MeterReadingData::getItem));

        dataMap.forEach((item, readingList) -> {
            List<TS> timeSeriesListFromMeterData = prepareTimeSeries(item, readingList, now);

            /* Calculate number of readings that should be sent for this meterReadingData.
             * numberOfItemsToSend is key for seriesMultimap */
            long numberOfItemsToSend = calculateNumberOfReadingsInTimeSeries(timeSeriesListFromMeterData);
            if (numberOfItemsToSend != 0) {
                seriesMultimap.put(numberOfItemsToSend, new TimeSeriesWrapper<>(timeSeriesListFromMeterData, item));
            }
        });

        /* Send one by one readings that exceed numberOfReadingsPerMsg */
        List<Long> listToSend = seriesMultimap.keySet().stream()
                .filter(numberOfItemsToSend -> numberOfItemsToSend >= numberOfReadingsPerMsg)
                .collect(Collectors.toList());
        for (Long key : listToSend) {
            seriesMultimap.removeAll(key)
                    .forEach(timeSeriesWrapper -> sendPartOfData(endPointConfiguration, context,
                            Collections.singletonList(timeSeriesWrapper),
                            now,
                            timeout)
                    );
        }

        long timeSeriesNumber = 0;
        List<TimeSeriesWrapper<TS>> timeSeriesWrapperListToSend = new ArrayList<>();
        Set<Long> keys = seriesMultimap.keySet();
        while (!seriesMultimap.isEmpty()) {
            long diff = numberOfReadingsPerMsg - timeSeriesNumber;
            /* Check if we have readingData  with number of readings less then free size in message */
            Optional<Long> key = keys.stream()
                    .filter(value -> value <= diff)
                    .max(Comparator.naturalOrder());
            if (!key.isPresent()) {
                /* No readings can be added to message. So send all that we already have in message */
                sendPartOfData(endPointConfiguration, context,
                        timeSeriesWrapperListToSend,
                        now, timeout);
                timeSeriesWrapperListToSend.clear();
                timeSeriesNumber = 0;
            } else {
                List<TimeSeriesWrapper<TS>> timeSeriesWrapperList = seriesMultimap.get(key.get());
                TimeSeriesWrapper<TS> timeSeriesWrapper = timeSeriesWrapperList.remove(0);
                timeSeriesWrapperListToSend.add(timeSeriesWrapper);

                timeSeriesNumber += key.get();
                if (timeSeriesWrapperList.isEmpty()) {
                    keys.remove(key.get());
                }
                if (seriesMultimap.isEmpty()) {
                    sendPartOfData(endPointConfiguration,
                            context,
                            timeSeriesWrapperListToSend,
                            now,
                            timeout);
                    timeSeriesWrapperListToSend.clear();
                    timeSeriesNumber = 0;
                }
            }
        }
    }

    private void sendPartOfData(EndPointConfiguration endPointConfiguration, ExportContext context,
                                List<TimeSeriesWrapper<TS>> timeSeriesWrappers,
                                Instant now,
                                TimeDuration timeout) {
        String uuid = UUID.randomUUID().toString();
        try {

            Map<ReadingTypeDataExportItem, String> data = new HashMap<>();
            List<TS> timeSeriesList = new ArrayList<>();
            for (TimeSeriesWrapper<TS> ts : timeSeriesWrappers) {
                List<TS> series = ts.getTimeSeries();
                data.put(ts.getReadingTypeDataExportItem(), createCustomInfo(series));
                timeSeriesList.addAll(series);
            }

            SetMultimap<String, String> values = HashMultimap.create();

            MSG message = createMessageFromTimeSeries(timeSeriesList, uuid, values, now);
            if (message != null) {
                BulkWebServiceCallResult result = using(getMessageSenderMethod())
                        .toEndpoints(endPointConfiguration)
                        .withRelatedAttributes(values)
                        .send(message);
                if (!result.isSuccessful(endPointConfiguration)) {
                    throw SAPWebServiceException.endpointsNotProcessed(thesaurus, endPointConfiguration);
                }
                Optional.ofNullable(timeout)
                        .map(TimeDuration::getMilliSeconds)
                        .ifPresent(millis -> context.startAndRegisterServiceCall(uuid, millis, data));
            }
        } catch (Exception ex) {
            endPointConfiguration.log(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }

    abstract String getMessageSenderMethod();

    private static Optional<TimeDuration> getTimeout(EndPointConfiguration endPoint) {
        return endPoint.getProperties().stream()
                .filter(property -> DataExportWebService.TIMEOUT_PROPERTY_KEY.equals(property.getName()))
                .findAny()
                .map(EndPointProperty::getValue)
                .filter(TimeDuration.class::isInstance)
                .map(TimeDuration.class::cast);
    }

    static Range<Instant> getRange(List<MeterReading> meterReading) {
        Optional<Range<Instant>> intervalRange = getRange(meterReading.stream()
                .map(MeterReading::getIntervalBlocks)
                .flatMap(List::stream)
                .map(IntervalBlock::getIntervals)
                .flatMap(List::stream));
        Optional<Range<Instant>> registerRange = getRange(meterReading.stream().map(MeterReading::getReadings).flatMap(List::stream));
        if (intervalRange.isPresent()) {
            return registerRange.map(intervalRange.get()::span).orElseGet(intervalRange::get);
        } else {
            return registerRange.orElseGet(Range::all);
        }
    }

    private static Optional<Range<Instant>> getRange(Stream<? extends BaseReading> readings) {
        return readings
                .map(BaseReading::getTimeStamp)
                .map(instant -> Pair.of(instant, instant))
                .reduce((a, b) -> Pair.of(
                        min(a.getFirst(), b.getFirst()),
                        max(a.getLast(), b.getLast())
                ))
                .map(pair -> Range.closed(pair.getFirst(), pair.getLast()));
    }

    private static Instant min(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }

    private static Instant max(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    Map<String, RangeSet<Instant>> getTimeSlicedProfileId(Channel channel, Range<Instant> range) {
        return sapCustomPropertySets.getProfileId(channel, range);
    }

    void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    private static class TimeSeriesWrapper<TS> {
        private List<TS> timeSeries;
        private ReadingTypeDataExportItem item;

        TimeSeriesWrapper(List<TS> timeSeries, ReadingTypeDataExportItem item) {
            this.timeSeries = timeSeries;
            this.item = item;
        }

        List<TS> getTimeSeries() {
            return timeSeries;
        }

        ReadingTypeDataExportItem getReadingTypeDataExportItem() {
            return this.item;
        }
    }
}
