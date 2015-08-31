package com.elster.jupiter.metering.cim.soap.impl;

import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.GetMeterReadingsPort;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.getmeterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.time.Clock;

class GetMeterReadingsPortImpl implements GetMeterReadingsPort {
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final ch.iec.tc57._2011.meterreadings.ObjectFactory payloadObjectFactory = new ch.iec.tc57._2011.meterreadings.ObjectFactory();
    private final Clock clock;

    @Override
    public void getMeterReadings(
            @WebParam(name = "Header", targetNamespace = "http://iec.ch/TC57/2011/GetMeterReadingsMessage", mode = WebParam.Mode.INOUT)
            Holder<HeaderType> header,
            @WebParam(name = "Request", targetNamespace = "http://iec.ch/TC57/2011/GetMeterReadingsMessage")
            GetMeterReadingsRequestType request,
            @WebParam(name = "Payload", targetNamespace = "http://iec.ch/TC57/2011/GetMeterReadingsMessage", mode = WebParam.Mode.INOUT)
            Holder<MeterReadingsPayloadType> payload,
            @WebParam(name = "Reply", targetNamespace = "http://iec.ch/TC57/2011/GetMeterReadingsMessage", mode = WebParam.Mode.OUT)
            Holder<ReplyType> reply
    ) throws FaultMessage {
        newHandler().getMeterReadings(header, request, payload, reply);
    }

    GetMeterReadingsPortImpl(MeteringService meteringService, MeteringGroupsService meteringGroupsService, Clock clock) {
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.clock = clock;
    }

    private GetMeterReadingsHandler newHandler() {
        return new GetMeterReadingsHandler(meteringService, meteringGroupsService);
    }
}
