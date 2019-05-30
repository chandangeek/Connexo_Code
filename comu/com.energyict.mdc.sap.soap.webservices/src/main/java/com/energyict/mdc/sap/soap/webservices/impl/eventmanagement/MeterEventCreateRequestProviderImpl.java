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
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventERPBulkCreateRequestEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventERPBulkCreateRequestEOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Singleton;
import javax.xml.ws.Service;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@Component(name = MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT,
        service = {MeterEventCreateRequestProvider.class, OutboundSoapEndPointProvider.class}, immediate = true,
        property = {"name=" + MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT})
public class MeterEventCreateRequestProviderImpl implements MeterEventCreateRequestProvider, OutboundSoapEndPointProvider {

    private final List<UtilitiesSmartMeterEventERPBulkCreateRequestEOut> endpoints = new CopyOnWriteArrayList<>();
    private Thesaurus thesaurus;

    public MeterEventCreateRequestProviderImpl() {
        // for OSGI purposes
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUtilitiesSmartMeterEventERPBulkCreateRequestEOut(UtilitiesSmartMeterEventERPBulkCreateRequestEOut out) {
        endpoints.add(out);
    }

    public void removeUtilitiesSmartMeterEventERPBulkCreateRequestEOut(UtilitiesSmartMeterEventERPBulkCreateRequestEOut out) {
        endpoints.removeIf(port -> out == port);
    }

    @Override
    public Service get() {
        return new UtilitiesSmartMeterEventERPBulkCreateRequestEOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesSmartMeterEventERPBulkCreateRequestEOut.class;
    }

    @Override
    public void send(UtilsSmrtMtrEvtERPBulkCrteReqMsg reqMsg) {
        if (endpoints.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        endpoints.forEach(soapService -> soapService
                .utilitiesSmartMeterEventERPBulkCreateRequestEOut(reqMsg));
    }


}
