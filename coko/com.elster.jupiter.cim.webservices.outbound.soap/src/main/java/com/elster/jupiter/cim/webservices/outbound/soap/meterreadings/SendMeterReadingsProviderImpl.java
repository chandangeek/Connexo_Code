/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsEventMessageType;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.meterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;
import ch.iec.tc57._2011.sendmeterreadings.SendMeterReadings;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.meterreadings.SendMeterReadingsProviderImpl",
        service = {SendMeterReadingsProvider.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + SendMeterReadingsProvider.NAME})
public class SendMeterReadingsProviderImpl extends AbstractOutboundEndPointProvider<MeterReadingsPort> implements SendMeterReadingsProvider, OutboundSoapEndPointProvider, ApplicationSpecific {
    private static final Logger LOGGER = Logger.getLogger(SendMeterReadingsProviderImpl.class.getName());

    private static final QName QNAME = new QName("http://iec.ch/TC57/2011/SendMeterReadings", "SendMeterReadings");
    private static final String RESOURCE = "/meterreadings/SendMeterReadings.wsdl";
    private static final String NOUN = "MeterReadings";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ObjectFactory meterReadingsMessageObjectFactory = new ObjectFactory();
    private final MeterReadingsBuilder readingBuilderProvider = new MeterReadingsBuilder();

    public SendMeterReadingsProviderImpl() {
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingsPort(MeterReadingsPort out, Map<String, Object> properties) {
        super.doAddEndpoint(out, properties);
    }

    public void removeMeterReadingsPort(MeterReadingsPort out) {
        super.doRemoveEndpoint(out);
    }

    @Reference
    public void addWebServicesService(WebServicesService webServicesService) {
        // Just to inject WebServicesService
    }

    @Override
    public Service get() {
        return new SendMeterReadings(getService().getClassLoader().getResource(RESOURCE), QNAME);
    }

    @Override
    public Class getService() {
        return MeterReadingsPort.class;
    }

    @Override
    protected String getName() {
        return SendMeterReadingsProvider.NAME;
    }

    public String getWebServiceName() {
        return getName();
    }

    public void call(List<ReadingInfo> readingInfos, HeaderType.Verb requestVerb) {
        MeterReadings meterReadings = readingBuilderProvider.build(readingInfos);
        String method;
        MeterReadingsEventMessageType message = createMeterReadingsEventMessage(meterReadings, requestVerb);
        if (requestVerb.equals(HeaderType.Verb.CREATED)) {
            method = "createdMeterReadings";
        } else if (requestVerb.equals(HeaderType.Verb.CHANGED)) {
            method = "changedMeterReadings";
        } else {
            throw new UnsupportedOperationException(requestVerb + " isn't supported.");
        }
        using(method).send(message);
    }

    public boolean call(MeterReadings meterReadings, HeaderType.Verb requestVerb, EndPointConfiguration endPointConfiguration) {
        if (!checkMeterReadingsAndMeterReadingsPorts(meterReadings)) {
            return false;
        }
        String method;
        MeterReadingsEventMessageType message = createMeterReadingsEventMessage(meterReadings, requestVerb);
        if (requestVerb.equals(HeaderType.Verb.CREATED)) {
            method = "createdMeterReadings";
        } else if (requestVerb.equals(HeaderType.Verb.CHANGED)) {
            method = "changedMeterReadings";
        } else {
            throw new UnsupportedOperationException(requestVerb + " isn't supported.");
        }
        using(method)
                .toEndpoints(endPointConfiguration)
                .send(message);
        return true;
    }

    protected MeterReadingsEventMessageType createMeterReadingsEventMessage(MeterReadings meterReadings, HeaderType.Verb requestVerb) {
        MeterReadingsEventMessageType meterReadingsResponseMessageType = meterReadingsMessageObjectFactory.createMeterReadingsEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(requestVerb);
        header.setNoun(NOUN);
        meterReadingsResponseMessageType.setHeader(header);

        // set payload
        MeterReadingsPayloadType meterReadingsPayloadType = meterReadingsMessageObjectFactory.createMeterReadingsPayloadType();
        meterReadingsPayloadType.setMeterReadings(meterReadings);
        meterReadingsResponseMessageType.setPayload(meterReadingsPayloadType);

        return meterReadingsResponseMessageType;
    }

    private boolean checkMeterReadingsAndMeterReadingsPorts(MeterReadings meterReadings) {
        if (meterReadings.getMeterReading().isEmpty()) {
            LOGGER.log(Level.SEVERE, "No meter readings to send.");
            return false;
        }
        return true;
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
    }
}