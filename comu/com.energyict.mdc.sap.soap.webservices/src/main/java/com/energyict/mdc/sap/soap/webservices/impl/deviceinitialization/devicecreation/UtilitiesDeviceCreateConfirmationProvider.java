/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfUtilsDvce;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationProvider",
        service = {UtilitiesDeviceCreateConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceCreateConfirmation.NAME})
public class UtilitiesDeviceCreateConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterCreateConfirmationCOut> implements UtilitiesDeviceCreateConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public UtilitiesDeviceCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService){
        // No action, just for binding WebServicesService
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterCreateConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterCreateConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService();
    }

    @Override
    public Class<UtilitiesDeviceERPSmartMeterCreateConfirmationCOut> getService() {
        return UtilitiesDeviceERPSmartMeterCreateConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return UtilitiesDeviceCreateConfirmation.NAME;
    }

    @Override
    public void call(UtilitiesDeviceCreateConfirmationMessage msg) {
        SetMultimap<String, String> values = HashMultimap.create();

        UtilsDvceERPSmrtMtrCrteConfMsg confirmationMessage = msg.getConfirmationMessage()
                .orElseThrow(() -> new IllegalStateException("Unable to get confirmation message"));

        getDeviceId(confirmationMessage).ifPresent(value->values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));

        using("utilitiesDeviceERPSmartMeterCreateConfirmationCOut")
                .withRelatedAttributes(values)
                .send(confirmationMessage);
    }

    private static Optional<String> getDeviceId(UtilsDvceERPSmrtMtrCrteConfMsg msg) {
        return Optional.ofNullable(msg)
                .map(UtilsDvceERPSmrtMtrCrteConfMsg::getUtilitiesDevice)
                .map(UtilsDvceERPSmrtMtrCrteConfUtilsDvce::getID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
