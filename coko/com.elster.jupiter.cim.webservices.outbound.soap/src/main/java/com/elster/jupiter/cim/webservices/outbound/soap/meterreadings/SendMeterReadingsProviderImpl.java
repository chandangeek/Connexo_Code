/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsEventMessageType;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.meterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.sendmeterreadings.FaultMessage;
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;
import ch.iec.tc57._2011.sendmeterreadings.SendMeterReadings;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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

    private final List<MeterReadingsPort> meterReadingsPortServices = new CopyOnWriteArrayList<>();
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ObjectFactory meterReadingsMessageObjectFactory = new ObjectFactory();
    private final MeterReadingsBuilder readingBuilderProvider = new MeterReadingsBuilder();

    public SendMeterReadingsProviderImpl() {
        // for OSGI purposes
    }

    public List<MeterReadingsPort> getMeterReadingsPortServices() {
        return meterReadingsPortServices;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterReadingsPortService(MeterReadingsPort out) {
        meterReadingsPortServices.add(out);
    }

    public void removeMeterReadingsPortService(MeterReadingsPort out) {
        meterReadingsPortServices.removeIf(port -> out == port);
    }

    @Override
    public Service get() {
        return new SendMeterReadings(getService().getClassLoader().getResource(RESOURCE), QNAME);
    }

    @Override
    public Class getService() {
        return MeterReadingsPort.class;
    }

    public String getWebServiceName() {
        return SendMeterReadingsProvider.NAME;
    }

    public void call(List<ReadingInfo> readingInfos, HeaderType.Verb requestVerb) {
        MeterReadings meterReadings = readingBuilderProvider.build(readingInfos);

        if (meterReadings.getMeterReading().isEmpty()) {
            // do not want to send out a message without readings info
            return;
        }
        if (meterReadingsPortServices.isEmpty()) {
            LOGGER.log(Level.SEVERE, "No published web service endpoint is found to send meter readings.");
            return;
        }
        meterReadingsPortServices.forEach(soapService -> {
            try {
                soapService.createdMeterReadings(createMeterReadingsEventMessage(meterReadings, requestVerb));
            } catch (FaultMessage faultMessage) {
                LOGGER.log(Level.SEVERE, faultMessage.getLocalizedMessage(), faultMessage);
            }
        });
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
}