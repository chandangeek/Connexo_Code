/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;

import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;

@Component(name = UtilitiesDeviceCreateConfirmation.NAME,
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
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterCreateConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return UtilitiesDeviceCreateConfirmation.NAME;
    }

    @Override
    public void call(UtilsDvceERPSmrtMtrCrteConfMsg msg) {

        SetMultimap<String, String> values = HashMultimap.create();
        values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
            msg.getUtilitiesDevice().getID().getValue());

        using("utilitiesDeviceERPSmartMeterCreateConfirmationCOut")
                .withRelatedAttributes(values)
                .send(msg);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
