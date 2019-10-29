/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.eventmanagement;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.MeterEventCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
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
import java.util.Map;
import java.util.Optional;

@Singleton
@Component(name = MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT,
        service = {MeterEventCreateRequestProvider.class, OutboundSoapEndPointProvider.class}, immediate = true,
        property = {"name=" + MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT})
public class MeterEventCreateRequestProviderImpl extends AbstractOutboundEndPointProvider<UtilitiesSmartMeterEventERPBulkCreateRequestCOut>
        implements MeterEventCreateRequestProvider, OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterEventCreateRequestProviderImpl() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUtilitiesSmartMeterEventERPBulkCreateRequestEOut(UtilitiesSmartMeterEventERPBulkCreateRequestCOut out, Map<String, Object> properties) {
        super.doAddEndpoint(out, properties);
    }

    public void removeUtilitiesSmartMeterEventERPBulkCreateRequestEOut(UtilitiesSmartMeterEventERPBulkCreateRequestCOut out) {
        super.doRemoveEndpoint(out);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Override
    public Service get() {
        return new UtilitiesSmartMeterEventERPBulkCreateRequestCOutService();
    }

    @Override
    public Class<UtilitiesSmartMeterEventERPBulkCreateRequestCOut> getService() {
        return UtilitiesSmartMeterEventERPBulkCreateRequestCOut.class;
    }

    @Override
    protected String getName() {
        return MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT;
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
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
