/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
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
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;
import ch.iec.tc57._2011.sendmeterreadings.SendMeterReadings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.meterreadings.SendMeterReadingsProviderImpl",
        service = {SendMeterReadingsProvider.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + SendMeterReadingsProvider.NAME})
public class SendMeterReadingsProviderImpl extends AbstractOutboundEndPointProvider<MeterReadingsPort> implements SendMeterReadingsProvider, OutboundSoapEndPointProvider, ApplicationSpecific {
    private static final Logger LOGGER = Logger.getLogger(SendMeterReadingsProviderImpl.class.getName());

    private static final QName QNAME = new QName("http://iec.ch/TC57/2011/SendMeterReadings", "SendMeterReadings");
    private static final String RESOURCE = "/wsdl/meterreadings/SendMeterReadings.wsdl";
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
        if (checkMeterReadings(meterReadings)) {
            String method;
            MeterReadingsEventMessageType message = createMeterReadingsEventMessage(meterReadings, getHeader(requestVerb));
            if (requestVerb.equals(HeaderType.Verb.CREATED)) {
                method = "createdMeterReadings";
            } else if (requestVerb.equals(HeaderType.Verb.CHANGED)) {
                method = "changedMeterReadings";
            } else {
                throw new UnsupportedOperationException(requestVerb + " isn't supported.");
            }
            SetMultimap<String, String> values = HashMultimap.create();
            readingInfos.forEach(reading->{
                reading.getMeter().ifPresent(meter->{
                    values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(),
                            meter.getName());
                    values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(),
                            meter.getMRID());
                });

                reading.getUsagePoint().ifPresent(usp->{
                    values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(),
                            usp.getName());
                    values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(),
                            usp.getMRID());
                });
            });

            using(method)
                    .withRelatedAttributes(values)
                    .send(message);
        }
    }

    public boolean call(MeterReadings meterReadings, HeaderType header, EndPointConfiguration endPointConfiguration) {
        String method;
        MeterReadingsEventMessageType message = createMeterReadingsEventMessage(meterReadings, header);
        switch(header.getVerb()) {
            case CREATED:
            case REPLY:
                method = "createdMeterReadings";
                break;
            case CHANGED:
                method = "changedMeterReadings";
                break;
            default:
                throw new UnsupportedOperationException(header.getVerb() + " isn't supported.");
        }

        SetMultimap<String, String> values = HashMultimap.create();
        meterReadings.getMeterReading().forEach(reading->{
            Optional.ofNullable(reading.getMeter()).ifPresent(meter->{
                values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(),
                        meter.getNames().get(0).getName());
                values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(),
                        meter.getMRID());
            });

            Optional.ofNullable(reading.getUsagePoint()).ifPresent(usp->{
                values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(),
                        usp.getNames().get(0).getName());
                values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(),
                        usp.getMRID());
            });
        });

        Map response = using(method)
                .toEndpoints(endPointConfiguration)
                .withRelatedAttributes(values)
                .send(message);
        if (response == null || response.get(endPointConfiguration) == null || ReplyType.Result.OK != ((MeterReadingsResponseMessageType)response.get(endPointConfiguration)).getReply().getResult()) {
            return false;
        }
        return true;
    }

    protected MeterReadingsEventMessageType createMeterReadingsEventMessage(MeterReadings meterReadings, HeaderType header) {
        MeterReadingsEventMessageType meterReadingsResponseMessageType = meterReadingsMessageObjectFactory.createMeterReadingsEventMessageType();

        // set header
        meterReadingsResponseMessageType.setHeader(header);

        // set payload
        MeterReadingsPayloadType meterReadingsPayloadType = meterReadingsMessageObjectFactory.createMeterReadingsPayloadType();
        meterReadingsPayloadType.setMeterReadings(meterReadings);
        meterReadingsResponseMessageType.setPayload(meterReadingsPayloadType);

        return meterReadingsResponseMessageType;
    }

    private boolean checkMeterReadings(MeterReadings meterReadings) {
        if (meterReadings.getMeterReading().isEmpty()) {
            LOGGER.log(Level.SEVERE, "No meter readings to send.");
            return false;
        }
        return true;
    }

    private HeaderType getHeader(HeaderType.Verb requestVerb) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(requestVerb);
        header.setNoun(NOUN);
        return header;
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
    }
}