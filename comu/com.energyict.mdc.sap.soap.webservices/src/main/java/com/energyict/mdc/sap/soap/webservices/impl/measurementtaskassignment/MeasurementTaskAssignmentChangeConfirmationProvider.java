/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MeasurementTaskAssignmentChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilsTmeSersERPMsmtTskAssgmtChgConfMsg;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.time.Clock;
import java.util.Map;

@Component(name = "com.energyict.mdc.sap.measurementtaskassignment.confirmation.outbound.provider",
        service = {MeasurementTaskAssignmentChangeConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeasurementTaskAssignmentChangeConfirmation.SAP_MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATION})
public class MeasurementTaskAssignmentChangeConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut> implements MeasurementTaskAssignmentChangeConfirmation, OutboundSoapEndPointProvider, ApplicationSpecific {

//    private final Map<String, UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut> ports = new HashMap<>();

    private volatile Thesaurus thesaurus;
    private volatile Clock clock;

    public MeasurementTaskAssignmentChangeConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut port,
                                           Map<String, Object> properties) {
/*        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));*/
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut port) {
        super.doRemoveEndpoint(port);
        //ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        this.thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Service get() {
        return new UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return MeasurementTaskAssignmentChangeConfirmation.SAP_MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATION;
    }

    @Override
    public void call(MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage) {
        UtilsTmeSersERPMsmtTskAssgmtChgConfMsg message = confirmationMessage.getConfirmationMessage();
        using("utilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut")
                .send(message);
/*        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        ports.values().stream().findFirst().get().utilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut(confirmationMessage.getConfirmationMessage());*/
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
