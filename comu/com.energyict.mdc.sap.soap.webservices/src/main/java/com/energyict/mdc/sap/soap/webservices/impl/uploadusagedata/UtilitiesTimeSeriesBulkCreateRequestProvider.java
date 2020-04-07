/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.RangeSets;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.Quantity;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesERPItemBulkCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesERPItemBulkCreateRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesItemTypeCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmCrteReqItm;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmCrteReqItmSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmCrteReqUtilsTmeSers;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkCreateRequestProvider",
        service = {DataExportWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesTimeSeriesBulkCreateRequestProvider.NAME})
public class UtilitiesTimeSeriesBulkCreateRequestProvider extends AbstractUtilitiesTimeSeriesBulkRequestProvider<UtilitiesTimeSeriesERPItemBulkCreateRequestCOut, UtilsTmeSersERPItmBulkCrteReqMsg, UtilsTmeSersERPItmCrteReqMsg> implements ApplicationSpecific {
    public static final String NAME = "SAP TimeSeriesBulkCreateRequest";

    public UtilitiesTimeSeriesBulkCreateRequestProvider() {
        // for OSGi purposes
    }

    @Inject
    public UtilitiesTimeSeriesBulkCreateRequestProvider(PropertySpecService propertySpecService,
                                                        DataExportServiceCallType dataExportServiceCallType, Thesaurus thesaurus, Clock clock,
                                                        SAPCustomPropertySets sapCustomPropertySets,
                                                        ReadingNumberPerMessageProvider readingNumberPerMessageProvider,
                                                        WebServiceActivator webServiceActivator, DeviceService deviceService) {
        super(propertySpecService, dataExportServiceCallType, thesaurus, clock, sapCustomPropertySets, readingNumberPerMessageProvider, webServiceActivator, deviceService);
    }

    @Override
    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        super.setDataExportServiceCallType(dataExportService.getDataExportServiceCallType());
    }

    @Reference
    public void setTranslationsProvider(WebServiceActivator translationsProvider) {
        super.setThesaurus(translationsProvider.getThesaurus());
    }

    @Override
    @Reference
    public void setClock(Clock clock) {
        super.setClock(clock);
    }

    @Override
    @Reference
    public void setSapCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        super.setSapCustomPropertySets(sapCustomPropertySets);
    }

    @Reference(target = "(name=" + UtilitiesTimeSeriesBulkCreateConfirmationReceiver.NAME + ")")
    public void setInboundPart(InboundSoapEndPointProvider inboundPart) {
        // need to know that response from SAP can be processed correctly
    }

    @Reference
    public void setReadingNumberPerMessageProvider(ReadingNumberPerMessageProvider readingNumberPerMessageProvider) {
        super.setReadingNumberPerMessageProvider(readingNumberPerMessageProvider);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCreateRequestEndpoint(UtilitiesTimeSeriesERPItemBulkCreateRequestCOut createRequestPort, Map<String, Object> properties) {
        super.doAddEndpoint(createRequestPort, properties);
    }

    public void removeCreateRequestEndpoint(UtilitiesTimeSeriesERPItemBulkCreateRequestCOut createRequestPort) {
        super.doRemoveEndpoint(createRequestPort);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        super.setWebServiceActivator(webServiceActivator);
    }

    @Override
    @Reference
    public void setDeviceService(DeviceService deviceService) {
        super.setDeviceService(deviceService);
    }

    @Override
    String getMessageSenderMethod() {
        return UtilitiesTimeSeriesERPItemBulkCreateRequestCOut.class.getMethods()[0].getName();
    }

    @Override
    public Service get() {
        return new UtilitiesTimeSeriesERPItemBulkCreateRequestCOutService();
    }

    @Override
    public Class<UtilitiesTimeSeriesERPItemBulkCreateRequestCOut> getService() {
        return UtilitiesTimeSeriesERPItemBulkCreateRequestCOut.class;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<Operation> getSupportedOperations() {
        return EnumSet.of(Operation.CREATE, Operation.CHANGE);
    }


    @Override
    UtilsTmeSersERPItmBulkCrteReqMsg createMessageFromTimeSeries(List<UtilsTmeSersERPItmCrteReqMsg> timeSeriesList, String uuid, SetMultimap<String, String> values, Instant now) {
        UtilsTmeSersERPItmBulkCrteReqMsg msg = new UtilsTmeSersERPItmBulkCrteReqMsg();
        msg.setMessageHeader(createMessageHeader(uuid, now));
        msg.getUtilitiesTimeSeriesERPItemCreateRequestMessage().addAll(timeSeriesList);
        if (!msg.getUtilitiesTimeSeriesERPItemCreateRequestMessage().isEmpty()) {
            msg.getUtilitiesTimeSeriesERPItemCreateRequestMessage().forEach(message -> {
                values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(),
                        message.getUtilitiesTimeSeries().getID().getValue());
            });
            return msg;
        }
        return null;
    }

    @Override
    String createCustomInfo(List<UtilsTmeSersERPItmCrteReqMsg> timeSeriesList) {
        return timeSeriesList
                .stream()
                .map(UtilsTmeSersERPItmCrteReqMsg::getUtilitiesTimeSeries)
                .map(UtilsTmeSersERPItmCrteReqUtilsTmeSers::getID)
                .map(UtilitiesTimeSeriesID::getValue)
                .collect(Collectors.joining(","));
    }


    private BusinessDocumentMessageHeader createMessageHeader(String uuid, Instant now) {
        BusinessDocumentMessageHeader header = new BusinessDocumentMessageHeader();
        header.setSenderBusinessSystemID(webServiceActivator.getMeteringSystemId());
        header.setReconciliationIndicator(true);
        header.setUUID(createUUID(uuid));
        header.setCreationDateTime(now);
        return header;
    }

    private static com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UUID messageUUID
                = new com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    @Override
    List<UtilsTmeSersERPItmCrteReqMsg> prepareTimeSeries(ReadingTypeDataExportItem item, List<MeterReadingData> readingList, Instant now) {
        OptionalInt numberOfFractionDigits = getNumberOfFractionDigits(item);
        ReadingType readingType = item.getReadingType();
        Optional<TimeDuration> requestedReadingInterval = item.getRequestedReadingInterval();
        TemporalAmount interval = requestedReadingInterval.isPresent() ? requestedReadingInterval.get().asTemporalAmount() : readingType.getIntervalLength().orElse(Duration.ZERO);
        String unit = readingType.getMultiplier().getSymbol() + readingType.getUnit().getSymbol();
        Map<Instant, String> statuses = readingList.stream()
                .map(MeterReadingData::getReadingStatuses)
                .filter(Objects::nonNull)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a)          //it doesn't matter
                );
        List<MeterReading> meterReading = readingList.stream().map(MeterReadingData::getMeterReading).collect(Collectors.toList());
        Range<Instant> allReadingsRange = getRange(meterReading);
        Map<String, RangeSet<Instant>> profileRanges = item.getReadingContainer().getChannelsContainers().stream()
                .filter(cc -> cc.getInterval().toOpenClosedRange().isConnected(allReadingsRange))
                .map(cc -> Pair.of(cc, cc.getInterval().toOpenClosedRange().intersection(allReadingsRange)))
                .filter(ccAndRange -> !ccAndRange.getLast().isEmpty())
                .map(ccAndRange -> ccAndRange.getFirst().getChannel(readingType)
                        .map(channel -> Pair.of(channel, ccAndRange.getLast())))
                .flatMap(Functions.asStream())
                .flatMap(channelAndRange -> getTimeSlicedProfileId(channelAndRange.getFirst(), channelAndRange.getLast()).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, RangeSets::union));
        return profileRanges.entrySet().stream()
                .map(profileIdAndRange -> createRequestItem(profileIdAndRange.getKey(), profileIdAndRange.getValue(),
                        meterReading, interval, unit, now, numberOfFractionDigits, statuses))
                .collect(Collectors.toList());
    }

    @Override
    long calculateNumberOfReadingsInTimeSeries(List<UtilsTmeSersERPItmCrteReqMsg> list) {
        long count = 0;
        for (UtilsTmeSersERPItmCrteReqMsg msg : list) {
            count = count + msg.getUtilitiesTimeSeries().getItem().size();
        }
        return count;
    }


    private UtilsTmeSersERPItmCrteReqMsg createRequestItem(String profileId, RangeSet<Instant> rangeSet, List<MeterReading> meterReading, TemporalAmount interval,
                                                           String unit, Instant now, OptionalInt numberOfFractionDigits, Map<Instant, String> statuses) {
        UtilsTmeSersERPItmCrteReqMsg msg = new UtilsTmeSersERPItmCrteReqMsg();
        msg.setMessageHeader(createMessageHeader(UUID.randomUUID().toString(), now));
        msg.setUtilitiesTimeSeries(createTimeSeries(profileId, rangeSet, meterReading, interval, unit, numberOfFractionDigits, statuses));
        return msg;
    }

    private UtilsTmeSersERPItmCrteReqUtilsTmeSers createTimeSeries(String profileId, RangeSet<Instant> rangeSet, List<MeterReading> meterReading,
                                                                   TemporalAmount interval, String unit, OptionalInt numberOfFractionDigits, Map<Instant, String> statuses) {
        UtilsTmeSersERPItmCrteReqUtilsTmeSers timeSeries = new UtilsTmeSersERPItmCrteReqUtilsTmeSers();
        timeSeries.setID(createTimeSeriesID(profileId));

        meterReading.stream().map(MeterReading::getIntervalBlocks)
                .flatMap(List::stream)
                .map(IntervalBlock::getIntervals)
                .flatMap(List::stream)
                .filter(reading -> rangeSet.contains(reading.getTimeStamp()))
                .sorted(Comparator.comparing(BaseReading::getTimeStamp))
                .map(reading -> createItem(reading, interval, unit,
                        numberOfFractionDigits, statuses))
                .forEach(timeSeries.getItem()::add);
        return timeSeries;
    }

    private static UtilitiesTimeSeriesID createTimeSeriesID(String profileId) {
        UtilitiesTimeSeriesID id = new UtilitiesTimeSeriesID();
        id.setValue(profileId);
        return id;
    }

    private UtilsTmeSersERPItmCrteReqItm createItem(IntervalReading reading, TemporalAmount interval, String unit, OptionalInt numberOfFractionDigits, Map<Instant, String> statuses) {
        UtilsTmeSersERPItmCrteReqItm crteReqItm = new UtilsTmeSersERPItmCrteReqItm();
        crteReqItm.setUTCValidityStartDateTime(reading.getTimeStamp().minus(interval));
        crteReqItm.setUTCValidityEndDateTime(reading.getTimeStamp());
        crteReqItm.setQuantity(createQuantity(getRoundedBigDecimal(reading.getValue(), numberOfFractionDigits), unit));
        crteReqItm.getItemStatus().add(createStatus(statuses != null ? statuses.get(reading.getTimeStamp()) : "0"));
        return crteReqItm;
    }

    private static Quantity createQuantity(BigDecimal value, String unit) {
        Quantity quantity = new Quantity();
        quantity.setUnitCode(unit.toUpperCase());
        quantity.setValue(value);
        return quantity;
    }

    private static UtilsTmeSersERPItmCrteReqItmSts createStatus(String code) {
        UtilsTmeSersERPItmCrteReqItmSts status = new UtilsTmeSersERPItmCrteReqItmSts();
        UtilitiesTimeSeriesItemTypeCode typeCode = new UtilitiesTimeSeriesItemTypeCode();
        typeCode.setValue(code);
        status.setUtilitiesTimeSeriesItemTypeCode(typeCode);
        return status;
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
