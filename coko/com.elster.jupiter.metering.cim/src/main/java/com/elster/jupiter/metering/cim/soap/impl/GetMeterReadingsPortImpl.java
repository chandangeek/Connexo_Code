/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim.soap.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;

import ch.iec.tc57._2011.getmeterreadings.GetMeterReadingsPort;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.time.Clock;

class GetMeterReadingsPortImpl extends AbstractInboundEndPoint implements GetMeterReadingsPort, ApplicationSpecific {
    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
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
    ) {
        runInTransactionWithOccurrence(() -> {
            newHandler().getMeterReadings(header, request, payload, reply);
            return null;
        });

    }

    GetMeterReadingsPortImpl(MeteringService meteringService, MeteringGroupsService meteringGroupsService, Clock clock) {
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.clock = clock;
    }

    private GetMeterReadingsHandler newHandler() {
        return new GetMeterReadingsHandler(meteringService, meteringGroupsService);
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
    }
}
