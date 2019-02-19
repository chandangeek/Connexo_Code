/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.eventmanagement;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.MeterEventCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@Component(name = MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT,
        service = {MeterEventCreateRequestProvider.class, OutboundSoapEndPointProvider.class}, immediate = true,
        property = {"name=" + MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT})
public class MeterEventCreateRequestProviderImpl implements MeterEventCreateRequestProvider, OutboundSoapEndPointProvider {

    private final List<SOAUtilitiesSmartMeterEventERPBulkCreateRequest> endpoints = new CopyOnWriteArrayList<>();
    private static final QName QNAME = new QName("http://dewa.gov.ae/AMI/Bulk", "SOA_UtilitiesSmartMeterEventERPBulkCreateRequestService");
    private static final String RESOURCE = "/wsdl/sap/UtilitiesSmartMeterEventERPBulkCreateRequestService.wsdl";
    private Thesaurus thesaurus;

    public MeterEventCreateRequestProviderImpl() {
        // for OSGI purposes
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addSOAUtilitiesSmartMeterEventERPBulkCreateRequest(SOAUtilitiesSmartMeterEventERPBulkCreateRequest out) {
        endpoints.add(out);
    }

    public void removeSOAUtilitiesSmartMeterEventERPBulkCreateRequest(SOAUtilitiesSmartMeterEventERPBulkCreateRequest out) {
        endpoints.removeIf(port -> out == port);
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
    public void send(UtilsSmrtMtrEvtERPBulkCrteReqMsg reqMsg) {
        if (endpoints.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        endpoints.forEach(soapService -> soapService
                .soaUtilitiesSmartMeterEventERPBulkCreateRequest(reqMsg));
    }


}