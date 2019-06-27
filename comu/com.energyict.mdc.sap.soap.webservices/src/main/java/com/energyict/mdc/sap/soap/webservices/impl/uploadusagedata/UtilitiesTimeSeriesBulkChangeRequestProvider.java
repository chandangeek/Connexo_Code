/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.Quantity;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilitiesTimeSeriesERPItemBulkChangeRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilitiesTimeSeriesERPItemBulkChangeRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilitiesTimeSeriesID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilitiesTimeSeriesItemTypeCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilsTmeSersERPItmBulkChgReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilsTmeSersERPItmChgReqItm;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilsTmeSersERPItmChgReqItmSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilsTmeSersERPItmChgReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilsTmeSersERPItmChgReqUtilsTmeSers;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component(name = UtilitiesTimeSeriesBulkChangeRequestProvider.NAME,
        service = {DataExportWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesTimeSeriesBulkChangeRequestProvider.NAME})
public class UtilitiesTimeSeriesBulkChangeRequestProvider extends AbstractUtilitiesTimeSeriesBulkRequestProvider<UtilsTmeSersERPItmBulkChgReqMsg> {
    static final String NAME = "SAP UtilitiesTimeSeriesERPItemBulkChangeRequest_C_Out";
    private static final QName QNAME = new QName("urn:webservices.wsdl.soap.sap.mdc.energyict.com:utilitiestimeseriesbulkchangerequest",
            "UtilitiesTimeSeriesERPItemBulkChangeRequest_E_OutService");

    private Map<String, UtilitiesTimeSeriesERPItemBulkChangeRequestCOut> changeRequestPorts = new ConcurrentHashMap<>();

    public UtilitiesTimeSeriesBulkChangeRequestProvider() {
        // for OSGi purposes
    }

    @Inject
    public UtilitiesTimeSeriesBulkChangeRequestProvider(WebServicesService webServicesService, PropertySpecService propertySpecService,
                                                        DataExportServiceCallType dataExportServiceCallType, Thesaurus thesaurus, Clock clock,
                                                        SAPCustomPropertySets sapCustomPropertySets) {
        super(webServicesService, propertySpecService, dataExportServiceCallType, thesaurus, clock, sapCustomPropertySets);
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        super.setWebServicesService(webServicesService);
    }

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

    @Reference
    public void setClock(Clock clock) {
        super.setClock(clock);
    }

    @Reference
    public void setSapCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        super.setSapCustomPropertySets(sapCustomPropertySets);
    }

    @Reference(target = "(name=" + UtilitiesTimeSeriesBulkChangeConfirmationReceiver.NAME + ")")
    public void setInboundPart(InboundSoapEndPointProvider inboundPart) {
        // need to know that response from SAP can be processed correctly
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addChangeRequestPort(UtilitiesTimeSeriesERPItemBulkChangeRequestCOut changeRequestPort, Map<String, Object> properties) {
        changeRequestPorts.put(getUrl(properties), changeRequestPort);
    }

    public void removeChangeRequestPort(UtilitiesTimeSeriesERPItemBulkChangeRequestCOut changeRequestPort) {
        changeRequestPorts.values().removeIf(port -> changeRequestPort == port);
    }

    @Override
    Optional<Consumer<UtilsTmeSersERPItmBulkChgReqMsg>> getPort(EndPointConfiguration endPointConfiguration) {
        return Optional.ofNullable(changeRequestPorts.values().stream().findFirst().get())
                .map(port -> (Consumer<UtilsTmeSersERPItmBulkChgReqMsg>) port::utilitiesTimeSeriesERPItemBulkChangeRequestCOut);
    }

    @Override
    public Service get() {
        return new UtilitiesTimeSeriesERPItemBulkChangeRequestCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesTimeSeriesERPItemBulkChangeRequestCOut.class;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<Operation> getSupportedOperations() {
        return EnumSet.of(Operation.CHANGE);
    }

    @Override
    UtilsTmeSersERPItmBulkChgReqMsg createMessage(Stream<? extends ExportData> data, String uuid) {
        UtilsTmeSersERPItmBulkChgReqMsg msg = new UtilsTmeSersERPItmBulkChgReqMsg();
        Instant now = getClock().instant();
        msg.setMessageHeader(createMessageHeader(uuid, now));
        data.filter(MeterReadingData.class::isInstance)
                .map(MeterReadingData.class::cast)
                .forEach(item -> addDataItem(msg, item, now));
        return msg;
    }

    private static BusinessDocumentMessageHeader createMessageHeader(String uuid, Instant now) {
        BusinessDocumentMessageHeader header = new BusinessDocumentMessageHeader();
        header.setID(createID(uuid));
        header.setUUID(createUUID(uuid));
        header.setCreationDateTime(now);
        return header;
    }

    private static BusinessDocumentMessageID createID(String id) {
        BusinessDocumentMessageID messageID = new BusinessDocumentMessageID();
        messageID.setValue(id);
        return messageID;
    }

    private static com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UUID messageUUID
                = new com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    private void addDataItem(UtilsTmeSersERPItmBulkChgReqMsg msg, MeterReadingData item, Instant now) {
        ReadingType readingType = item.getItem().getReadingType();
        TemporalAmount interval = readingType.getIntervalLength()
                .orElse(Duration.ZERO);
        String unit = readingType.getMultiplier().getSymbol() + readingType.getUnit().getSymbol();
        MeterReading meterReading = item.getMeterReading();
        IdentifiedObject meter = item.getItem().getDomainObject();
        Range<Instant> allReadingsRange = getRange(meterReading);
        item.getItem().getReadingContainer().getChannelsContainers().stream()
                .filter(cc -> cc.getInterval().toOpenClosedRange().isConnected(allReadingsRange))
                .map(cc -> Pair.of(cc, cc.getInterval().toOpenClosedRange().intersection(allReadingsRange)))
                .filter(ccAndRange -> !ccAndRange.getLast().isEmpty())
                .map(ccAndRange -> ccAndRange.getFirst().getChannel(readingType)
                        .map(channel -> Pair.of(channel, ccAndRange.getLast())))
                .flatMap(Functions.asStream())
                .flatMap(channelAndRange -> getTimeSlicedLRN(channelAndRange.getFirst(), channelAndRange.getLast(), meter).entrySet().stream())
                .map(lrnAndRange -> createRequestItem(lrnAndRange.getKey(), lrnAndRange.getValue(), meterReading, interval, unit, now))
                .forEach(msg.getUtilitiesTimeSeriesERPItemChangeRequestMessage()::add);
    }

    private static UtilsTmeSersERPItmChgReqMsg createRequestItem(String lrn, RangeSet<Instant> rangeSet, MeterReading meterReading, TemporalAmount interval, String unit, Instant now) {
        UtilsTmeSersERPItmChgReqMsg msg = new UtilsTmeSersERPItmChgReqMsg();
        msg.setMessageHeader(createMessageHeader(UUID.randomUUID().toString(), now));
        msg.setUtilitiesTimeSeries(createTimeSeries(lrn, rangeSet, meterReading, interval, unit));
        return msg;
    }

    private static UtilsTmeSersERPItmChgReqUtilsTmeSers createTimeSeries(String lrn, RangeSet<Instant> rangeSet, MeterReading meterReading, TemporalAmount interval, String unit) {
        UtilsTmeSersERPItmChgReqUtilsTmeSers timeSeries = new UtilsTmeSersERPItmChgReqUtilsTmeSers();
        timeSeries.setID(createTimeSeriesID(lrn));
        meterReading.getIntervalBlocks().stream()
                .map(IntervalBlock::getIntervals)
                .flatMap(List::stream)
                .filter(reading -> rangeSet.contains(reading.getTimeStamp()))
                .map(reading -> createItem(reading, interval, unit))
                .forEach(timeSeries.getItem()::add);
        meterReading.getReadings().stream()
                .filter(reading -> rangeSet.contains(reading.getTimeStamp()))
                .map(reading -> createItem(reading, unit))
                .forEach(timeSeries.getItem()::add);
        return timeSeries;
    }

    private static UtilitiesTimeSeriesID createTimeSeriesID(String lrn) {
        UtilitiesTimeSeriesID id = new UtilitiesTimeSeriesID();
        id.setValue(lrn);
        return id;
    }

    private static UtilsTmeSersERPItmChgReqItm createItem(IntervalReading reading, TemporalAmount interval, String unit) {
        UtilsTmeSersERPItmChgReqItm item = new UtilsTmeSersERPItmChgReqItm();
        item.setUTCValidityStartDateTime(reading.getTimeStamp().minus(interval));
        item.setUTCValidityEndDateTime(reading.getTimeStamp());
        item.setQuantity(createQuantity(reading.getValue(), unit));
        item.getItemStatus().add(createStatus("0"));
        return item;
    }

    private static UtilsTmeSersERPItmChgReqItm createItem(Reading reading, String unit) {
        UtilsTmeSersERPItmChgReqItm item = new UtilsTmeSersERPItmChgReqItm();
        item.setUTCValidityEndDateTime(reading.getTimeStamp());
        item.setQuantity(createQuantity(reading.getValue(), unit));
        item.getItemStatus().add(createStatus("0"));
        return item;
    }

    private static Quantity createQuantity(BigDecimal value, String unit) {
        Quantity quantity = new Quantity();
        quantity.setUnitCode(unit);
        quantity.setValue(value);
        return quantity;
    }

    private static UtilsTmeSersERPItmChgReqItmSts createStatus(String code) {
        UtilsTmeSersERPItmChgReqItmSts status = new UtilsTmeSersERPItmChgReqItmSts();
        UtilitiesTimeSeriesItemTypeCode typeCode = new UtilitiesTimeSeriesItemTypeCode();
        typeCode.setValue(code);
        status.setUtilitiesTimeSeriesItemTypeCode(typeCode);
        return status;
    }
}
