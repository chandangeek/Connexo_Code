/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsEventMessageType;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsResponseMessageType;
import ch.iec.tc57._2011.meterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.sendmeterreadings.FaultMessage;
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;
import ch.iec.tc57._2011.sendmeterreadings.SendMeterReadings;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.meterreadings.SendMeterReadingsProviderImpl",
        service = {SendMeterReadingsProvider.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + SendMeterReadingsProvider.NAME})
public class SendMeterReadingsProviderImpl implements SendMeterReadingsProvider, OutboundSoapEndPointProvider {
    private static final Logger LOGGER = Logger.getLogger(SendMeterReadingsProviderImpl.class.getName());

    private static final QName QNAME = new QName("http://iec.ch/TC57/2011/SendMeterReadings", "SendMeterReadings");
    private static final String RESOURCE = "/meterreadings/SendMeterReadings.wsdl";
    private static final String NOUN = "MeterReadings";
    private static final String URL = "url";

    private final Map<String, MeterReadingsPort> meterReadingsPorts = new ConcurrentHashMap<>();
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ObjectFactory meterReadingsMessageObjectFactory = new ObjectFactory();
    private final MeterReadingsBuilder readingBuilderProvider = new MeterReadingsBuilder();
    private volatile WebServicesService webServicesService;

    public SendMeterReadingsProviderImpl() {
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingsPort(MeterReadingsPort out, Map<String, Object> properties) {
        meterReadingsPorts.put(properties.get(URL).toString(), out);
    }

    public void removeMeterReadingsPort(MeterReadingsPort out) {
        meterReadingsPorts.values().removeIf(port -> out == port);
    }

    public Map<String, MeterReadingsPort> getMeterReadingsPorts() {
        return Collections.unmodifiableMap(meterReadingsPorts);
    }

    @Override
    public Service get() {
        return new SendMeterReadings(getService().getClassLoader().getResource(RESOURCE), QNAME);
    }

    @Override
    public Class getService() {
        return MeterReadingsPort.class;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    public String getWebServiceName() {
        return SendMeterReadingsProvider.NAME;
    }

    public void call(List<ReadingInfo> readingInfos, HeaderType.Verb requestVerb) {
        MeterReadings meterReadings = readingBuilderProvider.build(readingInfos);
        if (checkMeterReadingsAndMeterReadingsPorts(meterReadings)) {
            meterReadingsPorts.forEach((url, soapService) ->
                    sendMeterReadingsPortResponse(soapService, meterReadings, getHeader(requestVerb))
            );
        }
    }

    public boolean call(MeterReadings meterReadings, HeaderType header, EndPointConfiguration endPointConfiguration) {
        if (!checkMeterReadingsAndMeterReadingsPorts(meterReadings)) {
            return false;
        }
        MeterReadingsPort meterReadingsPort = getMeterReadingsPorts().get(endPointConfiguration.getUrl());
        if (meterReadingsPort == null) {
            LOGGER.log(Level.SEVERE, "No meter reading port was found for url: " + endPointConfiguration.getUrl());
            return false;
        }
        return sendMeterReadingsPortResponse(meterReadingsPort, meterReadings, header);
    }

    protected MeterReadingsEventMessageType createMeterReadingsEventMessage(MeterReadings meterReadings, HeaderType header) {
        MeterReadingsEventMessageType meterReadingsResponseMessageType = meterReadingsMessageObjectFactory.createMeterReadingsEventMessageType();

        meterReadingsResponseMessageType.setHeader(header);

        // set payload
        MeterReadingsPayloadType meterReadingsPayloadType = meterReadingsMessageObjectFactory.createMeterReadingsPayloadType();
        meterReadingsPayloadType.setMeterReadings(meterReadings);
        meterReadingsResponseMessageType.setPayload(meterReadingsPayloadType);

        return meterReadingsResponseMessageType;
    }

    private HeaderType getHeader(HeaderType.Verb requestVerb) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(requestVerb);
        header.setNoun(NOUN);
        return header;
    }

    private boolean sendMeterReadingsPortResponse(MeterReadingsPort meterReadingsPort, MeterReadings meterReadings, HeaderType header) {
        MeterReadingsResponseMessageType meterReadingsResponseMessageType;
        try {
            if (header.getVerb().equals(HeaderType.Verb.CREATED)) {
                meterReadingsResponseMessageType = meterReadingsPort.createdMeterReadings(createMeterReadingsEventMessage(meterReadings, header));
            } else if (header.getVerb().equals(HeaderType.Verb.CHANGED)) {
                meterReadingsResponseMessageType = meterReadingsPort.changedMeterReadings(createMeterReadingsEventMessage(meterReadings, header));
            } else {
                LOGGER.log(Level.SEVERE, "Unknown request type to send meter readings.");
                return false;
            }
        } catch (FaultMessage faultMessage) {
            LOGGER.log(Level.SEVERE, faultMessage.getLocalizedMessage(), faultMessage);
            return false;
        }
        if (meterReadingsResponseMessageType == null
                || ReplyType.Result.OK != meterReadingsResponseMessageType.getReply().getResult()) {
            return false;
        }
        return true;
    }

    private boolean checkMeterReadingsAndMeterReadingsPorts(MeterReadings meterReadings) {
        if (meterReadings.getMeterReading().isEmpty()) {
            LOGGER.log(Level.SEVERE, "No meter readings to send.");
            return false;
        }
        if (meterReadingsPorts.isEmpty()) {
            LOGGER.log(Level.SEVERE, "No published web service endpoint is found to send meter readings.");
            return false;
        }
        return true;
    }
}