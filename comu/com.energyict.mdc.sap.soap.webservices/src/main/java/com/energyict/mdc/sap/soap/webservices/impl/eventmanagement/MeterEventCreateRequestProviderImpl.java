/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.eventmanagement;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.MeterEventCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.SOAUtilitiesSmartMeterEventERPBulkCreateRequest;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.SOAUtilitiesSmartMeterEventERPBulkCreateRequestService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Singleton;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.Map;

@Singleton
@Component(name = MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT,
        service = {MeterEventCreateRequestProvider.class, OutboundSoapEndPointProvider.class}, immediate = true,
        property = {"name=" + MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT})
public class MeterEventCreateRequestProviderImpl extends AbstractOutboundEndPointProvider<SOAUtilitiesSmartMeterEventERPBulkCreateRequest> implements MeterEventCreateRequestProvider, OutboundSoapEndPointProvider, ApplicationSpecific {

    private static final QName QNAME = new QName("http://dewa.gov.ae/AMI/Bulk", "SOA_UtilitiesSmartMeterEventERPBulkCreateRequestService");
    private static final String RESOURCE = "/wsdl/sap/UtilitiesSmartMeterEventERPBulkCreateRequestService.wsdl";

    public MeterEventCreateRequestProviderImpl() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSOAUtilitiesSmartMeterEventERPBulkCreateRequest(SOAUtilitiesSmartMeterEventERPBulkCreateRequest out, Map<String, Object> properties) {
        super.doAddEndpoint(out, properties);
    }

    public void removeSOAUtilitiesSmartMeterEventERPBulkCreateRequest(SOAUtilitiesSmartMeterEventERPBulkCreateRequest out) {
        super.doRemoveEndpoint(out);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Override
    public Service get() {
        return new SOAUtilitiesSmartMeterEventERPBulkCreateRequestService(
                getService().getClassLoader().getResource(RESOURCE), QNAME);
    }

    @Override
    public Class getService() {
        return SOAUtilitiesSmartMeterEventERPBulkCreateRequest.class;
    }

    @Override
    protected String getName() {
        return MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT;
    }

    @Override
    public void send(UtilsSmrtMtrEvtERPBulkCrteReqMsg reqMsg) {
        using("soaUtilitiesSmartMeterEventERPBulkCreateRequest")
                .send(reqMsg);
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}