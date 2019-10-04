/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;

@Component(name = StatusChangeRequestCancellationConfirmation.NAME,
        service = {StatusChangeRequestCancellationConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + StatusChangeRequestCancellationConfirmation.NAME})
public class StatusChangeRequestCancellationConfirmationProvider extends AbstractOutboundEndPointProvider<SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut> implements StatusChangeRequestCancellationConfirmation,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    public StatusChangeRequestCancellationConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return StatusChangeRequestCancellationConfirmation.NAME;
    }

    @Override
    public void call(StatusChangeRequestCancellationConfirmationMessage msg) {
        using("smartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut")
                .send(msg.getConfirmationMessage().orElseThrow(() -> new IllegalStateException("Confirmation message is empty.")));
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

}
