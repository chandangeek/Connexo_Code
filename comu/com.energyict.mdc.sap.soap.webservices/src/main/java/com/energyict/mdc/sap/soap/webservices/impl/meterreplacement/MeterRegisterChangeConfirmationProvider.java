/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MeterRegisterChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilsDvceERPSmrtMtrRegChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilsDvceERPSmrtMtrRegChgConfUtilsDvce;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterChangeConfirmationProvider",
        service = {MeterRegisterChangeConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterRegisterChangeConfirmation.NAME})
public class MeterRegisterChangeConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut> implements MeterRegisterChangeConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterRegisterChangeConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        // No action, just for binding WebServicesService
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return MeterRegisterChangeConfirmation.NAME;
    }

    @Override
    public void call(MeterRegisterChangeConfirmationMessage msg) {
        SetMultimap<String, String> values = HashMultimap.create();
        getDeviceId(msg.getConfirmationMessage()).ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
        using("utilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut")
                .withRelatedAttributes(values)
                .send(msg.getConfirmationMessage());
    }

    private static Optional<String> getDeviceId(UtilsDvceERPSmrtMtrRegChgConfMsg confMsg) {
        return Optional.ofNullable(confMsg)
                .map(UtilsDvceERPSmrtMtrRegChgConfMsg::getUtilitiesDevice)
                .map(UtilsDvceERPSmrtMtrRegChgConfUtilsDvce::getID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
