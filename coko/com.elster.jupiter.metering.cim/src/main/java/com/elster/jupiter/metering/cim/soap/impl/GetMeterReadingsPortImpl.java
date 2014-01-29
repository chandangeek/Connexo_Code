package com.elster.jupiter.metering.cim.soap.impl;

import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.GetMeterReadingsPort;
import ch.iec.tc57._2011.getmeterreadings_.EndDevice;
import ch.iec.tc57._2011.getmeterreadings_.GetMeterReadings;
import ch.iec.tc57._2011.getmeterreadings_.UsagePoint;
import ch.iec.tc57._2011.getmeterreadings_.UsagePointGroup;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.meterreadings_.MeterReading;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

class GetMeterReadingsPortImpl implements GetMeterReadingsPort {

    private final ThreadPrincipalService threadPrincipalService;

    GetMeterReadingsPortImpl(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

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
        Principal principal = determinePrincipal();
        threadPrincipalService.set(principal, MessageService.COMPONENTNAME, null, null);
        try {
            List<String> endDeviceMrids = new ArrayList<>();
            List<String> usagePointMrids = new ArrayList<>();
            List<String> usagePointGroups = new ArrayList<>();

            if (request != null) {
                if (request.getGetMeterReadings() != null) {
                    GetMeterReadings getMeterReadings = request.getGetMeterReadings();
                    for (UsagePoint usagePoint : getMeterReadings.getUsagePoint()) {
                        usagePointMrids.add(usagePoint.getMRID());
                    }
                    for (EndDevice endDevice : getMeterReadings.getEndDevice()) {
                        endDeviceMrids.add(endDevice.getMRID());
                    }
                    for (UsagePointGroup usagePointGroup : getMeterReadings.getUsagePointGroup()) {
                        usagePointGroups.add(usagePointGroup.getMRID());
                    }
                }
                List<MeterReading> meterReading = payload.value.getMeterReadings().getMeterReading();

            }
        } finally {
            threadPrincipalService.clear();
        }

    }

    private Principal determinePrincipal() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }
}
