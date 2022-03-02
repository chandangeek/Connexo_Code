/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.meterchange;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceMeterChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangeconfirmation.UtilitiesDeviceERPSmartMeterChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangeconfirmation.UtilitiesDeviceERPSmartMeterChangeConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangeconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangeconfirmation.UtilsDvceERPSmrtMtrChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangeconfirmation.UtilsDvceERPSmrtMtrChgConfUtilsDvce;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.meterchange.UtilitiesDeviceMeterChangeConfirmationProvider",
        service = {UtilitiesDeviceMeterChangeConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceMeterChangeConfirmation.NAME})
public class UtilitiesDeviceMeterChangeConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterChangeConfirmationCOut> implements UtilitiesDeviceMeterChangeConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public UtilitiesDeviceMeterChangeConfirmationProvider() {
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
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterChangeConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterChangeConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterChangeConfirmationCOutService();
    }

    @Override
    public Class<UtilitiesDeviceERPSmartMeterChangeConfirmationCOut> getService() {
        return UtilitiesDeviceERPSmartMeterChangeConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return UtilitiesDeviceMeterChangeConfirmation.NAME;
    }

    @Override
    public void call(UtilitiesDeviceMeterChangeConfirmationMessage msg) {
        SetMultimap<String, String> values = HashMultimap.create();

        UtilsDvceERPSmrtMtrChgConfMsg confirmationMessage = msg.getConfirmationMessage()
                .orElseThrow(() -> new IllegalStateException("Unable to get confirmation message"));

        getDeviceId(confirmationMessage).ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));

        using("utilitiesDeviceERPSmartMeterChangeConfirmationCOut")
                .withRelatedAttributes(values)
                .send(confirmationMessage);
    }

    private static Optional<String> getDeviceId(UtilsDvceERPSmrtMtrChgConfMsg msg) {
        return Optional.ofNullable(msg)
                .map(UtilsDvceERPSmrtMtrChgConfMsg::getUtilitiesDevice)
                .map(UtilsDvceERPSmrtMtrChgConfUtilsDvce::getID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
