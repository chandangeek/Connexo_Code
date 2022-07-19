/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.eventmanagement;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.MeterEventCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.events.MeterEventCreateRequestFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventERPBulkCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventERPBulkCreateRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Singleton;
import javax.xml.ws.Service;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.eventmanagement.MeterEventCreateRequestProviderImpl",
        service = {DataExportWebService.class, MeterEventCreateRequestProvider.class, OutboundSoapEndPointProvider.class}, immediate = true,
        property = {"name=" + MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT})
public class MeterEventCreateRequestProviderImpl extends AbstractOutboundEndPointProvider<UtilitiesSmartMeterEventERPBulkCreateRequestCOut>
        implements DataExportWebService, MeterEventCreateRequestProvider, OutboundSoapEndPointProvider, ApplicationSpecific {

    private volatile Clock clock;
    private volatile WebServiceActivator webServiceActivator;

    private SAPCustomPropertySets sapCustomPropertySets;

    public MeterEventCreateRequestProviderImpl() {
        // for OSGI purposes
    }

    // for OSGI purposes
    public MeterEventCreateRequestProviderImpl(Clock clock, SAPCustomPropertySets sapCustomPropertySets, WebServiceActivator webServiceActivator) {
        setClock(clock);
        setSapCustomPropertySets(sapCustomPropertySets);
        setWebServiceActivator(webServiceActivator);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUtilitiesSmartMeterEventERPBulkCreateRequestEOut(UtilitiesSmartMeterEventERPBulkCreateRequestCOut out, Map<String, Object> properties) {
        super.doAddEndpoint(out, properties);
    }

    public void removeUtilitiesSmartMeterEventERPBulkCreateRequestEOut(UtilitiesSmartMeterEventERPBulkCreateRequestCOut out) {
        super.doRemoveEndpoint(out);
    }

    @Override
    public Service get() {
        return new UtilitiesSmartMeterEventERPBulkCreateRequestCOutService();
    }

    @Override
    public Class<UtilitiesSmartMeterEventERPBulkCreateRequestCOut> getService() {
        return UtilitiesSmartMeterEventERPBulkCreateRequestCOut.class;
    }

    public void call(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data, ExportContext context) {
        Map<EndDevice, Boolean> pushEventsToSapFlagCache = new HashMap<>();
        List<EndDeviceEventRecord> events = new ArrayList<>();
        List<MeterEventData> meterEventDataList = data.filter(MeterEventData.class::isInstance)
                .map(MeterEventData.class::cast)
                .collect(Collectors.toList());
        meterEventDataList.forEach(eventData -> {
            eventData.getMeterReading().getEvents().forEach(event -> {
                EndDevice endDevice = ((EndDeviceEventRecord) event).getEndDevice();
                Boolean pushEventsToSapFlag = pushEventsToSapFlagCache.get(endDevice);
                if (pushEventsToSapFlag == null) {
                    pushEventsToSapFlag = new Boolean(sapCustomPropertySets.isPushEventsToSapFlagSet(endDevice));
                    pushEventsToSapFlagCache.put(endDevice, pushEventsToSapFlag);
                }
                if (pushEventsToSapFlag) {
                    events.add((EndDeviceEventRecord) event);
                }
            });
        });
        Optional<UtilsSmrtMtrEvtERPBulkCrteReqMsg> bulkMessage = createBulkMessage(events.stream().toArray(EndDeviceEventRecord[]::new));
        if (bulkMessage.isPresent()) {
            send(bulkMessage.get());
        }
    }

    @Override
    public String getName() {
        return MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT;
    }

    @Override
    public String getSupportedDataType() {
        return DataExportService.STANDARD_EVENT_DATA_TYPE;
    }

    @Override
    public Set<Operation> getSupportedOperations() {
        return EnumSet.of(Operation.CREATE);
    }

    @Override
    public Optional<UtilsSmrtMtrEvtERPBulkCrteReqMsg> createBulkMessage(EndDeviceEventRecord... event) {
        Instant time = clock.instant();
        MeterEventCreateRequestFactory meterEventCreateRequestFactory = webServiceActivator.getMeterEventCreateRequestFactory();
        return meterEventCreateRequestFactory.getMeterEventBulkMessage(time, webServiceActivator.getMeteringSystemId(), event);
    }

    @Override
    public void send(UtilsSmrtMtrEvtERPBulkCrteReqMsg reqMsg) {
        SetMultimap<String, String> values = HashMultimap.create();

        reqMsg.getUtilitiesSmartMeterEventERPCreateRequestMessage().stream()
                .map(MeterEventCreateRequestProviderImpl::getDeviceId)
                .flatMap(Functions.asStream())
                .forEach(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));

        using("utilitiesSmartMeterEventERPBulkCreateRequestCOut")
                .withRelatedAttributes(values)
                .send(reqMsg);
    }

    private static Optional<String> getDeviceId(UtilsSmrtMtrEvtERPCrteReqMsg msg) {
        return Optional.ofNullable(msg)
                .map(UtilsSmrtMtrEvtERPCrteReqMsg::getUtilitiesSmartMeterEvent)
                .map(UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt::getUtilitiesDeviceID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setSapCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }
}
