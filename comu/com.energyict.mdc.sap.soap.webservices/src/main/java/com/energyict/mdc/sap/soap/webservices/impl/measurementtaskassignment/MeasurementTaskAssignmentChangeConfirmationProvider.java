/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MeasurementTaskAssignmentChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilitiesTimeSeriesID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilsTmeSersERPMsmtTskAssgmtChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilsTmeSersERPMsmtTskAssgmtChgConfUtilsTmeSers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.measurementtaskassignment.confirmation.outbound.provider",
        service = {MeasurementTaskAssignmentChangeConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeasurementTaskAssignmentChangeConfirmation.SAP_MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATION})
public class MeasurementTaskAssignmentChangeConfirmationProvider extends AbstractOutboundEndPointProvider<UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut> implements MeasurementTaskAssignmentChangeConfirmation, OutboundSoapEndPointProvider, ApplicationSpecific {

    private volatile Thesaurus thesaurus;
    private volatile Clock clock;

    public MeasurementTaskAssignmentChangeConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        this.thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService){
        // No action, just for binding WebServicesService
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
    public Class<UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut> getService() {
        return UtilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return MeasurementTaskAssignmentChangeConfirmation.SAP_MEASUREMENT_TASK_ASSIGNMENT_CHANGE_CONFIRMATION;
    }

    @Override
    public void call(MeasurementTaskAssignmentChangeConfirmationMessage confirmationMessage) {
        SetMultimap<String, String> values = HashMultimap.create();
        UtilsTmeSersERPMsmtTskAssgmtChgConfMsg message = confirmationMessage.getConfirmationMessage()
                .orElseThrow(() -> new IllegalStateException("Confirmation message is empty."));

        getUtilitiesTimeSeriesId(message).ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(), value));

        using("utilitiesTimeSeriesERPMeasurementTaskAssignmentChangeConfirmationCOut")
                .withRelatedAttributes(values)
                .send(message);
    }

    private static Optional<String> getUtilitiesTimeSeriesId(UtilsTmeSersERPMsmtTskAssgmtChgConfMsg msg) {
        return Optional.ofNullable(msg)
                .map(UtilsTmeSersERPMsmtTskAssgmtChgConfMsg::getUtilitiesTimeSeries)
                .map(UtilsTmeSersERPMsmtTskAssgmtChgConfUtilsTmeSers::getID)
                .map(UtilitiesTimeSeriesID::getValue);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
