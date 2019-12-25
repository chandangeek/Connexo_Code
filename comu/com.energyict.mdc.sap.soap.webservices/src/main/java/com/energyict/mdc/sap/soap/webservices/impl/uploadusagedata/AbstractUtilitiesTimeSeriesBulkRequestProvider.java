/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalLong;
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
                                                   WebServiceActivator webServiceActivator) {
        setPropertySpecService(propertySpecService);
        setDataExportServiceCallType(dataExportServiceCallType);
        setThesaurus(thesaurus);
        setClock(clock);
        setSapCustomPropertySets(sapCustomPropertySets);
        setReadingNumberPerMessageProvider(readingNumberPerMessageProvider);
        setWebServiceActivator(webServiceActivator);
    }


    void setReadingNumberPerMessageProvider(ReadingNumberPerMessageProvider readingNumberPerMessageProvider){
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

    abstract List<TS> prepareTimeSeries(MeterReadingData item, Instant now);
    abstract MSG createMessageFromTimeSeries(List<TS> list, String uuid, SetMultimap<String, String> attributes, Instant now);
    abstract long calculateNumberOfReadingsInTimeSeries(List<TS> list);

    @Override
    public void call(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data, ExportContext context) {

        Instant now = getClock().instant();
        TimeDuration timeout = getTimeout(endPointConfiguration).filter(tout -> !tout.isEmpty()).orElse(null);
        TreeMultimap<Long, TimeSeriesWrapperImpl> seriesMultimap = TreeMultimap.create();

        List<MeterReadingData> readingDataList = data.filter(MeterReadingData.class::isInstance)
                .map(MeterReadingData.class::cast)
                .collect(Collectors.toList());

        for (Iterator iterator = readingDataList.iterator(); iterator.hasNext(); ) {
            MeterReadingData meterReadingData = (MeterReadingData) iterator.next();
            List<TS> timeSeriesListFromMeterData = prepareTimeSeries(meterReadingData, now);

            /* Calculate number of readings that should be sent for this meterReadingData.
             * numberOfItemsToSend is key for seriesMultimap */
            Long numberOfItemsToSend = calculateNumberOfReadingsInTimeSeries(timeSeriesListFromMeterData);
            if (numberOfItemsToSend != 0) {
                TimeSeriesWrapperImpl tsWrapper = new TimeSeriesWrapperImpl();
                tsWrapper.setTimeSeries(timeSeriesListFromMeterData);
                tsWrapper.setMeterReadingData(meterReadingData);
                seriesMultimap.put(numberOfItemsToSend, tsWrapper);
            }
        }
        /* Send one by one readings that exceed numberOfReadingsPerMsg */
        List<Long> listToSend = seriesMultimap.keySet().stream().filter(numberOfItemsToSend->numberOfItemsToSend > numberOfReadingsPerMsg).collect(Collectors.toList());
        if (!listToSend.isEmpty()) {
            for (Iterator keyIterator = listToSend.iterator(); keyIterator.hasNext();) {
                Long key = (Long)keyIterator.next();
                seriesMultimap.get(key).stream().forEach(timeSeriesWrapper -> {
                            sendPartOfData(endPointConfiguration, context,
                                    timeSeriesWrapper.getTimeSeries(),
                                    Collections.singletonList(timeSeriesWrapper.getMeterReadingData()),
                                    now,
                                    timeout);
                        }
                );
                seriesMultimap.removeAll(key);
            }
        }

        long timeSeriesNumber = 0;
        List<TS> timeSerieslistToSend = new ArrayList<>();
        List<MeterReadingData> meterReadingDataListToSend = new ArrayList<>();
        NavigableSet<Long> keys = seriesMultimap.keySet();

        while (!seriesMultimap.isEmpty()) {
            long diff = numberOfReadingsPerMsg - timeSeriesNumber;
            if (diff >= 0) {
                /* Check if we have readingData  with number of readings less then free size in message */
                Optional<Long> key = keys.stream().filter(value -> value <= diff).max(Long::compare);
                if (!key.isPresent()) {
                    /* No readings can be added to message. So send all that we already have in message */
                    sendPartOfData(endPointConfiguration, context,
                            timeSerieslistToSend,
                            meterReadingDataListToSend,
                            now, timeout);
                    timeSerieslistToSend.clear();
                    meterReadingDataListToSend.clear();
                    timeSeriesNumber = 0;
                } else {
                    TimeSeriesWrapperImpl timeSeriesWrapper = seriesMultimap.get(key.get()).pollLast();
                    timeSerieslistToSend.addAll(timeSeriesWrapper.getTimeSeries());
                    meterReadingDataListToSend.add(timeSeriesWrapper.getMeterReadingData());
                    timeSeriesNumber = timeSeriesNumber + key.get();
                    if (seriesMultimap.get(key.get()).isEmpty()) {
                        seriesMultimap.removeAll(key.get());
                    }
                    if (seriesMultimap.isEmpty()) {
                        sendPartOfData(endPointConfiguration,
                                context,
                                timeSerieslistToSend,
                                meterReadingDataListToSend,
                                now,
                                timeout);
                        timeSerieslistToSend.clear();
                        meterReadingDataListToSend.clear();
                        timeSeriesNumber = 0;
                    }
                }
            }
        }
    }

    private void sendPartOfData(EndPointConfiguration endPointConfiguration, ExportContext context,
                                List<TS> timeSeries, List<MeterReadingData> exportData,
                                Instant now,
                                TimeDuration timeout) {
        String uuid = UUID.randomUUID().toString();
        try {
            SetMultimap<String, String> values = HashMultimap.create();
            MSG message = createMessageFromTimeSeries(timeSeries, uuid, values, now);
            if (message != null) {
                Set<EndPointConfiguration> processedEndpoints = using(getMessageSenderMethod())
                        .toEndpoints(endPointConfiguration)
                        .withRelatedAttributes(values)
                        .send(message)
                        .keySet();
                if (!processedEndpoints.contains(endPointConfiguration)) {
                    throw SAPWebServiceException.endpointsNotProcessed(thesaurus, endPointConfiguration);
                }
                Optional.ofNullable(timeout)
                        .map(TimeDuration::getMilliSeconds)
                        .ifPresent(millis -> context.startAndRegisterServiceCall(uuid, millis, exportData.stream().map(MeterReadingData::getItem).collect(Collectors.toList())));
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

    static Range<Instant> getRange(MeterReading meterReading) {
        Optional<Range<Instant>> intervalRange = getRange(meterReading.getIntervalBlocks().stream()
                .map(IntervalBlock::getIntervals)
                .flatMap(List::stream));
        Optional<Range<Instant>> registerRange = getRange(meterReading.getReadings().stream());
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

    private class TimeSeriesWrapperImpl implements Comparable<TimeSeriesWrapperImpl> {

        List<TS> timeSeries;
        MeterReadingData meterReadingData;

        public int compareTo(TimeSeriesWrapperImpl o) {
            /* It doesn't matter in which order timeserieses will be stored in seriesMultimap for the same key */
            return 1;
        }


        public void setTimeSeries(List<TS> series) {
            timeSeries = series;
        }


        public List<TS> getTimeSeries() {
            return timeSeries;
        }

        public void setMeterReadingData(MeterReadingData meterReadingData) {
            this.meterReadingData = meterReadingData;
        }

        public MeterReadingData getMeterReadingData() {
            return this.meterReadingData;
        }
    }
}
