/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.impl.webservicecall.DataExportServiceCallTypeImpl;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.ValidationResult;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.SetMultimap;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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

    protected int NUMBER_OF_READINGS_PER_MSG;
    protected final int NUMBER_OF_READINGS_PER_MSG_DEFAULT = 500;
    protected int MESSAGE_SIZE;
    protected int MESSAGE_SIZE_DEFAULT = 512000;//500kB
    protected int READING_SIZE; //bytes
    protected final int READING_SIZE_DEFAULT = 327; //bytes
    protected final String PROPERTY_READING_SIZE = "reading.size";
    protected final String PROPERTY_MSG_SIZE = "msg.size.property";
    protected final int HEADER_SIZE = 1024; // bytes

    AbstractUtilitiesTimeSeriesBulkRequestProvider() {
        // for OSGi
    }

    @Inject
        // for tests
    AbstractUtilitiesTimeSeriesBulkRequestProvider(PropertySpecService propertySpecService,
                                                   DataExportServiceCallType dataExportServiceCallType, Thesaurus thesaurus, Clock clock,
                                                   SAPCustomPropertySets sapCustomPropertySets,
                                                   BundleContext bundleContext) {
        setPropertySpecService(propertySpecService);
        setDataExportServiceCallType(dataExportServiceCallType);
        setThesaurus(thesaurus);
        setClock(clock);
        setSapCustomPropertySets(sapCustomPropertySets);
        initNumberOfReadingsPerMsg(bundleContext);
    }

    protected void initNumberOfReadingsPerMsg(BundleContext bundleContext){
        String msgSize = bundleContext.getProperty(PROPERTY_MSG_SIZE);
        String readingSize = bundleContext.getProperty(PROPERTY_READING_SIZE);
        if (!Strings.isNullOrEmpty(msgSize)) {
            MESSAGE_SIZE = Integer.valueOf(msgSize)*1024;
        }else{
            MESSAGE_SIZE = MESSAGE_SIZE_DEFAULT;
        }

        if (!Strings.isNullOrEmpty(readingSize)) {
            READING_SIZE = Integer.valueOf(readingSize);
        }else{
            READING_SIZE = READING_SIZE_DEFAULT;
        }

        NUMBER_OF_READINGS_PER_MSG = (MESSAGE_SIZE - HEADER_SIZE)/READING_SIZE;

        if (NUMBER_OF_READINGS_PER_MSG == 0)
            NUMBER_OF_READINGS_PER_MSG = NUMBER_OF_READINGS_PER_MSG_DEFAULT;
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

    abstract void prepareTimeSerieses(List<TS> list, MeterReadingData item, Instant now);
    abstract MSG createMessageFromTimeSerieses(List<TS> list, String uuid, SetMultimap<String, String> attributes, Instant now);
    abstract long calculateNumberOfReadingsInTimeSerieses(List<TS> list);

    @Override
    public List<ServiceCall> call(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data) {
        List<TS> timeSeriesListToSend = new ArrayList<>();
        Instant now = null;
        long meterReadingDataNr = 0;
        List<ServiceCall> srvCallList = new ArrayList<>();
        List<MeterReadingData> readingDataToSend = new ArrayList<>();

        //List<MeterReadingData> readingDataList = data.map(MeterReadingData.class::cast).collect(Collectors.toList());
        List<MeterReadingData> readingDataList = data.map(MeterReadingData.class::cast).collect(Collectors.toList());
        for (Iterator iterator = readingDataList.iterator(); iterator.hasNext(); ) {
            MeterReadingData meterReadingData = (MeterReadingData) iterator.next();
            /* Calculate number of readings that should be sent for this meterReadingData */
            List<TS> timeSeriesListFromMeterData = new ArrayList<>();
            if (now == null){
                now = getClock().instant();
            }

            prepareTimeSerieses(timeSeriesListFromMeterData, meterReadingData, now);
            long numberOfItemsToSend = calculateNumberOfReadingsInTimeSerieses(timeSeriesListFromMeterData);
            if (numberOfItemsToSend > NUMBER_OF_READINGS_PER_MSG) {
                /*It means that number of readings in one meterReadingData is more than allowable size.
                /* Just send it. But actually shouldn't happen */
                sendPartOfData(endPointConfiguration, timeSeriesListFromMeterData, new ArrayList<MeterReadingData>(Arrays.asList(meterReadingData)), now).ifPresent(srvCall -> {
                    srvCallList.add(srvCall);
                });
                now = null;
                continue;
            }

            if (meterReadingDataNr < NUMBER_OF_READINGS_PER_MSG) {
                if (NUMBER_OF_READINGS_PER_MSG - meterReadingDataNr > numberOfItemsToSend) {
                    /* If we have enough space in message for readings add it to message. If no send message without current readings.
                     * Current readings will be sent in next message */
                    readingDataToSend.add(meterReadingData);
                    timeSeriesListToSend.addAll(timeSeriesListFromMeterData);
                    meterReadingDataNr = meterReadingDataNr + numberOfItemsToSend;
                    if (!iterator.hasNext()) {
                        sendPartOfData(endPointConfiguration, timeSeriesListToSend, readingDataToSend, now).ifPresent(srvCall -> {
                            srvCallList.add(srvCall);
                        });
                        meterReadingDataNr = 0;
                        now = null;
                        timeSeriesListToSend.clear();
                        readingDataToSend.clear();
                    }
                } else {
                    sendPartOfData(endPointConfiguration, timeSeriesListToSend, readingDataToSend, now).ifPresent(srvCall -> {
                        srvCallList.add(srvCall);
                    });
                    meterReadingDataNr = numberOfItemsToSend;
                    now = null;
                    timeSeriesListToSend.clear();
                    timeSeriesListToSend.addAll(timeSeriesListFromMeterData);
                    readingDataToSend.clear();
                    readingDataToSend.add(meterReadingData);

                    if (!iterator.hasNext()) {
                        /*It was last readings. Just send it*/
                        sendPartOfData(endPointConfiguration, timeSeriesListToSend, readingDataToSend, now).ifPresent(srvCall -> {
                            srvCallList.add(srvCall);
                        });
                        now = null;
                    }
                }

            } else {
                sendPartOfData(endPointConfiguration, timeSeriesListToSend, readingDataToSend, now).ifPresent(srvCall -> srvCallList.add(srvCall));
                meterReadingDataNr = 0;
                now = null;
                timeSeriesListToSend.clear();
                readingDataToSend.clear();
            }
        }
        return srvCallList;
    }

    private Optional<ServiceCall> sendPartOfData(EndPointConfiguration endPointConfiguration, List<TS> timeSerieses, List<MeterReadingData> exportData, Instant now){
        String uuid = UUID.randomUUID().toString();
        try {
            SetMultimap<String, String> values = HashMultimap.create();
            MSG message = createMessageFromTimeSerieses(timeSerieses, uuid, values, now);
            if (message != null) {
                Set<EndPointConfiguration> processedEndpoints = using(getMessageSenderMethod())
                        .toEndpoints(endPointConfiguration)
                        .withRelatedAttributes(values)
                        .send(message)
                        .keySet();
                if (!processedEndpoints.contains(endPointConfiguration)) {
                    throw SAPWebServiceException.endpointsNotProcessed(thesaurus, endPointConfiguration);
                }
                Optional<ServiceCall> serviceCall = getTimeout(endPointConfiguration)
                        .filter(timeout -> !timeout.isEmpty())
                        .map(timeout -> dataExportServiceCallType.startServiceCallAsync(uuid, timeout.getMilliSeconds(), exportData.stream().map(data->data.getItem()).collect(Collectors.toList())));

                return serviceCall;
            }
            return Optional.empty();
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

    static String asString(ValidationResult validationResult) {
        switch (validationResult) {
            case ACTUAL:
                return "ACTL";
            case INVALID:
                return "INVL";
            default:
                return "0";
        }
    }
}
