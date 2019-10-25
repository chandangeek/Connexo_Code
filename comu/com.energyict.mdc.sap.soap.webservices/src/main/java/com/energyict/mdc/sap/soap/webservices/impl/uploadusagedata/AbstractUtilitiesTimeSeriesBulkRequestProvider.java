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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.SetMultimap;
import org.osgi.framework.BundleContext;

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

public abstract class AbstractUtilitiesTimeSeriesBulkRequestProvider<EP, MSG> extends AbstractOutboundEndPointProvider<EP> implements DataExportWebService, OutboundSoapEndPointProvider {
    private volatile PropertySpecService propertySpecService;
    private volatile DataExportServiceCallType dataExportServiceCallType;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;

    protected int NUMBER_OF_READINGS_PER_MSG;//Some default value
    protected final String PROPERTY_MSG_SIZE = "msg.size.property";

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
        String NUMBER_OF_READINGS_PER_MSG = bundleContext.getProperty(PROPERTY_MSG_SIZE);
        System.out.println("NUMBER_OF_READINGS_PER_MSG = "+NUMBER_OF_READINGS_PER_MSG);
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

    @Override
    public List<ServiceCall> call(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data){//
    /*{
          return  sendPartOfData(endPointConfiguration, data);
    }*/
        System.out.println("CALL!!!!!!!!!");
        int meterReadingDataNr = 0;
        List<ServiceCall> srvCallList = new ArrayList<>();

        List<MeterReadingData> readingDataToSend = new ArrayList<>();
        int x =0;

        List<MeterReadingData> readingDataList = data.map(MeterReadingData.class::cast).collect(Collectors.toList());

        MeterReadingData readingDataTmp = readingDataList.get(0);
        /* Test data */
        readingDataList.add(readingDataTmp);
        readingDataList.add(readingDataTmp);
        readingDataList.add(readingDataTmp);

        Iterator iterator;

        for(iterator = readingDataList.iterator(); iterator.hasNext(); ){
            MeterReadingData meterReadingData = (MeterReadingData)iterator.next();
            /*Calculate number of readings that should be sent for this meterReadingData*/
            int numberOfItemsToSend = meterReadingData.getMeterReading().getReadings().size();

            Iterator intervalBlocksIterator;
            for (intervalBlocksIterator = meterReadingData.getMeterReading().getIntervalBlocks().iterator();
                 intervalBlocksIterator.hasNext(); ) {
                numberOfItemsToSend = numberOfItemsToSend+((IntervalBlock) intervalBlocksIterator.next()).getIntervals().size();
            }

            if ( numberOfItemsToSend >  NUMBER_OF_READINGS_PER_MSG){
                /*It means that number of readings in one meterReadingData is more than allowable size.
                * This reading will not be sent? At current moment no because all reading from one profileId
                * have to be sent in one message */
                System.out.println("CONTINUE!!!");
                continue;
            }

            //List<IntervalBlock>  tmplist = meterReadingData.getMeterReading().getIntervalBlocks();
            if (meterReadingDataNr < NUMBER_OF_READINGS_PER_MSG){
                System.out.println("NUMBER OF READINGS = "+numberOfItemsToSend);
                System.out.println("NUMBER ALL READINGS = "+meterReadingDataNr);
                if (NUMBER_OF_READINGS_PER_MSG - meterReadingDataNr > numberOfItemsToSend) {
                    readingDataToSend.add(meterReadingData);
                    meterReadingDataNr = meterReadingDataNr + numberOfItemsToSend;
                    if (!iterator.hasNext()){
                        System.out.println("SEND 1!!!!!!!!!!");
                        sendPartOfData(endPointConfiguration, readingDataToSend.stream()).ifPresent(srvCall->{
                            srvCallList.add(srvCall);
                        });
                        meterReadingDataNr = 0;
                        readingDataToSend.clear();
                    }
                }else{
                    System.out.println("SEND 2!!!!!!!!!!");
                    sendPartOfData(endPointConfiguration, readingDataToSend.stream()).ifPresent(srvCall->{
                        srvCallList.add(srvCall);
                    });
                    meterReadingDataNr = numberOfItemsToSend;
                    readingDataToSend.clear();
                    readingDataToSend.add(meterReadingData);
                    if (!iterator.hasNext()){
                        System.out.println("IT WAS LAST READING SEND IT 2!!!!!!!!!!");
                        /*It was last readings. Just send it*/
                        sendPartOfData(endPointConfiguration, readingDataToSend.stream()).ifPresent(srvCall->{
                            srvCallList.add(srvCall);
                        });
                    }
                }

            }else{
                System.out.println("SEND 3!!!!!!!!!!");
                sendPartOfData(endPointConfiguration, readingDataToSend.stream()).ifPresent(srvCall->srvCallList.add(srvCall));
                meterReadingDataNr = 0;
                readingDataToSend.clear();
            }

        }

        return srvCallList;
    }


    private Optional<ServiceCall> sendPartOfData(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data){
        String uuid = UUID.randomUUID().toString();
        try {
            SetMultimap<String, String> values = HashMultimap.create();
            MSG message = createMessage(data, uuid, values);
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
                        .map(timeout -> dataExportServiceCallType.startServiceCallAsync(uuid, timeout.getMilliSeconds(), data));

                return serviceCall;
            }
            return Optional.empty();
        } catch (Exception ex) {
            endPointConfiguration.log(ex.getLocalizedMessage(), ex);
            throw ex;
        }
    }

    abstract MSG createMessage(Stream<? extends ExportData> data, String uuid, SetMultimap<String, String> attributes);

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
