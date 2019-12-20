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

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    protected volatile WebServiceActivator webServiceActivator;

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
        List<TS> timeSeriesListToSend = new ArrayList<>();
        Instant now = null;
        long meterReadingDataNr = 0;
        List<MeterReadingData> readingDataToSend = new ArrayList<>();
        TimeDuration timeout = getTimeout(endPointConfiguration).filter(tout -> !tout.isEmpty()).orElse(null);

        List<MeterReadingData> readingDataList = data.filter(MeterReadingData.class::isInstance)
                .map(MeterReadingData.class::cast)
                .collect(Collectors.toList());
        for (Iterator iterator = readingDataList.iterator(); iterator.hasNext(); ) {
            MeterReadingData meterReadingData = (MeterReadingData) iterator.next();

            if (now == null){
                now = getClock().instant();
            }

            List<TS> timeSeriesListFromMeterData = prepareTimeSeries(meterReadingData, now);
            /* Calculate number of readings that should be sent for this meterReadingData */
            long numberOfItemsToSend = calculateNumberOfReadingsInTimeSeries(timeSeriesListFromMeterData);

            if (numberOfItemsToSend >= numberOfReadingsPerMsg) {
                /* It means that number of readings in one meterReadingData is more than or equal to allowed size.
                /* Just send them and also previously kept readings */
                if (meterReadingDataNr != 0) {
                    sendPartOfData(endPointConfiguration, context, timeSeriesListToSend, readingDataToSend, now, timeout);
                    meterReadingDataNr = 0;
                    timeSeriesListToSend.clear();
                    readingDataToSend.clear();
                }
                sendPartOfData(endPointConfiguration, context, timeSeriesListFromMeterData, Collections.singletonList(meterReadingData), now, timeout);
                now = null;
                continue;
            }

            if (meterReadingDataNr < numberOfReadingsPerMsg) {
                if (numberOfReadingsPerMsg - meterReadingDataNr >= numberOfItemsToSend) {
                    /* If we have enough space in message for readings add it to message. If no send message without current readings.
                     * Current readings will be sent in next message */
                    readingDataToSend.add(meterReadingData);
                    timeSeriesListToSend.addAll(timeSeriesListFromMeterData);
                    meterReadingDataNr += numberOfItemsToSend;
                    if (!iterator.hasNext()) {
                        sendPartOfData(endPointConfiguration, context, timeSeriesListToSend, readingDataToSend, now, timeout);
                        meterReadingDataNr = 0;
                        now = null;
                        timeSeriesListToSend.clear();
                        readingDataToSend.clear();
                    }
                } else {
                    sendPartOfData(endPointConfiguration, context, timeSeriesListToSend, readingDataToSend, now, timeout);
                    meterReadingDataNr = numberOfItemsToSend;
                    timeSeriesListToSend.clear();
                    timeSeriesListToSend.addAll(timeSeriesListFromMeterData);
                    readingDataToSend.clear();
                    readingDataToSend.add(meterReadingData);

                    if (!iterator.hasNext()) {
                        /* These were last readings. Just send them*/
                        sendPartOfData(endPointConfiguration, context, timeSeriesListToSend, readingDataToSend, now, timeout);
                    }
                    now = null;
                }

            } else {
                sendPartOfData(endPointConfiguration, context, timeSeriesListToSend, readingDataToSend, now, timeout);
                meterReadingDataNr = 0;
                now = null;
                timeSeriesListToSend.clear();
                readingDataToSend.clear();
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
                        .ifPresent(millis -> context.startServiceCall(uuid, millis, exportData.stream().map(MeterReadingData::getItem).collect(Collectors.toList())));
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
}
