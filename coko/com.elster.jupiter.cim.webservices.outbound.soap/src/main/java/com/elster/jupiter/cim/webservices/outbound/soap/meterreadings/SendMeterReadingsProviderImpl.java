/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jdk.nashorn.internal.objects.NativeMath.log;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.meterreadings.provider",
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
    private Thesaurus thesaurus;

    public SendMeterReadingsProviderImpl() {
        // for OSGI purposes
    }

    @Inject
    public SendMeterReadingsProviderImpl(NlsService nlsService) {
        this();
        setNlsService(nlsService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(SendMeterReadingsProvider.NAME, Layer.SERVICE);
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

    public void call(ReadingStorer readingStorer, boolean isCreated) {
        readingBuilderProvider.setThesaurus(thesaurus);
        MeterReadings meterReadings = readingBuilderProvider.build(readingStorer);

        if (meterReadings.getMeterReading().isEmpty()) {
            // do not want to send out a message without readings info
            return;
        }
        if (meterReadingsPortServices.isEmpty()) {
            throw new MeterReadinsServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        meterReadingsPortServices.forEach(soapService -> {
            try {
                soapService.createdMeterReadings(createMeterReadingsEventMessage(meterReadings, isCreated));
            } catch (FaultMessage faultMessage) {
                LOGGER.log(Level.SEVERE, faultMessage.getLocalizedMessage(), faultMessage);
            }
        });
    }

    protected MeterReadingsEventMessageType createMeterReadingsEventMessage(MeterReadings meterReadings, boolean isCreated) {
        MeterReadingsEventMessageType meterReadingsResponseMessageType = meterReadingsMessageObjectFactory.createMeterReadingsEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        if (isCreated) {
            header.setVerb(HeaderType.Verb.CREATED);
        } else {
            header.setVerb(HeaderType.Verb.CHANGED);
        }
        header.setNoun(NOUN);
        meterReadingsResponseMessageType.setHeader(header);

        // set payload
        MeterReadingsPayloadType meterReadingsPayloadType = meterReadingsMessageObjectFactory.createMeterReadingsPayloadType();
        meterReadingsPayloadType.setMeterReadings(meterReadings);
        meterReadingsResponseMessageType.setPayload(meterReadingsPayloadType);

        return meterReadingsResponseMessageType;
    }
}