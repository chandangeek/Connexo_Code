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
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.Quantity;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesERPItemBulkCreateRequestEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesERPItemBulkCreateRequestEOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesItemTypeCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmCrteReqItm;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmCrteReqItmSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmCrteReqUtilsTmeSers;

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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Component(name = UtilitiesTimeSeriesBulkCreateRequestProvider.NAME,
        service = {DataExportWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesTimeSeriesBulkCreateRequestProvider.NAME})
public class UtilitiesTimeSeriesBulkCreateRequestProvider extends AbstractUtilitiesTimeSeriesBulkRequestProvider<UtilitiesTimeSeriesERPItemBulkCreateRequestEOut, UtilsTmeSersERPItmBulkCrteReqMsg> implements ApplicationSpecific {
    static final String NAME = "SAP UtilitiesTimeSeriesERPItemBulkCreateRequest_C_Out";
    private static final QName QNAME = new QName("urn:webservices.wsdl.soap.sap.mdc.energyict.com:utilitiestimeseriesbulkcreaterequest",
            "UtilitiesTimeSeriesERPItemBulkCreateRequest_E_OutService");

    public UtilitiesTimeSeriesBulkCreateRequestProvider() {
        // for OSGi purposes
    }

    @Inject
    public UtilitiesTimeSeriesBulkCreateRequestProvider(PropertySpecService propertySpecService,
                                                        DataExportServiceCallType dataExportServiceCallType, Thesaurus thesaurus, Clock clock,
                                                        SAPCustomPropertySets sapCustomPropertySets) {
        super(propertySpecService, dataExportServiceCallType, thesaurus, clock, sapCustomPropertySets);
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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCreateRequestEndpoint(UtilitiesTimeSeriesERPItemBulkCreateRequestEOut createRequestPort, Map<String, Object> properties) {
        super.doAddEndpoint(createRequestPort, properties);
    }

    public void removeCreateRequestEndpoint(UtilitiesTimeSeriesERPItemBulkCreateRequestEOut createRequestPort) {
        super.doRemoveEndpoint(createRequestPort);
    }

    @Override
    String getMessageSenderMethod() {
        return UtilitiesTimeSeriesERPItemBulkCreateRequestEOut.class.getMethods()[0].getName();
    }

    @Override
    public Service get() {
        return new UtilitiesTimeSeriesERPItemBulkCreateRequestEOutService(
                UtilitiesTimeSeriesERPItemBulkCreateRequestEOutService.class.getResource("/wsdl/sap/UtilitiesTimeSeriesERPItemBulkCreateRequest_E_OutService.wsdl"), QNAME);
    }

    @Override
    public Class<UtilitiesTimeSeriesERPItemBulkCreateRequestEOut> getService() {
        return UtilitiesTimeSeriesERPItemBulkCreateRequestEOut.class;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<Operation> getSupportedOperations() {
        return EnumSet.of(Operation.CREATE);
    }

    @Override
    UtilsTmeSersERPItmBulkCrteReqMsg createMessage(Stream<? extends ExportData> data, String uuid) {
        UtilsTmeSersERPItmBulkCrteReqMsg msg = new UtilsTmeSersERPItmBulkCrteReqMsg();
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

    private static com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UUID messageUUID
                = new com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    private void addDataItem(UtilsTmeSersERPItmBulkCrteReqMsg msg, MeterReadingData item, Instant now) {
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
                .forEach(msg.getUtilitiesTimeSeriesERPItemCreateRequestMessage()::add);
    }

    private static UtilsTmeSersERPItmCrteReqMsg createRequestItem(BigDecimal lrn, RangeSet<Instant> rangeSet, MeterReading meterReading, TemporalAmount interval, String unit, Instant now) {
        UtilsTmeSersERPItmCrteReqMsg msg = new UtilsTmeSersERPItmCrteReqMsg();
        msg.setMessageHeader(createMessageHeader(UUID.randomUUID().toString(), now));
        msg.setUtilitiesTimeSeries(createTimeSeries(lrn, rangeSet, meterReading, interval, unit));
        return msg;
    }

    private static UtilsTmeSersERPItmCrteReqUtilsTmeSers createTimeSeries(BigDecimal lrn, RangeSet<Instant> rangeSet, MeterReading meterReading, TemporalAmount interval, String unit) {
        UtilsTmeSersERPItmCrteReqUtilsTmeSers timeSeries = new UtilsTmeSersERPItmCrteReqUtilsTmeSers();
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

    private static UtilitiesTimeSeriesID createTimeSeriesID(BigDecimal lrn) {
        UtilitiesTimeSeriesID id = new UtilitiesTimeSeriesID();
        id.setValue(lrn.toPlainString());
        return id;
    }

    private static UtilsTmeSersERPItmCrteReqItm createItem(IntervalReading reading, TemporalAmount interval, String unit) {
        UtilsTmeSersERPItmCrteReqItm item = new UtilsTmeSersERPItmCrteReqItm();
        item.setUTCValidityStartDateTime(reading.getTimeStamp().minus(interval));
        item.setUTCValidityEndDateTime(reading.getTimeStamp());
        item.setQuantity(createQuantity(reading.getValue(), unit));
        item.getItemStatus().add(createStatus("0"));
        return item;
    }

    private static UtilsTmeSersERPItmCrteReqItm createItem(Reading reading, String unit) {
        UtilsTmeSersERPItmCrteReqItm item = new UtilsTmeSersERPItmCrteReqItm();
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

    private static UtilsTmeSersERPItmCrteReqItmSts createStatus(String code) {
        UtilsTmeSersERPItmCrteReqItmSts status = new UtilsTmeSersERPItmCrteReqItmSts();
        UtilitiesTimeSeriesItemTypeCode typeCode = new UtilitiesTimeSeriesItemTypeCode();
        typeCode.setValue(code);
        status.setUtilitiesTimeSeriesItemTypeCode(typeCode);
        return status;
    }

    @Override
    public String getApplication(){
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
