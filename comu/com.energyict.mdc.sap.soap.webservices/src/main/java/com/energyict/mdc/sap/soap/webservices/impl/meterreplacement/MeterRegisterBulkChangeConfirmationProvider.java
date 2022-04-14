/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MeterRegisterBulkChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilsDvceERPSmrtMtrRegBulkChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilsDvceERPSmrtMtrRegChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilsDvceERPSmrtMtrRegChgConfUtilsDvce;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterBulkChangeConfirmationProvider",
        service = {MeterRegisterBulkChangeConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterRegisterBulkChangeConfirmation.NAME})
public class MeterRegisterBulkChangeConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut> implements MeterRegisterBulkChangeConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterRegisterBulkChangeConfirmationProvider() {
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
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut.class;
    }

    @Override
    public void call(MeterRegisterBulkChangeConfirmationMessage message) {
        SetMultimap<String, String> values = HashMultimap.create();
        getDeviceConfirmationMessages(message)
                .forEach(msg -> getDeviceId(msg.getUtilitiesDevice())
                        .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value)));

        using("utilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut")
                .withRelatedAttributes(values)
                .send(message.getConfirmationMessage());
    }

    private static List<UtilsDvceERPSmrtMtrRegChgConfMsg> getDeviceConfirmationMessages(MeterRegisterBulkChangeConfirmationMessage message) {
        return Optional.ofNullable(message)
                .map(MeterRegisterBulkChangeConfirmationMessage::getConfirmationMessage)
                .map(UtilsDvceERPSmrtMtrRegBulkChgConfMsg::getUtilitiesDeviceERPSmartMeterRegisterChangeConfirmationMessage)
                .orElse(Collections.emptyList());
    }

    private static Optional<String> getDeviceId(UtilsDvceERPSmrtMtrRegChgConfUtilsDvce device) {
        return Optional.ofNullable(device)
                .map(UtilsDvceERPSmrtMtrRegChgConfUtilsDvce::getID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    protected String getName() {
        return MeterRegisterBulkChangeConfirmation.NAME;
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
