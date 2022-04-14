/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceBulkCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrBlkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrCrteConfUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceBulkCreateConfirmationProvider",
        service = {UtilitiesDeviceBulkCreateConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceBulkCreateConfirmation.NAME})
public class UtilitiesDeviceBulkCreateConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut> implements UtilitiesDeviceBulkCreateConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public UtilitiesDeviceBulkCreateConfirmationProvider() {
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
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut.class;
    }

    @Override
    public void call(UtilitiesDeviceCreateConfirmationMessage msg) {
        SetMultimap<String, String> values = HashMultimap.create();

        UtilsDvceERPSmrtMtrBlkCrteConfMsg bulkConfirmationMessage = msg.getBulkConfirmationMessage()
                .orElseThrow(() -> new IllegalStateException("Unable to get confirmation message"));

        bulkConfirmationMessage.getUtilitiesDeviceERPSmartMeterCreateConfirmationMessage()
                .forEach(message -> getDeviceId(message).ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value)));

        using("utilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut")
                .withRelatedAttributes(values)
                .send(bulkConfirmationMessage);
    }

    private static Optional<String> getDeviceId(UtilsDvceERPSmrtMtrCrteConfMsg msg) {
        return Optional.ofNullable(msg)
                .map(UtilsDvceERPSmrtMtrCrteConfMsg::getUtilitiesDevice)
                .map(UtilsDvceERPSmrtMtrCrteConfUtilsDvce::getID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    protected String getName() {
        return UtilitiesDeviceBulkCreateConfirmation.NAME;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
